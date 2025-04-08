package com.cbconnectit.utils

import org.mindrot.jbcrypt.BCrypt
import java.security.SecureRandom
import kotlin.random.asKotlinRandom

private const val DEFAULT_PASSWORD_LENGTH = 6

object PasswordManager : PasswordManagerContract {
    private val letters = 'a'..'z'
    private val uppercaseLetters = 'A'..'Z'
    private val numbers = '0'..'9'
    private const val special: String = "@#=+!£\$%&?"

    override fun generatePasswordWithDefault() = generatePassword()

    /**
     * Generate a random password
     * @param isWithLetters Boolean value to specify if the password must contain letters
     * @param isWithUppercase Boolean value to specify if the password must contain uppercase letters
     * @param isWithNumbers Boolean value to specify if the password must contain numbers
     * @param isWithSpecial Boolean value to specify if the password must contain special chars
     * @param length Int value with the length of the password
     * @return the new password.
     */
    fun generatePassword(
        isWithLetters: Boolean = true,
        isWithUppercase: Boolean = true,
        isWithNumbers: Boolean = true,
        isWithSpecial: Boolean = true,
        length: Int = DEFAULT_PASSWORD_LENGTH
    ): String {
        if (length < 5) throw IllegalArgumentException("Length should at least be 5")

        val rnd = SecureRandom.getInstance("SHA1PRNG").asKotlinRandom()

        val chars = mutableListOf<Char>()
        val charsToUse = mutableListOf<Char>()

        if (isWithLetters) {
            chars += letters.toList()
            charsToUse += letters.random(rnd)
        }
        if (isWithUppercase) {
            chars += uppercaseLetters.toList()
            charsToUse += uppercaseLetters.random(rnd)
        }
        if (isWithNumbers) {
            chars += numbers.toList()
            charsToUse += numbers.random(rnd)
        }
        if (isWithSpecial) {
            chars += special.toList()
            charsToUse += special.random(rnd)
        }

        if (chars.isEmpty()) throw IllegalArgumentException("At least one character type must be selected")

        val passwordBeforeShuffle = charsToUse + List(length - charsToUse.size) { chars.random(rnd) }
        return passwordBeforeShuffle.shuffled(rnd).joinToString("")
    }

    override fun validatePassword(attempt: String, userPassword: String): Boolean {
        return BCrypt.checkpw(attempt, userPassword)
    }

    override fun encryptPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }
}

interface PasswordManagerContract {
    fun validatePassword(attempt: String, userPassword: String): Boolean
    fun encryptPassword(password: String): String
    fun generatePasswordWithDefault(): String
}
