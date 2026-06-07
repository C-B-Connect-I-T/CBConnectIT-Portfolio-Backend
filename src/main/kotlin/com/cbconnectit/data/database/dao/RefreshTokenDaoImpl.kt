package com.cbconnectit.data.database.dao

import com.cbconnectit.data.database.tables.RefreshTokensTable
import com.cbconnectit.domain.interfaces.IRefreshTokenDao
import com.cbconnectit.utils.PasswordManager
import org.jetbrains.exposed.sql.IsNotNullOp
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime
import java.util.*

class RefreshTokenDaoImpl : IRefreshTokenDao {

    override fun saveRefreshToken(userId: UUID, token: String, expiresAt: LocalDateTime) {
        // Hash the token before storing (security best practice)
        val hashedToken = PasswordManager.encryptPassword(token)

        RefreshTokensTable.insert {
            it[RefreshTokensTable.userId] = userId
            it[RefreshTokensTable.token] = hashedToken
            it[RefreshTokensTable.createdAt] = LocalDateTime.now()
            it[RefreshTokensTable.expiresAt] = expiresAt
        }
    }

    override fun isRefreshTokenValid(userId: UUID, token: String): Boolean {
        // Filter by userId first to drastically reduce the number of tokens to check
        val validTokenHashes = RefreshTokensTable.selectAll()
            .where {
                (RefreshTokensTable.userId eq userId) and
                        (RefreshTokensTable.invalidated eq false) and
                        (RefreshTokensTable.replacedAt.isNull()) and
                        (RefreshTokensTable.expiresAt greater LocalDateTime.now())
            }
            .map { it[RefreshTokensTable.token] }

        // Check if the provided token matches any of the user's hashed tokens
        return validTokenHashes.any { hashedToken ->
            PasswordManager.validatePassword(token, hashedToken)
        }
    }

    override fun invalidateRefreshToken(userId: UUID, token: String) {
        // Filter by userId first to reduce the number of tokens to check
        val tokenHashes = RefreshTokensTable.selectAll()
            .where { RefreshTokensTable.userId eq userId }
            .map { it[RefreshTokensTable.token] }

        val matchingHash = tokenHashes.firstOrNull { hashedToken ->
            PasswordManager.validatePassword(token, hashedToken)
        }

        matchingHash?.let { hash ->
            RefreshTokensTable.update({ RefreshTokensTable.token eq hash }) {
                it[invalidated] = true
            }
        }
    }

    override fun replaceRefreshToken(userId: UUID, oldToken: String, newToken: String) {
        // Filter by userId first to reduce the number of tokens to check
        val tokenRows = RefreshTokensTable.selectAll()
            .where { RefreshTokensTable.userId eq userId }
            .map { row ->
                row[RefreshTokensTable.token] to row
            }

        val matchingRow = tokenRows.firstOrNull { (hashedToken, _) ->
            PasswordManager.validatePassword(oldToken, hashedToken)
        }?.second

        matchingRow?.let { row ->
            val oldTokenHash = row[RefreshTokensTable.token]
            val hashedNewToken = PasswordManager.encryptPassword(newToken)

            RefreshTokensTable.update({ RefreshTokensTable.token eq oldTokenHash }) {
                it[replacedByToken] = hashedNewToken
                it[replacedAt] = LocalDateTime.now()
            }
        }
    }

    override fun cleanupExpiredTokens() {
        val now = LocalDateTime.now()

        // Delete expired, invalidated, and replaced tokens in one query
        RefreshTokensTable.deleteWhere {
            (RefreshTokensTable.expiresAt less now) or
                    (RefreshTokensTable.invalidated eq true) or
                    IsNotNullOp(RefreshTokensTable.replacedAt)
        }
    }

    override fun detectReplayAttack(userId: UUID, token: String): Boolean {
        // Find the token record by userId first (reduces search space)
        val tokenRows = RefreshTokensTable.selectAll()
            .where { RefreshTokensTable.userId eq userId }
            .toList()

        val matchingRow = tokenRows.firstOrNull { row ->
            PasswordManager.validatePassword(token, row[RefreshTokensTable.token])
        }

        matchingRow?.let { row ->
            val replacedAt = row[RefreshTokensTable.replacedAt]
            if (replacedAt != null) {
                // SECURITY BREACH: Token was already rotated and is being reused
                return true
            }
        }

        return false
    }

    override fun invalidateAllUserTokens(userId: UUID) {
        RefreshTokensTable.update({ RefreshTokensTable.userId eq userId }) {
            it[invalidated] = true
        }
    }
}
