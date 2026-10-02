package com.gustavobento.mygym.model

/**
 * Calorias e macronutrientes. É usado para:
 * - o que um alimento tem em 100 g;
 * - o que foi consumido em uma refeição ou no dia;
 * - as metas diárias do usuário.
 */
data class Macros(
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
) {
    operator fun plus(other: Macros): Macros {
        return Macros(
            calories = calories + other.calories,
            protein = protein + other.protein,
            carbs = carbs + other.carbs,
            fat = fat + other.fat,
        )
    }

    /** Multiplica todos os valores. Ex.: macros de 100 g × 1,5 = macros de 150 g. */
    operator fun times(factor: Double): Macros {
        return Macros(
            calories = calories * factor,
            protein = protein * factor,
            carbs = carbs * factor,
            fat = fat * factor,
        )
    }

    companion object {
        val ZERO = Macros(0.0, 0.0, 0.0, 0.0)
    }
}
