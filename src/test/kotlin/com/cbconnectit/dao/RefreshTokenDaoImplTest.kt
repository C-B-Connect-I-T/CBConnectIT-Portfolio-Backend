package com.cbconnectit.dao

import com.cbconnectit.data.database.dao.RefreshTokenDaoImpl
import com.cbconnectit.data.database.tables.RefreshTokensTable
import com.cbconnectit.data.database.tables.UsersTable
import com.cbconnectit.domain.interfaces.IRefreshTokenDao
import com.cbconnectit.utils.PasswordManager
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.selectAll
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class RefreshTokenDaoImplTest : BaseDaoTest() {

    private val dao: IRefreshTokenDao = RefreshTokenDaoImpl()

    override suspend fun seedData() {
        // Create a test user for foreign key constraint
        UsersTable.insert {
            it[id] = testUserId
            it[username] = "testuser@example.com"
            it[password] = PasswordManager.encryptPassword("password123")
            it[createdAt] = CurrentDateTime
            it[updatedAt] = CurrentDateTime
        }
    }

    companion object {
        private val testUserId = UUID.fromString("00000000-0000-0000-0000-000000000001")
    }

    // <editor-fold desc="SQL NULL Comparison Fix Tests">

    @Test
    fun `cleanupExpiredTokens should delete tokens with replacedAt IS NOT NULL`() = runTest {
        // Create three tokens
        val token1 = "refresh-token-1"
        val token2 = "refresh-token-2"
        val token3 = "refresh-token-3"
        val futureExpiry = LocalDateTime.now().plusDays(30)

        dao.saveRefreshToken(testUserId, token1, futureExpiry)
        dao.saveRefreshToken(testUserId, token2, futureExpiry)
        dao.saveRefreshToken(testUserId, token3, futureExpiry)

        // Replace token1 (sets replacedAt to non-null, but doesn't create a new token)
        dao.replaceRefreshToken(testUserId, token1, "new-token")

        // Verify we still have 3 tokens (replaceRefreshToken doesn't create a new one)
        val tokensBeforeCleanup = RefreshTokensTable.selectAll().count()
        assertThat(tokensBeforeCleanup).isEqualTo(3) // token1 (marked as replaced), token2, token3

        // Run cleanup - should delete token1 because replacedAt IS NOT NULL
        dao.cleanupExpiredTokens()

        // Verify token1 was deleted
        val tokensAfterCleanup = RefreshTokensTable.selectAll().count()
        assertThat(tokensAfterCleanup).isEqualTo(2) // token2, token3

        // Verify token1 is invalid (deleted)
        assertThat(dao.isRefreshTokenValid(testUserId, token1)).isFalse()

        // Verify other tokens are still valid
        assertThat(dao.isRefreshTokenValid(testUserId, token2)).isTrue()
        assertThat(dao.isRefreshTokenValid(testUserId, token3)).isTrue()
    }

    @Test
    fun `cleanupExpiredTokens should NOT delete tokens with replacedAt IS NULL`() = runTest {
        val token1 = "active-token-1"
        val token2 = "active-token-2"
        val futureExpiry = LocalDateTime.now().plusDays(30)

        dao.saveRefreshToken(testUserId, token1, futureExpiry)
        dao.saveRefreshToken(testUserId, token2, futureExpiry)

        // Verify tokens exist
        val tokensBeforeCleanup = RefreshTokensTable.selectAll().count()
        assertThat(tokensBeforeCleanup).isEqualTo(2)

        // Run cleanup - should NOT delete these tokens
        dao.cleanupExpiredTokens()

        // Verify tokens still exist
        val tokensAfterCleanup = RefreshTokensTable.selectAll().count()
        assertThat(tokensAfterCleanup).isEqualTo(2)

        // Verify tokens are still valid
        assertThat(dao.isRefreshTokenValid(testUserId, token1)).isTrue()
        assertThat(dao.isRefreshTokenValid(testUserId, token2)).isTrue()
    }

    @Test
    fun `cleanupExpiredTokens should delete expired tokens`() = runTest {
        val expiredToken = "expired-token"
        val validToken = "valid-token"
        val pastExpiry = LocalDateTime.now().minusDays(1)
        val futureExpiry = LocalDateTime.now().plusDays(30)

        dao.saveRefreshToken(testUserId, expiredToken, pastExpiry)
        dao.saveRefreshToken(testUserId, validToken, futureExpiry)

        // Verify both tokens exist
        val tokensBeforeCleanup = RefreshTokensTable.selectAll().count()
        assertThat(tokensBeforeCleanup).isEqualTo(2)

        // Run cleanup
        dao.cleanupExpiredTokens()

        // Verify only valid token remains
        val tokensAfterCleanup = RefreshTokensTable.selectAll().count()
        assertThat(tokensAfterCleanup).isEqualTo(1)

        assertThat(dao.isRefreshTokenValid(testUserId, expiredToken)).isFalse()
        assertThat(dao.isRefreshTokenValid(testUserId, validToken)).isTrue()
    }

    @Test
    fun `cleanupExpiredTokens should delete invalidated tokens`() = runTest {
        val invalidatedToken = "invalidated-token"
        val validToken = "valid-token"
        val futureExpiry = LocalDateTime.now().plusDays(30)

        dao.saveRefreshToken(testUserId, invalidatedToken, futureExpiry)
        dao.saveRefreshToken(testUserId, validToken, futureExpiry)

        // Invalidate first token
        dao.invalidateRefreshToken(testUserId, invalidatedToken)

        // Verify both tokens exist before cleanup
        val tokensBeforeCleanup = RefreshTokensTable.selectAll().count()
        assertThat(tokensBeforeCleanup).isEqualTo(2)

        // Run cleanup
        dao.cleanupExpiredTokens()

        // Verify only valid token remains
        val tokensAfterCleanup = RefreshTokensTable.selectAll().count()
        assertThat(tokensAfterCleanup).isEqualTo(1)

        assertThat(dao.isRefreshTokenValid(testUserId, invalidatedToken)).isFalse()
        assertThat(dao.isRefreshTokenValid(testUserId, validToken)).isTrue()
    }

    @Test
    fun `cleanupExpiredTokens should handle all three conditions in one cleanup`() = runTest {
        val expiredToken = "expired-token"
        val invalidatedToken = "invalidated-token"
        val replacedToken = "replaced-token"
        val validToken = "valid-token"

        val pastExpiry = LocalDateTime.now().minusDays(1)
        val futureExpiry = LocalDateTime.now().plusDays(30)

        // Create all token types
        dao.saveRefreshToken(testUserId, expiredToken, pastExpiry)
        dao.saveRefreshToken(testUserId, invalidatedToken, futureExpiry)
        dao.saveRefreshToken(testUserId, replacedToken, futureExpiry)
        dao.saveRefreshToken(testUserId, validToken, futureExpiry)

        // Set up different invalid states
        dao.invalidateRefreshToken(testUserId, invalidatedToken)
        dao.replaceRefreshToken(testUserId, replacedToken, "new-token")

        // Verify all tokens exist before cleanup (replaceRefreshToken doesn't create new tokens)
        val tokensBeforeCleanup = RefreshTokensTable.selectAll().count()
        assertThat(tokensBeforeCleanup).isEqualTo(4) // 4 original (no new token created by replaceRefreshToken)

        // Run cleanup - should delete 3 tokens (expired, invalidated, replaced)
        dao.cleanupExpiredTokens()

        // Verify only valid token remains
        val tokensAfterCleanup = RefreshTokensTable.selectAll().count()
        assertThat(tokensAfterCleanup).isEqualTo(1) // only validToken

        // Verify invalid tokens are gone
        assertThat(dao.isRefreshTokenValid(testUserId, expiredToken)).isFalse()
        assertThat(dao.isRefreshTokenValid(testUserId, invalidatedToken)).isFalse()
        assertThat(dao.isRefreshTokenValid(testUserId, replacedToken)).isFalse()

        // Verify valid token still works
        assertThat(dao.isRefreshTokenValid(testUserId, validToken)).isTrue()
    }

    // </editor-fold>

    // <editor-fold desc="Token Rotation Security Tests">

    @Test
    fun `isRefreshTokenValid should reject rotated tokens`() = runTest {
        val oldToken = "old-refresh-token"
        val newToken = "new-refresh-token"
        val futureExpiry = LocalDateTime.now().plusDays(30)

        // Save and rotate token
        dao.saveRefreshToken(testUserId, oldToken, futureExpiry)
        dao.replaceRefreshToken(testUserId, oldToken, newToken)
        dao.saveRefreshToken(testUserId, newToken, futureExpiry)

        // Old token should be INVALID (even though not expired or invalidated)
        assertThat(dao.isRefreshTokenValid(testUserId, oldToken)).isFalse()

        // New token should be VALID
        assertThat(dao.isRefreshTokenValid(testUserId, newToken)).isTrue()
    }

    @Test
    fun `detectReplayAttack should return true for rotated tokens`() = runTest {
        val oldToken = "old-refresh-token"
        val newToken = "new-refresh-token"
        val futureExpiry = LocalDateTime.now().plusDays(30)

        // Save and rotate token
        dao.saveRefreshToken(testUserId, oldToken, futureExpiry)
        dao.replaceRefreshToken(testUserId, oldToken, newToken)

        // Replay attack should be detected
        assertThat(dao.detectReplayAttack(testUserId, oldToken)).isTrue()
    }

    @Test
    fun `detectReplayAttack should return false for active tokens`() = runTest {
        val activeToken = "active-token"
        val futureExpiry = LocalDateTime.now().plusDays(30)

        dao.saveRefreshToken(testUserId, activeToken, futureExpiry)

        // No replay attack for active tokens
        assertThat(dao.detectReplayAttack(testUserId, activeToken)).isFalse()
    }

    @Test
    fun `invalidateAllUserTokens should invalidate all tokens for a user`() = runTest {
        val token1 = "token-1"
        val token2 = "token-2"
        val token3 = "token-3"
        val futureExpiry = LocalDateTime.now().plusDays(30)

        dao.saveRefreshToken(testUserId, token1, futureExpiry)
        dao.saveRefreshToken(testUserId, token2, futureExpiry)
        dao.saveRefreshToken(testUserId, token3, futureExpiry)

        // Verify all valid before
        assertThat(dao.isRefreshTokenValid(testUserId, token1)).isTrue()
        assertThat(dao.isRefreshTokenValid(testUserId, token2)).isTrue()
        assertThat(dao.isRefreshTokenValid(testUserId, token3)).isTrue()

        // Invalidate all
        dao.invalidateAllUserTokens(testUserId)

        // Verify all invalid after
        assertThat(dao.isRefreshTokenValid(testUserId, token1)).isFalse()
        assertThat(dao.isRefreshTokenValid(testUserId, token2)).isFalse()
        assertThat(dao.isRefreshTokenValid(testUserId, token3)).isFalse()
    }

    // </editor-fold>

    // <editor-fold desc="Token Lifecycle Tests">

    @Test
    fun `saveRefreshToken should hash the token before storing`() = runTest {
        val plainToken = "my-secret-refresh-token"
        val futureExpiry = LocalDateTime.now().plusDays(30)

        dao.saveRefreshToken(testUserId, plainToken, futureExpiry)

        // Query database directly
        val storedTokenHash = RefreshTokensTable.selectAll()
            .where { RefreshTokensTable.userId eq testUserId }
            .single()[RefreshTokensTable.token]

        // Stored token should NOT be plaintext
        assertThat(storedTokenHash).isNotEqualTo(plainToken)

        // But validation should still work
        assertThat(dao.isRefreshTokenValid(testUserId, plainToken)).isTrue()
    }

    @Test
    fun `replaceRefreshToken should set replacedAt and replacedByToken`() = runTest {
        val oldToken = "old-token"
        val newToken = "new-token"
        val futureExpiry = LocalDateTime.now().plusDays(30)

        dao.saveRefreshToken(testUserId, oldToken, futureExpiry)
        dao.replaceRefreshToken(testUserId, oldToken, newToken)

        // Query the old token row
        val oldTokenRow = RefreshTokensTable.selectAll()
            .where { RefreshTokensTable.userId eq testUserId }
            .firstOrNull { row ->
                PasswordManager.validatePassword(oldToken, row[RefreshTokensTable.token])
            }

        assertThat(oldTokenRow).isNotNull
        assertThat(oldTokenRow!![RefreshTokensTable.replacedAt]).isNotNull()
        assertThat(oldTokenRow[RefreshTokensTable.replacedByToken]).isNotNull()
    }

    @Test
    fun `invalidateRefreshToken should mark token as invalidated`() = runTest {
        val token = "token-to-invalidate"
        val futureExpiry = LocalDateTime.now().plusDays(30)

        dao.saveRefreshToken(testUserId, token, futureExpiry)

        // Verify valid before
        assertThat(dao.isRefreshTokenValid(testUserId, token)).isTrue()

        // Invalidate
        dao.invalidateRefreshToken(testUserId, token)

        // Verify invalid after
        assertThat(dao.isRefreshTokenValid(testUserId, token)).isFalse()
    }

    @Test
    fun `isRefreshTokenValid should return false for expired tokens`() = runTest {
        val expiredToken = "expired-token"
        val pastExpiry = LocalDateTime.now().minusDays(1)

        dao.saveRefreshToken(testUserId, expiredToken, pastExpiry)

        // Expired token should be invalid
        assertThat(dao.isRefreshTokenValid(testUserId, expiredToken)).isFalse()
    }

    @Test
    fun `isRefreshTokenValid should return false for wrong user`() = runTest {
        val token = "user-token"
        val futureExpiry = LocalDateTime.now().plusDays(30)
        val differentUserId = UUID.randomUUID()

        dao.saveRefreshToken(testUserId, token, futureExpiry)

        // Token should be invalid for different user
        assertThat(dao.isRefreshTokenValid(differentUserId, token)).isFalse()
    }

    // </editor-fold>
}
