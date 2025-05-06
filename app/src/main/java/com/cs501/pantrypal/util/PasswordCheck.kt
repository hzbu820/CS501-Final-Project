package com.cs501.pantrypal.util

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class PasswordCheck {
    companion object {
        private const val ITERATIONS = 65536
        private const val KEY_LENGTH = 256
        private const val SALT_LENGTH = 16
    }

    fun validatePassword(password: String): Boolean {
        return password.length >= 8 && password.any { it.isDigit() } && password.any { it.isLetter() }
    }

    fun hashPassword(password: String): String {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)

        val spec = PBEKeySpec(
            password.toCharArray(), salt, ITERATIONS, KEY_LENGTH
        )

        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded

        val saltBase64 = Base64.getEncoder().encodeToString(salt)
        val hashBase64 = Base64.getEncoder().encodeToString(hash)

        return "$saltBase64:$hashBase64"
    }


    fun verifyPassword(password: String, storedHash: String): Boolean {
        val parts = storedHash.split(":")
        if (parts.size != 2) return false

        val salt = Base64.getDecoder().decode(parts[0])
        val hash = Base64.getDecoder().decode(parts[1])

        val spec = PBEKeySpec(
            password.toCharArray(), salt, ITERATIONS, KEY_LENGTH
        )

        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val calculatedHash = factory.generateSecret(spec).encoded

        return hash.contentEquals(calculatedHash)
    }
}