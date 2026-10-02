package com.gustavobento.mygym.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelsTest {

    @Test
    fun `volume do treino ignora peso corporal`() {
        val treino = Workout(
            1, "A", "Teste", "Teste",
            listOf(
                Exercise(1, "Supino", 4, 10, 30.0),
                Exercise(2, "Mergulho", 3, 12, 0.0),
            ),
        )
        assertEquals(1200.0, treino.totalVolumeKg, 0.01)
        assertEquals(7, treino.totalSets)
        assertEquals(21, treino.estimatedMinutes) // 7 séries × 3 min
        assertTrue(treino.exercises[1].isBodyweight)
    }

    @Test
    fun `treino novo sem exercicios esta vazio`() {
        val vazio = Workout(4, "D", "Ombros", "Personalizado", emptyList())
        assertTrue(vazio.isEmpty)
        assertEquals(0, vazio.estimatedMinutes)
    }

    @Test
    fun `nome e iniciais do usuario`() {
        val user = User("1", "  Gustavo Francisco Bento ", "g@g.com", Sex.MALE, 22, 78.0, 178.0, Goal.MAINTAIN)
        assertEquals("Gustavo", user.firstName)
        assertEquals("GB", user.initials)
    }

    @Test
    fun `macros somam e multiplicam`() {
        val soma = Macros(100.0, 10.0, 5.0, 2.0) + Macros(50.0, 5.0, 5.0, 1.0)
        assertEquals(Macros(150.0, 15.0, 10.0, 3.0), soma)
        assertEquals(Macros(50.0, 5.0, 2.5, 1.0), Macros(100.0, 10.0, 5.0, 2.0) * 0.5)
    }
}
