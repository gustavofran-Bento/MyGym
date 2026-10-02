package com.gustavobento.mygym.data.mock

import com.gustavobento.mygym.data.local.LocalStorage
import com.gustavobento.mygym.data.repository.AuthRepository
import com.gustavobento.mygym.data.repository.DiaryRepository
import com.gustavobento.mygym.data.repository.WorkoutRepository
import com.gustavobento.mygym.model.DiaryEntry
import com.gustavobento.mygym.model.Goal
import com.gustavobento.mygym.model.MealType
import com.gustavobento.mygym.model.Sex
import com.gustavobento.mygym.model.WorkoutLog
import com.gustavobento.mygym.util.NutritionCalculator
import java.time.LocalDate
import java.util.UUID

/**
 * Cria, apenas na primeira vez que o app é aberto, uma conta de demonstração
 * com refeições dos últimos dias. Assim o gráfico semanal já aparece com dados.
 *
 * Conta demo → e-mail: gustavo@mygym.com | senha: mygym123
 */
object DemoData {

    const val DEMO_EMAIL = "gustavo@mygym.com"
    const val DEMO_PASSWORD = "mygym123"
    private const val KEY_CREATED = "demo_criado"

    fun createIfNeeded(
        storage: LocalStorage,
        authRepository: AuthRepository,
        diaryRepository: DiaryRepository,
        workoutRepository: WorkoutRepository,
    ) {
        if (storage.readBoolean(KEY_CREATED)) return

        val user = authRepository.register(
            name = "Gustavo Francisco Bento",
            email = DEMO_EMAIL,
            password = DEMO_PASSWORD,
            sex = Sex.MALE,
            age = 22,
            weightKg = 78.0,
            heightCm = 178.0,
            goal = Goal.GAIN_MUSCLE,
            startSession = false,
        )
        if (user != null) {
            val today = LocalDate.now()
            val entries = mutableListOf<DiaryEntry>()
            // 6 dias anteriores com quantidades diferentes para o gráfico variar
            val dayFactors = listOf(1.15, 1.3, 0.75, 1.2, 1.55, 1.05)
            for (daysAgo in 1..6) {
                val date = today.minusDays(daysAgo.toLong())
                val factor = dayFactors[daysAgo - 1]
                entries.addAll(fullDay(date, factor, skipSnack = daysAgo == 3))
                val workout = workoutRepository.workoutFor(user.id, date)
                if (workout != null && daysAgo % 2 == 1) {
                    workoutRepository.saveLog(user.id, WorkoutLog(date, workout.id, 6, 6))
                }
            }
            // Hoje: só café da manhã e almoço (o resto o usuário adiciona)
            entries.addAll(meal(today, MealType.BREAKFAST, listOf(9 to 50.0, 22 to 100.0, 64 to 100.0, 40 to 90.0)))
            entries.addAll(meal(today, MealType.LUNCH, listOf(1 to 150.0, 3 to 100.0, 20 to 150.0, 51 to 40.0, 52 to 80.0)))

            diaryRepository.addEntries(user.id, entries)
            diaryRepository.setWaterGlasses(user.id, today, 3)
        }
        storage.saveBoolean(KEY_CREATED, true)
    }

    private fun fullDay(date: LocalDate, factor: Double, skipSnack: Boolean): List<DiaryEntry> {
        val entries = mutableListOf<DiaryEntry>()
        entries.addAll(meal(date, MealType.BREAKFAST, listOf(9 to 100.0 * factor, 22 to 100.0, 30 to 200.0, 40 to 90.0)))
        entries.addAll(meal(date, MealType.LUNCH, listOf(1 to 250.0 * factor, 3 to 150.0, 20 to 180.0 * factor, 62 to 10.0, 51 to 40.0)))
        if (!skipSnack) {
            entries.addAll(meal(date, MealType.SNACK, listOf(27 to 30.0, 12 to 50.0 * factor, 40 to 90.0, 60 to 20.0)))
        }
        entries.addAll(meal(date, MealType.DINNER, listOf(5 to 250.0 * factor, 24 to 180.0, 50 to 80.0, 62 to 10.0)))
        return entries
    }

    /** Cria os registros de uma refeição a partir de pares (id do alimento no catálogo, gramas). */
    private fun meal(date: LocalDate, mealType: MealType, items: List<Pair<Int, Double>>): List<DiaryEntry> {
        val entries = mutableListOf<DiaryEntry>()
        for (item in items) {
            val food = FoodCatalog.foods.find { it.id == "taco_${item.first}" } ?: continue
            val grams = Math.round(item.second).toDouble()
            entries.add(
                DiaryEntry(
                    id = UUID.randomUUID().toString(),
                    date = date,
                    mealType = mealType,
                    foodName = food.name,
                    grams = grams,
                    macros = NutritionCalculator.macrosForPortion(food, grams),
                ),
            )
        }
        return entries
    }
}
