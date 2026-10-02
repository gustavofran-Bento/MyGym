package com.gustavobento.mygym.ui.food

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.gustavobento.mygym.appContainer
import com.gustavobento.mygym.databinding.SheetAddFoodBinding
import com.gustavobento.mygym.model.DiaryEntry
import com.gustavobento.mygym.model.Food
import com.gustavobento.mygym.model.MealType
import com.gustavobento.mygym.util.Formatters
import com.gustavobento.mygym.util.NutritionCalculator
import com.gustavobento.mygym.util.Validators
import java.time.LocalDate

/**
 * Painel "Adicionar alimento" que sobe de baixo da tela do Diário.
 * 1. O usuário busca e toca em um alimento (RecyclerView).
 * 2. Digita os gramas → proteína, carbo, gordura e kcal são calculados na hora.
 * 3. "Adicionar" salva no diário e avisa a tela pelo [onAdded].
 */
class AddFoodSheet(
    private val context: Context,
    private val userId: String,
    private val date: LocalDate,
    private val mealType: MealType,
    private val onAdded: (DiaryEntry) -> Unit,
) {

    private val binding = SheetAddFoodBinding.inflate(LayoutInflater.from(context))
    private val dialog = BottomSheetDialog(context)
    private val adapter = FoodAdapter { food -> selectFood(food) }
    private var selectedFood: Food? = null

    fun show() {
        binding.textTitle.text = "${mealType.emoji} Adicionar ao ${mealType.label}"
        binding.textDate.text = Formatters.relativeDate(date)
        binding.buttonAdd.text = "Adicionar ao ${mealType.label}"
        binding.recyclerFoods.adapter = adapter

        binding.inputSearch.doAfterTextChanged { text -> showFoods(text.toString()) }
        binding.inputGrams.doAfterTextChanged { updatePortion() }
        binding.buttonAdd.setOnClickListener { addToDiary() }
        binding.buttonNewFood.setOnClickListener {
            // Abre a janela de novo alimento; ao salvar, ele já fica selecionado
            NewFoodDialog(context, userId) { food ->
                binding.inputSearch.setText("")
                selectFood(food)
            }.show(binding.inputSearch.text.toString())
        }

        showFoods("")
        dialog.setContentView(binding.root)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.behavior.skipCollapsed = true
        dialog.show()
    }

    /** Sem texto na busca: sugestões da refeição primeiro. Com texto: filtra tudo. */
    private fun showFoods(query: String) {
        val repository = context.appContainer.foodRepository
        val foods = if (query.isBlank()) {
            repository.foodsForMeal(userId, mealType)
        } else {
            repository.search(userId, query)
        }
        adapter.submitFoods(foods)
        binding.textEmpty.visibility = if (foods.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun selectFood(food: Food) {
        selectedFood = food
        binding.panelSelected.visibility = View.VISIBLE
        binding.textSelectedName.text = food.name
        binding.inputGrams.setText(Formatters.number(food.defaultGrams))
        updatePortion()
    }

    /** Cálculo automático: macros da quantidade digitada. */
    private fun updatePortion() {
        val food = selectedFood ?: return
        val gramsText = binding.inputGrams.text.toString()
        val error = Validators.validateGrams(gramsText)
        binding.layoutGrams.error = if (gramsText.isEmpty()) null else error

        val grams = if (error == null) Validators.toNumber(gramsText) else 0.0
        val portion = NutritionCalculator.macrosForPortion(food, grams)

        binding.textPortionCalories.text = "${Formatters.integer(portion.calories)} kcal"
        binding.textPortionMacros.text = "P ${Formatters.number(portion.protein)} g • " +
            "C ${Formatters.number(portion.carbs)} g • G ${Formatters.number(portion.fat)} g"
        binding.layoutPortion.contentDescription = "Nesta porção: ${Formatters.integer(portion.calories)} quilocalorias, " +
            "${Formatters.number(portion.protein)} gramas de proteína"
        binding.buttonAdd.isEnabled = error == null
    }

    private fun addToDiary() {
        val food = selectedFood ?: return
        val gramsText = binding.inputGrams.text.toString()
        val error = Validators.validateGrams(gramsText)
        binding.layoutGrams.error = error
        if (error != null) return

        val entry = context.appContainer.diaryRepository.addFood(
            userId, date, mealType, food, Validators.toNumber(gramsText),
        )
        onAdded(entry)
        dialog.dismiss()
    }
}
