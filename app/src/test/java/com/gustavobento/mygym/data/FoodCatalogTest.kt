package com.gustavobento.mygym.data

import com.gustavobento.mygym.data.mock.FoodCatalog
import com.gustavobento.mygym.model.MealType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FoodCatalogTest {

    @Test
    fun `ids do catalogo sao unicos`() {
        val ids = FoodCatalog.foods.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `macros de cada alimento sao validos`() {
        for (food in FoodCatalog.foods) {
            val m = food.per100g
            assertTrue("${food.name}: negativo", m.calories >= 0 && m.protein >= 0 && m.carbs >= 0 && m.fat >= 0)
            assertTrue("${food.name}: macros passam de 100 g", m.protein + m.carbs + m.fat <= 100.0)
            assertTrue("${food.name}: porção inválida", food.defaultGrams > 0)
        }
    }

    @Test
    fun `cada refeicao tem sugestoes e todas existem no catalogo`() {
        for (meal in MealType.entries) {
            assertTrue("${meal.label} sem sugestões", FoodCatalog.suggestionsFor(meal).isNotEmpty())
        }
        // Lanche começa pelos sucos
        assertEquals("Suco de laranja natural", FoodCatalog.suggestionsFor(MealType.SNACK).first().name)
    }
}
