package com.gustavobento.mygym.model

import java.time.LocalDate

/**
 * Um alimento registrado no diário (ex.: 150 g de arroz no almoço de hoje).
 *
 * Os macros são guardados já calculados para a quantidade consumida. Assim,
 * o histórico não muda se o alimento for editado depois.
 */
data class DiaryEntry(
    val id: String,
    val date: LocalDate,
    val mealType: MealType,
    val foodName: String,
    val grams: Double,
    val macros: Macros,
)
