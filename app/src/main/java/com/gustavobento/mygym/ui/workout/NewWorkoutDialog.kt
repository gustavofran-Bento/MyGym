package com.gustavobento.mygym.ui.workout

import android.content.Context
import android.view.LayoutInflater
import androidx.core.widget.doAfterTextChanged
import com.gustavobento.mygym.appContainer
import com.gustavobento.mygym.databinding.DialogNewWorkoutBinding
import com.gustavobento.mygym.model.Workout
import com.gustavobento.mygym.ui.components.showFormDialog
import com.gustavobento.mygym.util.Validators

/** Janela para criar um treino novo (começa vazio; os exercícios são adicionados depois). */
class NewWorkoutDialog(
    private val context: Context,
    private val userId: String,
    private val onCreated: (Workout) -> Unit,
) {

    private val binding = DialogNewWorkoutBinding.inflate(LayoutInflater.from(context))

    fun show() {
        binding.inputName.doAfterTextChanged { binding.layoutName.error = null }
        showFormDialog(context, "Novo treino", binding.root, "Criar") { create() }
    }

    private fun create(): Boolean {
        val name = binding.inputName.text.toString()
        binding.layoutName.error = Validators.validateWorkoutName(name)
        if (binding.layoutName.error != null) return false

        val muscle = binding.inputMuscle.text.toString().trim().ifEmpty { "Personalizado" }
        val workout = context.appContainer.workoutRepository.addWorkout(userId, name, muscle)
        onCreated(workout)
        return true
    }
}
