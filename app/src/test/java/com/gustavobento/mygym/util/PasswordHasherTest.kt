package com.gustavobento.mygym.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {

    @Test
    fun `senha correta e aceita e errada e recusada`() {
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hash("minhaSenha1", salt)
        assertTrue(PasswordHasher.verify("minhaSenha1", salt, hash))
        assertFalse(PasswordHasher.verify("minhaSenha2", salt, hash))
    }

    @Test
    fun `hash nunca contem a senha original`() {
        val hash = PasswordHasher.hash("minhaSenha1", PasswordHasher.generateSalt())
        assertFalse(hash.contains("minhaSenha1"))
    }

    @Test
    fun `mesma senha com salts diferentes gera hashes diferentes`() {
        val hash1 = PasswordHasher.hash("123456", PasswordHasher.generateSalt())
        val hash2 = PasswordHasher.hash("123456", PasswordHasher.generateSalt())
        assertNotEquals(hash1, hash2)
    }
}
