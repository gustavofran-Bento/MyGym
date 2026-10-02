package com.gustavobento.mygym.util

import com.gustavobento.mygym.model.DiaryEntry
import com.gustavobento.mygym.model.Food
import com.gustavobento.mygym.model.Goal
import com.gustavobento.mygym.model.Macros
import com.gustavobento.mygym.model.MealType
import com.gustavobento.mygym.model.Sex
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class NutritionCalculatorTest {

    private val frango = Food("1", "Frango", Macros(159.0, 32.0, 0.0, 2.5))

    @Test
    fun `calorias sao calculadas pelos macros`() {
        // 33×4 + 40×4 + 12×9 = 132 + 160 + 108 = 400
        assertEquals(400.0, NutritionCalculator.caloriesFromMacros(33.0, 40.0, 12.0), 0.01)
    }

    @Test
    fun `porcao multiplica os valores de 100 g`() {
        val porcao = NutritionCalculator.macrosForPortion(frango, 150.0)
        assertEquals(238.5, porcao.calories, 0.01)
        assertEquals(48.0, porcao.protein, 0.01)
        assertEquals(3.75, porcao.fat, 0.01)
    }

    @Test
    fun `porcao zero ou negativa nao gera valores`() {
        assertEquals(Macros.ZERO, NutritionCalculator.macrosForPortion(frango, 0.0))
        assertEquals(Macros.ZERO, NutritionCalculator.macrosForPortion(frango, -50.0))
    }

    @Test
    fun `total soma todos os registros do dia`() {
        val hoje = LocalDate.of(2026, 9, 27)
        val registros = listOf(
            DiaryEntry("a", hoje, MealType.LUNCH, "Arroz", 100.0, Macros(128.0, 2.5, 28.1, 0.2)),
            DiaryEntry("b", hoje, MealType.LUNCH, "Frango", 100.0, Macros(159.0, 32.0, 0.0, 2.5)),
        )
        val total = NutritionCalculator.total(registros)
        assertEquals(287.0, total.calories, 0.01)
        assertEquals(34.5, total.protein, 0.01)
    }

    @Test
    fun `taxa basal pela formula de Mifflin-St Jeor`() {
        // Homem, 22 anos, 78 kg, 178 cm: 780 + 1112,5 - 110 + 5 = 1787,5
        assertEquals(1787.5, NutritionCalculator.basalMetabolicRate(Sex.MALE, 22, 78.0, 178.0), 0.01)
        // Mulher, 25 anos, 62,5 kg, 165 cm: 625 + 1031,25 - 125 - 161 = 1370,25
        assertEquals(1370.25, NutritionCalculator.basalMetabolicRate(Sex.FEMALE, 25, 62.5, 165.0), 0.01)
    }

    @Test
    fun `metas diarias para ganhar massa`() {
        val metas = NutritionCalculator.dailyGoals(Sex.MALE, 22, 78.0, 178.0, Goal.GAIN_MUSCLE)
        assertEquals(3070.0, metas.calories, 0.01)
        assertEquals(155.0, metas.protein, 0.01)
        assertEquals(85.0, metas.fat, 0.01)
        assertEquals(420.0, metas.carbs, 0.01)
    }

    @Test
    fun `perder peso tem menos calorias que manter`() {
        val manter = NutritionCalculator.dailyGoals(Sex.FEMALE, 25, 62.5, 165.0, Goal.MAINTAIN)
        val perder = NutritionCalculator.dailyGoals(Sex.FEMALE, 25, 62.5, 165.0, Goal.LOSE_WEIGHT)
        assertEquals(1620.0, perder.calories, 0.01)
        assertEquals(500.0, manter.calories - perder.calories, 10.0)
    }

    @Test
    fun `imc e categoria`() {
        val imc = NutritionCalculator.bmi(78.0, 178.0)
        assertEquals(24.6, imc, 0.05)
        assertEquals("Peso normal", NutritionCalculator.bmiCategory(imc))
        assertEquals("Abaixo do peso", NutritionCalculator.bmiCategory(17.0))
        assertEquals("Obesidade", NutritionCalculator.bmiCategory(32.0))
    }

    @Test
    fun `percentual limitado entre 0 e 100 e sem divisao por zero`() {
        assertEquals(50, NutritionCalculator.percent(50.0, 100.0))
        assertEquals(100, NutritionCalculator.percent(150.0, 100.0))
        assertEquals(0, NutritionCalculator.percent(50.0, 0.0))
    }
}
