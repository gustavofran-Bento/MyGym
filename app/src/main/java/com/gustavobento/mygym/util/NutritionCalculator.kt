package com.gustavobento.mygym.util

import com.gustavobento.mygym.model.DiaryEntry
import com.gustavobento.mygym.model.Food
import com.gustavobento.mygym.model.Goal
import com.gustavobento.mygym.model.Macros
import com.gustavobento.mygym.model.Sex
import com.gustavobento.mygym.model.User
import kotlin.math.roundToInt

/**
 * Todas as contas de nutrição do app ficam aqui (regra de negócio).
 * Não depende de nada do Android, por isso é fácil de testar com JUnit.
 */
object NutritionCalculator {

    // Valores de energia de cada macronutriente (padrão usado nos rótulos nutricionais)
    const val KCAL_PER_GRAM_PROTEIN = 4.0
    const val KCAL_PER_GRAM_CARBS = 4.0
    const val KCAL_PER_GRAM_FAT = 9.0

    /** Fator de atividade para quem treina 3 a 5 vezes por semana. */
    private const val ACTIVITY_FACTOR = 1.55

    /** Proteína recomendada para quem treina: 2 g por kg de peso corporal. */
    private const val PROTEIN_PER_KG = 2.0

    /** 25% das calorias do dia vêm de gordura. */
    private const val FAT_PERCENTAGE = 0.25

    /** Calcula as calorias a partir dos macros: proteína×4 + carboidrato×4 + gordura×9. */
    fun caloriesFromMacros(protein: Double, carbs: Double, fat: Double): Double {
        return protein * KCAL_PER_GRAM_PROTEIN + carbs * KCAL_PER_GRAM_CARBS + fat * KCAL_PER_GRAM_FAT
    }

    /** Macros de uma porção. Ex.: arroz (100 g) com 150 g → multiplica por 1,5. */
    fun macrosForPortion(food: Food, grams: Double): Macros {
        if (grams <= 0.0) return Macros.ZERO
        return food.per100g * (grams / 100.0)
    }

    /** Soma os macros de vários registros do diário. */
    fun total(entries: List<DiaryEntry>): Macros {
        var total = Macros.ZERO
        for (entry in entries) {
            total += entry.macros
        }
        return total
    }

    /**
     * Taxa metabólica basal (gasto em repouso) pela fórmula de Mifflin-St Jeor:
     * homens:   10×peso + 6,25×altura − 5×idade + 5
     * mulheres: 10×peso + 6,25×altura − 5×idade − 161
     */
    fun basalMetabolicRate(sex: Sex, age: Int, weightKg: Double, heightCm: Double): Double {
        val base = 10 * weightKg + 6.25 * heightCm - 5 * age
        return if (sex == Sex.MALE) base + 5 else base - 161
    }

    /**
     * Metas diárias:
     * 1. calorias = basal × fator de atividade + ajuste do objetivo;
     * 2. proteína = 2 g por kg;
     * 3. gordura = 25% das calorias;
     * 4. carboidrato = o que sobra das calorias.
     */
    fun dailyGoals(sex: Sex, age: Int, weightKg: Double, heightCm: Double, goal: Goal): Macros {
        val maintenance = basalMetabolicRate(sex, age, weightKg, heightCm) * ACTIVITY_FACTOR
        val calories = (maintenance + goal.calorieAdjustment).coerceAtLeast(1200.0)

        val protein = weightKg * PROTEIN_PER_KG
        val fat = calories * FAT_PERCENTAGE / KCAL_PER_GRAM_FAT
        val caloriesLeft = calories - protein * KCAL_PER_GRAM_PROTEIN - fat * KCAL_PER_GRAM_FAT
        val carbs = (caloriesLeft / KCAL_PER_GRAM_CARBS).coerceAtLeast(0.0)

        // Arredonda para números "redondos", mais fáceis de acompanhar
        return Macros(
            calories = roundTo(calories, 10),
            protein = roundTo(protein, 5),
            carbs = roundTo(carbs, 5),
            fat = roundTo(fat, 5),
        )
    }

    fun dailyGoals(user: User): Macros {
        return dailyGoals(user.sex, user.age, user.weightKg, user.heightCm, user.goal)
    }

    /** IMC = peso / altura² (altura em metros). */
    fun bmi(weightKg: Double, heightCm: Double): Double {
        if (heightCm <= 0.0) return 0.0
        val heightM = heightCm / 100.0
        return weightKg / (heightM * heightM)
    }

    fun bmiCategory(bmi: Double): String {
        return when {
            bmi <= 0.0 -> "—"
            bmi < 18.5 -> "Abaixo do peso"
            bmi < 25.0 -> "Peso normal"
            bmi < 30.0 -> "Sobrepeso"
            else -> "Obesidade"
        }
    }

    /** Percentual de 0 a 100 do consumido em relação à meta (sem passar de 100). */
    fun percent(consumed: Double, goal: Double): Int {
        if (goal <= 0.0) return 0
        return ((consumed / goal) * 100).roundToInt().coerceIn(0, 100)
    }

    private fun roundTo(value: Double, step: Int): Double {
        return ((value / step).roundToInt() * step).toDouble()
    }
}
