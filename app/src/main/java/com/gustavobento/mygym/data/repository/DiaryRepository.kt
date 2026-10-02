package com.gustavobento.mygym.data.repository

import com.gustavobento.mygym.data.local.LocalStorage
import com.gustavobento.mygym.model.DayClosing
import com.gustavobento.mygym.model.DayResult
import com.gustavobento.mygym.model.DiaryEntry
import com.gustavobento.mygym.model.Food
import com.gustavobento.mygym.model.Macros
import com.gustavobento.mygym.model.MealType
import com.gustavobento.mygym.util.NutritionCalculator
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

/** Diário alimentar (o que foi comido em cada refeição) e consumo de água. */
class DiaryRepository(private val storage: LocalStorage) {

    fun entriesFor(userId: String, date: LocalDate): List<DiaryEntry> {
        return loadEntries(userId).filter { it.date == date }
    }

    /** Registros entre duas datas (inclusive). Usado no resumo semanal. */
    fun entriesBetween(userId: String, start: LocalDate, end: LocalDate): List<DiaryEntry> {
        return loadEntries(userId).filter { !it.date.isBefore(start) && !it.date.isAfter(end) }
    }

    /** Registra uma porção de alimento e já calcula os macros da quantidade. */
    fun addFood(userId: String, date: LocalDate, mealType: MealType, food: Food, grams: Double): DiaryEntry {
        val entry = DiaryEntry(
            id = UUID.randomUUID().toString(),
            date = date,
            mealType = mealType,
            foodName = food.name,
            grams = grams,
            macros = NutritionCalculator.macrosForPortion(food, grams),
        )
        addEntries(userId, listOf(entry))
        return entry
    }

    fun addEntries(userId: String, newEntries: List<DiaryEntry>) {
        val entries = loadEntries(userId).toMutableList()
        entries.addAll(newEntries)
        saveEntries(userId, entries)
    }

    fun removeEntry(userId: String, entryId: String) {
        val entries = loadEntries(userId).filter { it.id != entryId }
        saveEntries(userId, entries)
    }

    // ---------- Água (copos de 250 ml por dia) ----------

    fun waterGlasses(userId: String, date: LocalDate): Int {
        val json = storage.readString(waterKey(userId)) ?: return 0
        return JSONObject(json).optInt(date.toString(), 0)
    }

    fun setWaterGlasses(userId: String, date: LocalDate, glasses: Int) {
        val json = storage.readString(waterKey(userId))
        val water = if (json == null) JSONObject() else JSONObject(json)
        water.put(date.toString(), glasses.coerceIn(0, MAX_WATER_GLASSES))
        storage.saveString(waterKey(userId), water.toString())
    }

    // ---------- Fechamento do dia ----------

    /** Salva o fechamento do dia. Se o dia já tinha sido finalizado, substitui. */
    fun saveClosing(userId: String, closing: DayClosing) {
        val json = storage.readString(closingsKey(userId))
        val closings = if (json == null) JSONObject() else JSONObject(json)

        val item = JSONObject()
        item.put("finishedAt", closing.finishedAt.toString())
        item.put("calories", closing.consumed.calories)
        item.put("protein", closing.consumed.protein)
        item.put("carbs", closing.consumed.carbs)
        item.put("fat", closing.consumed.fat)
        item.put("goalCalories", closing.goals.calories)
        item.put("goalProtein", closing.goals.protein)
        item.put("goalCarbs", closing.goals.carbs)
        item.put("goalFat", closing.goals.fat)
        item.put("water", closing.waterGlasses)
        item.put("workoutDone", closing.workoutDone)
        item.put("goalsMet", closing.goalsMet)
        item.put("totalGoals", closing.totalGoals)
        item.put("result", closing.result.name)

        closings.put(closing.date.toString(), item)
        storage.saveString(closingsKey(userId), closings.toString())
    }

    /** Fechamento do dia, ou null se o dia ainda não foi finalizado. */
    fun closingFor(userId: String, date: LocalDate): DayClosing? {
        val json = storage.readString(closingsKey(userId)) ?: return null
        val item = JSONObject(json).optJSONObject(date.toString()) ?: return null
        return DayClosing(
            date = date,
            finishedAt = LocalTime.parse(item.getString("finishedAt")),
            consumed = Macros(item.getDouble("calories"), item.getDouble("protein"), item.getDouble("carbs"), item.getDouble("fat")),
            goals = Macros(item.getDouble("goalCalories"), item.getDouble("goalProtein"), item.getDouble("goalCarbs"), item.getDouble("goalFat")),
            waterGlasses = item.getInt("water"),
            workoutDone = item.getBoolean("workoutDone"),
            goalsMet = item.getInt("goalsMet"),
            totalGoals = item.getInt("totalGoals"),
            result = DayResult.valueOf(item.getString("result")),
        )
    }

    // ---------- JSON ----------

    private fun entriesKey(userId: String) = "diario_$userId"
    private fun waterKey(userId: String) = "agua_$userId"
    private fun closingsKey(userId: String) = "fechamentos_$userId"

    private fun loadEntries(userId: String): List<DiaryEntry> {
        val json = storage.readString(entriesKey(userId)) ?: return emptyList()
        val array = JSONArray(json)
        val entries = mutableListOf<DiaryEntry>()
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            entries.add(
                DiaryEntry(
                    id = item.getString("id"),
                    date = LocalDate.parse(item.getString("date")),
                    mealType = MealType.valueOf(item.getString("mealType")),
                    foodName = item.getString("foodName"),
                    grams = item.getDouble("grams"),
                    macros = Macros(
                        calories = item.getDouble("calories"),
                        protein = item.getDouble("protein"),
                        carbs = item.getDouble("carbs"),
                        fat = item.getDouble("fat"),
                    ),
                ),
            )
        }
        return entries
    }

    private fun saveEntries(userId: String, entries: List<DiaryEntry>) {
        val array = JSONArray()
        for (entry in entries) {
            val item = JSONObject()
            item.put("id", entry.id)
            item.put("date", entry.date.toString())
            item.put("mealType", entry.mealType.name)
            item.put("foodName", entry.foodName)
            item.put("grams", entry.grams)
            item.put("calories", entry.macros.calories)
            item.put("protein", entry.macros.protein)
            item.put("carbs", entry.macros.carbs)
            item.put("fat", entry.macros.fat)
            array.put(item)
        }
        storage.saveString(entriesKey(userId), array.toString())
    }

    companion object {
        const val MAX_WATER_GLASSES = 20
        const val WATER_GOAL_GLASSES = 8
        const val ML_PER_GLASS = 250
    }
}
