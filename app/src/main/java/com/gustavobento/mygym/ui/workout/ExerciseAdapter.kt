package com.gustavobento.mygym.ui.workout

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.gustavobento.mygym.R
import com.gustavobento.mygym.databinding.ItemExerciseBinding
import com.gustavobento.mygym.model.Exercise
import com.gustavobento.mygym.util.Formatters

/**
 * Adapter da lista de exercícios. Guarda quais exercícios estão concluídos
 * e avisa a Activity (onToggle) quando o usuário toca em um item.
 */
class ExerciseAdapter(
    private val exercises: List<Exercise>,
    private val completedIds: Set<Int>,
    private val onToggle: (Exercise) -> Unit,
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    override fun getItemCount(): Int = exercises.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val binding = ItemExerciseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExerciseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(exercises[position], position + 1)
    }

    inner class ExerciseViewHolder(private val binding: ItemExerciseBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(exercise: Exercise, number: Int) {
            val context = binding.root.context
            val isDone = completedIds.contains(exercise.id)

            val setsReps = "${exercise.sets} séries × ${exercise.repetitions} reps"
            val weight = if (exercise.isBodyweight) {
                context.getString(R.string.exercise_bodyweight)
            } else {
                "${Formatters.number(exercise.weightKg)} kg"
            }

            binding.textNumber.text = number.toString()
            binding.textName.text = exercise.name
            binding.textSetsReps.text = setsReps
            binding.textWeight.text = weight

            // Observação é opcional: se for nula ou vazia, o campo fica escondido
            if (exercise.notes.isNullOrBlank()) {
                binding.textNotes.visibility = View.GONE
            } else {
                binding.textNotes.visibility = View.VISIBLE
                binding.textNotes.text = exercise.notes
            }

            // Concluído: ícone de check + nome riscado + borda verde
            if (isDone) {
                binding.imageStatus.setImageResource(R.drawable.ic_check_circle)
                binding.textName.paintFlags = binding.textName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.cardExercise.strokeColor = ContextCompat.getColor(context, R.color.mg_primary)
            } else {
                binding.imageStatus.setImageResource(R.drawable.ic_circle_outline)
                binding.textName.paintFlags = binding.textName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.cardExercise.strokeColor = ContextCompat.getColor(context, R.color.mg_outline)
            }

            val status = if (isDone) "Concluído" else "Pendente"
            binding.cardExercise.contentDescription = "${exercise.name}, $setsReps, $weight. $status. Toque duas vezes para alternar."
            binding.cardExercise.setOnClickListener { onToggle(exercise) }
        }
    }
}
