package com.gustavobento.mygym.data.repository

import com.gustavobento.mygym.data.local.LocalStorage
import com.gustavobento.mygym.data.mock.FoodCatalog
import com.gustavobento.mygym.model.Food
import com.gustavobento.mygym.model.Macros
import com.gustavobento.mygym.model.MealType
import com.gustavobento.mygym.util.NutritionCalculator
import org.json.JSONArray
import org.json.JSONObject
import java.text.Normalizer
import java.util.UUID

/**
 * Alimentos disponíveis para o usuário: catálogo fixo (TACO) + alimentos
 * que ele mesmo cadastrou (salvos por usuário).
 */
class FoodRepository(private val storage: LocalStorage) {

    /** Alimentos do usuário primeiro, depois o catálogo. */
    fun allFoods(userId: String): List<Food> {
        return loadCustomFoods(userId) + FoodCatalog.foods
    }

    /** Busca ignorando maiúsculas e acentos ("feijao" encontra "Feijão"). */
    fun search(userId: String, query: String): List<Food> {
        val cleanQuery = normalize(query)
        if (cleanQuery.isEmpty()) return allFoods(userId)
        return allFoods(userId).filter { normalize(it.name).contains(cleanQuery) }
    }

    /**
     * Lista inicial (busca vazia) de uma refeição:
     * alimentos do usuário → sugestões da refeição → resto do catálogo.
     */
    fun foodsForMeal(userId: String, mealType: MealType): List<Food> {
        val custom = loadCustomFoods(userId)
        val suggested = FoodCatalog.suggestionsFor(mealType)
        val others = FoodCatalog.foods.filter { it !in suggested }
        return custom + suggested + others
    }

    fun findById(userId: String, foodId: String): Food? {
        return allFoods(userId).find { it.id == foodId }
    }

    /**
     * Cadastra um alimento novo. As calorias são CALCULADAS automaticamente
     * a partir dos macros informados (P×4 + C×4 + G×9).
     */
    fun addCustomFood(userId: String, name: String, protein: Double, carbs: Double, fat: Double, defaultGrams: Double): Food {
        val calories = NutritionCalculator.caloriesFromMacros(protein, carbs, fat)
        val food = Food(
            id = "custom_" + UUID.randomUUID().toString(),
            name = name.trim(),
            per100g = Macros(calories, protein, carbs, fat),
            defaultGrams = defaultGrams,
            isCustom = true,
        )
        val foods = loadCustomFoods(userId).toMutableList()
        foods.add(0, food)
        saveCustomFoods(userId, foods)
        return food
    }

    private fun normalize(text: String): String {
        val withoutAccents = Normalizer.normalize(text.trim().lowercase(), Normalizer.Form.NFD)
        return withoutAccents.replace(Regex("\\p{M}"), "")
    }

    // ---------- JSON ----------

    private fun key(userId: String) = "alimentos_$userId"

    private fun loadCustomFoods(userId: String): List<Food> {
        val json = storage.readString(key(userId)) ?: return emptyList()
        val array = JSONArray(json)
        val foods = mutableListOf<Food>()
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            foods.add(
                Food(
                    id = item.getString("id"),
                    name = item.getString("name"),
                    per100g = Macros(
                        calories = item.getDouble("calories"),
                        protein = item.getDouble("protein"),
                        carbs = item.getDouble("carbs"),
                        fat = item.getDouble("fat"),
                    ),
                    defaultGrams = item.getDouble("defaultGrams"),
                    isCustom = true,
                ),
            )
        }
        return foods
    }

    private fun saveCustomFoods(userId: String, foods: List<Food>) {
        val array = JSONArray()
        for (food in foods) {
            val item = JSONObject()
            item.put("id", food.id)
            item.put("name", food.name)
            item.put("calories", food.per100g.calories)
            item.put("protein", food.per100g.protein)
            item.put("carbs", food.per100g.carbs)
            item.put("fat", food.per100g.fat)
            item.put("defaultGrams", food.defaultGrams)
            array.put(item)
        }
        storage.saveString(key(userId), array.toString())
    }
}
