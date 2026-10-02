package com.gustavobento.mygym.ui.workout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gustavobento.mygym.databinding.ItemWorkoutBinding
import com.gustavobento.mygym.model.Workout

/** Adapter da lista de treinos. O treino de hoje recebe a etiqueta "HOJE". */
class WorkoutAdapter(private val onWorkoutClick: (Workout) -> Unit) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    private var workouts: List<Workout> = emptyList()
    private var todayWorkoutId: Int? = null

    fun submitWorkouts(newWorkouts: List<Workout>, todayId: Int?) {
        workouts = newWorkouts
        todayWorkoutId = todayId
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = workouts.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val binding = ItemWorkoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WorkoutViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        holder.bind(workouts[position])
    }

    inner class WorkoutViewHolder(private val binding: ItemWorkoutBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(workout: Workout) {
            val isToday = workout.id == todayWorkoutId
            binding.textBadge.text = workout.label
            binding.textName.text = workout.name
            binding.textMuscle.text = workout.muscleGroup
            binding.textInfo.text = if (workout.isEmpty) {
                "Sem exercícios ainda"
            } else {
                "${workout.exercises.size} exercícios • ${workout.totalSets} séries • ~${workout.estimatedMinutes} min"
            }
            binding.textToday.visibility = if (isToday) View.VISIBLE else View.GONE
            binding.cardWorkout.contentDescription = "Treino ${workout.label}: ${workout.name}, " +
                "${workout.exercises.size} exercícios" + if (isToday) ", treino de hoje" else ""
            binding.cardWorkout.setOnClickListener { onWorkoutClick(workout) }
        }
    }
}
