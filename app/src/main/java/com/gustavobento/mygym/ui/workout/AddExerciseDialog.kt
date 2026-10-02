package com.gustavobento.mygym.ui.workout

import android.content.Context
import android.view.LayoutInflater
import com.gustavobento.mygym.appContainer
import com.gustavobento.mygym.databinding.DialogAddExerciseBinding
import com.gustavobento.mygym.model.Workout
import com.gustavobento.mygym.ui.components.showFormDialog
import com.gustavobento.mygym.util.Validators

/** Janela para adicionar um exercício (nome, séries, repetições e carga) a um treino. */
class AddExerciseDialog(
    private val context: Context,
    private val userId: String,
    private val workoutId: Int,
    private val onAdded: (Workout) -> Unit,
) {

    private val binding = DialogAddExerciseBinding.inflate(LayoutInflater.from(context))

    fun show() {
        showFormDialog(context, "Adicionar exercício", binding.root, "Adicionar") { add() }
    }

    private fun add(): Boolean {
        val name = binding.inputName.text.toString()
        val setsText = binding.inputSets.text.toString()
        val repsText = binding.inputReps.text.toString()
        val weightText = binding.inputWeight.text.toString()

        binding.layoutName.error = Validators.validateExerciseName(name)
        binding.layoutSets.error = Validators.validateWholeNumber(setsText, 1, 20)
        binding.layoutReps.error = Validators.validateWholeNumber(repsText, 1, 100)
        binding.layoutWeight.error = Validators.validateLoad(weightText)

        val hasError = binding.layoutName.error != null ||
            binding.layoutSets.error != null ||
            binding.layoutReps.error != null ||
            binding.layoutWeight.error != null
        if (hasError) return false

        val updated = context.appContainer.workoutRepository.addExercise(
            userId = userId,
            workoutId = workoutId,
            name = name,
            sets = Validators.toNumber(setsText).toInt(),
            repetitions = Validators.toNumber(repsText).toInt(),
            weightKg = Validators.toNumber(weightText),
        ) ?: return false

        onAdded(updated)
        return true
    }
}
