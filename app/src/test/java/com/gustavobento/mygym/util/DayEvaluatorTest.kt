package com.gustavobento.mygym.util

import com.gustavobento.mygym.model.DayResult
import com.gustavobento.mygym.model.Macros
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DayEvaluatorTest {

    private val metas = Macros(calories = 3000.0, protein = 150.0, carbs = 400.0, fat = 80.0)

    private fun avaliar(kcal: Double, proteina: Double, copos: Int, treinoMarcado: Boolean = true, treinoFeito: Boolean = true) =
        DayEvaluator.evaluate(
            consumed = Macros(kcal, proteina, 0.0, 0.0),
            goals = metas,
            waterGlasses = copos,
            waterGoal = 8,
            workoutScheduled = treinoMarcado,
            workoutDone = treinoFeito,
        )

    @Test
    fun `bateu todas as metas`() {
        val dia = avaliar(kcal = 2950.0, proteina = 150.0, copos = 8)
        assertEquals(DayResult.ALL_GOALS_MET, dia.result)
        assertEquals(4, dia.metCount)
    }

    @Test
    fun `faltou so um pouco`() {
        // 80% das calorias, 80% da proteína, 6 copos e treino feito
        val dia = avaliar(kcal = 2400.0, proteina = 120.0, copos = 6)
        assertEquals(DayResult.ALMOST_THERE, dia.result)
        assertEquals(1, dia.metCount)
    }

    @Test
    fun `falta bastante`() {
        val dia = avaliar(kcal = 900.0, proteina = 40.0, copos = 2)
        assertEquals(DayResult.FAR_FROM_GOALS, dia.result)
    }

    @Test
    fun `comer demais nao bate a meta de calorias`() {
        val dia = avaliar(kcal = 3700.0, proteina = 160.0, copos = 8) // 123% das calorias
        val calorias = dia.checks.first { it.label == "Calorias" }
        assertFalse(calorias.isMet)
        assertFalse(calorias.isNear)
        assertEquals(DayResult.FAR_FROM_GOALS, dia.result)
    }

    @Test
    fun `dia de descanso conta como meta de treino cumprida`() {
        val dia = avaliar(kcal = 3000.0, proteina = 150.0, copos = 8, treinoMarcado = false, treinoFeito = false)
        assertTrue(dia.checks.first { it.label == "Treino" }.isMet)
        assertEquals(DayResult.ALL_GOALS_MET, dia.result)
    }

    @Test
    fun `treino marcado e nao feito impede o quase`() {
        val dia = avaliar(kcal = 3000.0, proteina = 150.0, copos = 8, treinoMarcado = true, treinoFeito = false)
        assertEquals(DayResult.FAR_FROM_GOALS, dia.result)
        assertEquals(3, dia.metCount)
    }
}
