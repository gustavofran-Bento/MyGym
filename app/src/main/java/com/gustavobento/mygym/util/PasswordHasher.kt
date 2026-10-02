package com.gustavobento.mygym.util

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Segurança da senha: o app NUNCA salva a senha digitada.
 *
 * Salvamos apenas um "hash" (resultado de uma função de mão única) gerado com
 * PBKDF2 + um "salt" aleatório por usuário. No login, geramos o hash da senha
 * digitada de novo e comparamos. Mesmo quem abrir o arquivo não descobre a senha.
 */
object PasswordHasher {

    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 10_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_BYTES = 16

    fun generateSalt(): String {
        val salt = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(salt)
        return toHex(salt)
    }

    fun hash(password: String, salt: String): String {
        val spec = PBEKeySpec(password.toCharArray(), fromHex(salt), ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val hash = factory.generateSecret(spec).encoded
        spec.clearPassword()
        return toHex(hash)
    }

    fun verify(password: String, salt: String, expectedHash: String): Boolean {
        return hash(password, salt) == expectedHash
    }

    private fun toHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun fromHex(hex: String): ByteArray {
        return ByteArray(hex.length / 2) { index ->
            hex.substring(index * 2, index * 2 + 2).toInt(16).toByte()
        }
    }
}
