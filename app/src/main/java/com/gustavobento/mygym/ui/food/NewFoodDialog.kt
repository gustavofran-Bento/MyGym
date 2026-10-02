package com.gustavobento.mygym.ui.food

import android.content.Context
import android.view.LayoutInflater
import androidx.core.widget.doAfterTextChanged
import com.gustavobento.mygym.appContainer
import com.gustavobento.mygym.databinding.DialogNewFoodBinding
import com.gustavobento.mygym.model.Food
import com.gustavobento.mygym.ui.components.showFormDialog
import com.gustavobento.mygym.util.Formatters
import com.gustavobento.mygym.util.NutritionCalculator
import com.gustavobento.mygym.util.Validators

/**
 * Janela para cadastrar um alimento novo. O usuário digita proteína,
 * carboidrato e gordura em 100 g e as calorias são calculadas na hora.
 */
class NewFoodDialog(
    private val context: Context,
    private val userId: String,
    private val onSaved: (Food) -> Unit,
) {

    private val binding = DialogNewFoodBinding.inflate(LayoutInflater.from(context))

    fun show(suggestedName: String) {
        binding.inputName.setText(suggestedName.trim())
        binding.inputName.doAfterTextChanged { binding.layoutName.error = null }
        binding.inputProtein.doAfterTextChanged { updateCalculation() }
        binding.inputCarbs.doAfterTextChanged { updateCalculation() }
        binding.inputFat.doAfterTextChanged { updateCalculation() }
        updateCalculation()

        showFormDialog(context, "Novo alimento", binding.root, "Salvar") { save() }
    }

    /** Campo vazio conta como 0 só para a prévia do cálculo. */
    private fun valueOf(text: String): Double {
        val value = Validators.parseNumber(text) ?: return 0.0
        return if (value < 0) 0.0 else value
    }

    private fun updateCalculation() {
        val protein = valueOf(binding.inputProtein.text.toString())
        val carbs = valueOf(binding.inputCarbs.text.toString())
        val fat = valueOf(binding.inputFat.text.toString())
        val calories = NutritionCalculator.caloriesFromMacros(protein, carbs, fat)

        binding.textCalories.text = "${Formatters.integer(calories)} kcal em 100 g"
        binding.textFormula.text = "${Formatters.number(protein)} × 4 + ${Formatters.number(carbs)} × 4 + ${Formatters.number(fat)} × 9"
    }

    /** Retorna true se salvou (a janela fecha) ou false se algum campo é inválido. */
    private fun save(): Boolean {
        val name = binding.inputName.text.toString()
        val proteinText = binding.inputProtein.text.toString()
        val carbsText = binding.inputCarbs.text.toString()
        val fatText = binding.inputFat.text.toString()
        val portionText = binding.inputPortion.text.toString()

        binding.layoutName.error = Validators.validateFoodName(name)
        binding.layoutProtein.error = Validators.validateMacroPer100g(proteinText)
        binding.layoutCarbs.error = Validators.validateMacroPer100g(carbsText)
        binding.layoutFat.error = Validators.validateMacroPer100g(fatText)
        binding.layoutPortion.error = Validators.validateGrams(portionText)

        val hasError = binding.layoutName.error != null ||
            binding.layoutProtein.error != null ||
            binding.layoutCarbs.error != null ||
            binding.layoutFat.error != null ||
            binding.layoutPortion.error != null
        if (hasError) return false

        val protein = Validators.toNumber(proteinText)
        val carbs = Validators.toNumber(carbsText)
        val fat = Validators.toNumber(fatText)

        // Em 100 g, proteína + carboidrato + gordura não passam de 100 g
        val sumError = Validators.validateMacroSum(protein, carbs, fat)
        if (sumError != null) {
            binding.layoutFat.error = sumError
            return false
        }

        val food = context.appContainer.foodRepository.addCustomFood(
            userId = userId,
            name = name,
            protein = protein,
            carbs = carbs,
            fat = fat,
            defaultGrams = Validators.toNumber(portionText),
        )
        onSaved(food)
        return true
    }
}
