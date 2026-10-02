package com.gustavobento.mygym.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidatorsTest {

    @Test
    fun `numero aceita virgula e ponto`() {
        assertEquals(22.5, Validators.parseNumber("22,5")!!, 0.001)
        assertEquals(22.5, Validators.parseNumber(" 22.5 ")!!, 0.001)
        assertNull(Validators.parseNumber(""))
        assertNull(Validators.parseNumber("abc"))
    }

    @Test
    fun `email`() {
        assertNull(Validators.validateEmail("ana@email.com"))
        assertNotNull(Validators.validateEmail(""))
        assertNotNull(Validators.validateEmail("ana@"))
        assertNotNull(Validators.validateEmail("ana.email.com"))
    }

    @Test
    fun `senha e confirmacao`() {
        assertNull(Validators.validatePassword("123456"))
        assertNotNull(Validators.validatePassword("12345"))
        assertNull(Validators.validatePasswordConfirmation("abc123", "abc123"))
        assertNotNull(Validators.validatePasswordConfirmation("abc123", "abc124"))
    }

    @Test
    fun `dados do corpo fora do limite sao recusados`() {
        assertNull(Validators.validateAge("22"))
        assertNotNull(Validators.validateAge("8"))
        assertNotNull(Validators.validateAge("22,5"))
        assertNull(Validators.validateWeight("62,5"))
        assertNotNull(Validators.validateWeight("500"))
        assertNotNull(Validators.validateHeight("50"))
    }

    @Test
    fun `quantidade em gramas`() {
        assertNull(Validators.validateGrams("150"))
        assertNotNull(Validators.validateGrams("0"))
        assertNotNull(Validators.validateGrams("-10"))
        assertNotNull(Validators.validateGrams("3000"))
    }

    @Test
    fun `macros de alimento novo`() {
        assertNull(Validators.validateMacroPer100g("0"))
        assertNotNull(Validators.validateMacroPer100g(""))
        assertNotNull(Validators.validateMacroPer100g("-1"))
        assertNotNull(Validators.validateMacroPer100g("120"))
        assertNull(Validators.validateMacroSum(33.0, 40.0, 12.0))
        assertNotNull(Validators.validateMacroSum(60.0, 50.0, 5.0))
    }
}
