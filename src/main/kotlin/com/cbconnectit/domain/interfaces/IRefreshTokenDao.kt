package com.cbconnectit.domain.interfaces

import java.time.LocalDateTime
import java.util.*

interface IRefreshTokenDao {
    fun saveRefreshToken(userId: UUID, token: String, expiresAt: LocalDateTime)
    fun isRefreshTokenValid(userId: UUID, token: String): Boolean
    fun invalidateRefreshToken(userId: UUID, token: String)
    fun replaceRefreshToken(userId: UUID, oldToken: String, newToken: String)
    fun getReplacementToken(userId: UUID, oldToken: String): String?
    fun cleanupExpiredTokens()
}
