package com.cbconnectit.data.database.dao

import com.cbconnectit.data.database.tables.RefreshTokensTable
import com.cbconnectit.domain.interfaces.IRefreshTokenDao
import com.cbconnectit.utils.PasswordManager
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime
import java.util.*

private const val TOKEN_GRACE_PERIOD_SECONDS = 30L

class RefreshTokenDaoImpl : IRefreshTokenDao {

    override fun saveRefreshToken(userId: UUID, token: String, expiresAt: LocalDateTime) {
        // Hash the token before storing (security best practice)
        val hashedToken = PasswordManager.encryptPassword(token)

        RefreshTokensTable.insert {
            it[RefreshTokensTable.userId] = userId
            it[RefreshTokensTable.token] = hashedToken
            it[createdAt] = LocalDateTime.now()
            it[RefreshTokensTable.expiresAt] = expiresAt
        }
    }

    override fun isRefreshTokenValid(userId: UUID, token: String): Boolean {
        // Filter by userId first to drastically reduce the number of tokens to check
        val validTokenHashes = RefreshTokensTable.selectAll()
            .where {
                (RefreshTokensTable.userId eq userId) and
                        (RefreshTokensTable.invalidated eq false) and
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

    override fun getReplacementToken(userId: UUID, oldToken: String): String? {
        val now = LocalDateTime.now()
        val gracePeriodStart = now.minusSeconds(TOKEN_GRACE_PERIOD_SECONDS)

        // Filter by userId first, then find tokens replaced within the grace period
        val tokenRows = RefreshTokensTable.selectAll()
            .where { RefreshTokensTable.userId eq userId }
            .toList()
            .filter { row ->
                val replacedAt = row[RefreshTokensTable.replacedAt]
                replacedAt != null &&
                        replacedAt >= gracePeriodStart &&
                        replacedAt <= now
            }

        // Find the row where the old token matches
        val matchingRow = tokenRows.firstOrNull { row ->
            PasswordManager.validatePassword(oldToken, row[RefreshTokensTable.token])
        }

        // Return the replacement token hash if found
        return matchingRow?.get(RefreshTokensTable.replacedByToken)
    }

    override fun cleanupExpiredTokens() {
        val gracePeriodCutoff = LocalDateTime.now().minusSeconds(TOKEN_GRACE_PERIOD_SECONDS)

        // Delete expired tokens
        RefreshTokensTable.deleteWhere {
            (expiresAt less LocalDateTime.now()) or (invalidated eq true)
        }

        // Delete replaced tokens that are outside grace period
        val tokensToDelete = RefreshTokensTable.selectAll()
            .toList()
            .filter { row ->
                val replacedAt = row[RefreshTokensTable.replacedAt]
                replacedAt != null && replacedAt < gracePeriodCutoff
            }
            .map { it[RefreshTokensTable.token] }

        if (tokensToDelete.isNotEmpty()) {
            RefreshTokensTable.deleteWhere {
                RefreshTokensTable.token inList tokensToDelete
            }
        }
    }
}
