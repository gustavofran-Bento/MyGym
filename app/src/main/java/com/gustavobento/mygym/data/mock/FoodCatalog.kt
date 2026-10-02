package com.gustavobento.mygym.data.mock

import com.gustavobento.mygym.model.Food
import com.gustavobento.mygym.model.Macros
import com.gustavobento.mygym.model.MealType

/**
 * Catálogo de alimentos comuns no Brasil.
 * Valores aproximados por 100 g (ou 100 ml nas bebidas), baseados na Tabela TACO (Unicamp).
 */
object FoodCatalog {

    private fun food(id: Int, name: String, kcal: Double, protein: Double, carbs: Double, fat: Double, portion: Double = 100.0): Food {
        return Food(
            id = "taco_$id",
            name = name,
            per100g = Macros(kcal, protein, carbs, fat),
            defaultGrams = portion,
        )
    }

    val foods: List<Food> = listOf(
        // Carboidratos
        food(1, "Arroz branco cozido", 128.0, 2.5, 28.1, 0.2, portion = 150.0),
        food(2, "Arroz integral cozido", 124.0, 2.6, 25.8, 1.0, portion = 150.0),
        food(3, "Feijão carioca cozido", 76.0, 4.8, 13.6, 0.5, portion = 100.0),
        food(4, "Feijão preto cozido", 77.0, 4.5, 14.0, 0.5, portion = 100.0),
        food(5, "Batata-doce cozida", 77.0, 0.6, 18.4, 0.1, portion = 150.0),
        food(6, "Batata inglesa cozida", 52.0, 1.2, 11.9, 0.0, portion = 150.0),
        food(7, "Mandioca cozida", 125.0, 0.6, 30.1, 0.3, portion = 100.0),
        food(8, "Macarrão cozido", 102.0, 3.4, 19.9, 1.2, portion = 150.0),
        food(9, "Pão francês", 300.0, 8.0, 58.6, 3.1, portion = 50.0),
        food(10, "Pão integral", 253.0, 9.4, 49.9, 3.7, portion = 50.0),
        food(11, "Tapioca", 240.0, 0.1, 59.0, 0.1, portion = 60.0),
        food(12, "Aveia em flocos", 394.0, 13.9, 66.6, 8.5, portion = 30.0),
        food(13, "Pão de queijo", 363.0, 5.1, 34.2, 24.6, portion = 40.0),
        // Proteínas
        food(20, "Peito de frango grelhado", 159.0, 32.0, 0.0, 2.5, portion = 120.0),
        food(21, "Patinho bovino grelhado", 219.0, 35.9, 0.0, 7.3, portion = 120.0),
        food(22, "Ovo cozido", 146.0, 13.3, 0.6, 9.5, portion = 50.0),
        food(23, "Clara de ovo cozida", 52.0, 11.0, 0.7, 0.2, portion = 60.0),
        food(24, "Tilápia grelhada", 128.0, 26.0, 0.0, 2.7, portion = 120.0),
        food(25, "Salmão grelhado", 208.0, 20.0, 0.0, 13.0, portion = 120.0),
        food(26, "Atum em conserva (água)", 118.0, 26.0, 0.0, 1.0, portion = 60.0),
        food(27, "Whey protein", 400.0, 80.0, 8.0, 5.0, portion = 30.0),
        // Laticínios
        food(30, "Leite integral", 61.0, 2.9, 4.3, 3.2, portion = 200.0),
        food(31, "Leite desnatado", 35.0, 3.4, 4.9, 0.2, portion = 200.0),
        food(32, "Iogurte natural", 51.0, 4.1, 1.9, 3.0, portion = 170.0),
        food(33, "Queijo minas frescal", 264.0, 17.4, 3.2, 20.2, portion = 30.0),
        food(34, "Requeijão", 257.0, 9.6, 2.4, 23.4, portion = 20.0),
        // Frutas
        food(40, "Banana prata", 98.0, 1.3, 26.0, 0.1, portion = 90.0),
        food(41, "Maçã", 56.0, 0.3, 15.2, 0.0, portion = 130.0),
        food(42, "Mamão papaia", 40.0, 0.5, 10.4, 0.1, portion = 150.0),
        food(43, "Morango", 30.0, 0.9, 6.8, 0.3, portion = 100.0),
        food(44, "Abacate", 96.0, 1.2, 6.0, 8.4, portion = 100.0),
        // Verduras e legumes
        food(50, "Brócolis cozido", 25.0, 2.1, 4.4, 0.5, portion = 80.0),
        food(51, "Alface", 11.0, 1.3, 1.7, 0.2, portion = 40.0),
        food(52, "Tomate", 15.0, 1.1, 3.1, 0.2, portion = 80.0),
        food(53, "Cenoura crua", 34.0, 1.3, 7.7, 0.2, portion = 50.0),
        // Gorduras e outros
        food(60, "Pasta de amendoim", 588.0, 25.0, 20.0, 50.0, portion = 15.0),
        food(61, "Castanha-do-pará", 643.0, 14.5, 15.1, 63.5, portion = 10.0),
        food(62, "Azeite de oliva", 884.0, 0.0, 0.0, 100.0, portion = 10.0),
        food(63, "Chocolate ao leite", 540.0, 7.2, 59.6, 30.3, portion = 25.0),
        food(64, "Café coado (sem açúcar)", 9.0, 0.7, 1.5, 0.1, portion = 100.0),
        food(65, "Açúcar refinado", 387.0, 0.0, 99.5, 0.0, portion = 5.0),
        // Bebidas (valores por 100 ml)
        food(70, "Suco de laranja natural", 33.0, 0.7, 7.6, 0.1, portion = 250.0),
        food(71, "Suco de uva integral", 60.0, 0.2, 15.0, 0.0, portion = 200.0),
        food(72, "Suco de maracujá (sem açúcar)", 39.0, 0.8, 9.6, 0.2, portion = 250.0),
        food(73, "Vitamina de banana com leite", 80.0, 2.7, 12.0, 2.3, portion = 300.0),
        food(74, "Água de coco", 22.0, 0.0, 5.3, 0.0, portion = 300.0),
        food(75, "Café com leite", 47.0, 2.5, 4.5, 2.2, portion = 200.0),
        food(76, "Achocolatado com leite", 83.0, 3.0, 12.0, 2.8, portion = 200.0),
        // Lanches
        food(80, "Granola", 421.0, 10.0, 66.0, 13.0, portion = 40.0),
        food(81, "Bolo simples", 311.0, 4.8, 45.0, 12.0, portion = 60.0),
        food(82, "Biscoito de água e sal", 432.0, 10.1, 68.7, 14.4, portion = 30.0),
        food(83, "Biscoito recheado", 472.0, 6.4, 70.5, 19.6, portion = 30.0),
        food(84, "Pão de forma", 253.0, 12.0, 44.0, 2.7, portion = 50.0),
        food(85, "Misto quente", 260.0, 13.0, 25.0, 12.0, portion = 120.0),
        food(86, "Sanduíche natural de frango", 190.0, 11.0, 22.0, 6.0, portion = 150.0),
        food(87, "Coxinha", 283.0, 9.6, 34.5, 11.8, portion = 80.0),
        food(88, "Esfiha de carne", 285.0, 11.0, 40.0, 9.0, portion = 80.0),
        food(89, "Açaí na tigela", 130.0, 1.5, 20.0, 5.0, portion = 300.0),
        food(90, "Barra de cereal", 380.0, 5.0, 70.0, 9.0, portion = 25.0),
        food(91, "Barra de proteína", 360.0, 30.0, 35.0, 11.0, portion = 45.0),
        food(92, "Mix de castanhas", 600.0, 18.0, 22.0, 52.0, portion = 30.0),
        food(93, "Peito de peru fatiado", 102.0, 18.0, 3.0, 2.0, portion = 40.0),
        food(94, "Queijo prato", 360.0, 22.7, 1.9, 29.0, portion = 30.0),
        food(95, "Cuscuz de milho", 113.0, 2.2, 25.3, 0.7, portion = 150.0),
        food(96, "Uva", 53.0, 0.7, 13.6, 0.2, portion = 100.0),
        food(97, "Melancia", 33.0, 0.9, 8.1, 0.0, portion = 200.0),
        food(98, "Abacaxi", 48.0, 0.9, 12.3, 0.1, portion = 100.0),
    )

    /**
     * Sugestões de cada refeição: aparecem no topo da lista quando o usuário
     * abre "Adicionar alimento" sem digitar nada (ids da lista acima).
     */
    private val suggestions: Map<MealType, List<Int>> = mapOf(
        MealType.BREAKFAST to listOf(9, 84, 10, 22, 75, 64, 30, 95, 11, 33, 34, 40, 42, 32, 12, 80),
        MealType.LUNCH to listOf(1, 2, 3, 4, 20, 21, 24, 8, 5, 6, 51, 52, 53, 50, 62),
        MealType.SNACK to listOf(70, 71, 72, 73, 74, 76, 40, 41, 96, 97, 98, 32, 80, 27, 91, 90, 92, 85, 86, 84, 93, 94, 82, 83, 81, 13, 87, 88, 89),
        MealType.DINNER to listOf(20, 24, 25, 21, 22, 5, 6, 1, 8, 50, 51, 52, 53, 86, 62),
    )

    /** Alimentos sugeridos para a refeição, na ordem definida acima. */
    fun suggestionsFor(mealType: MealType): List<Food> {
        val ids = suggestions[mealType] ?: return emptyList()
        return ids.mapNotNull { id -> foods.find { it.id == "taco_$id" } }
    }
}
