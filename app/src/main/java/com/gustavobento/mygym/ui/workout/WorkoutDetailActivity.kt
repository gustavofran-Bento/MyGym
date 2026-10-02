package com.gustavobento.mygym.ui.workout

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.gustavobento.mygym.R
import com.gustavobento.mygym.appContainer
import com.gustavobento.mygym.databinding.ActivityWorkoutDetailBinding
import com.gustavobento.mygym.databinding.ViewStatBinding
import com.gustavobento.mygym.model.Workout
import com.gustavobento.mygym.model.WorkoutLog
import com.gustavobento.mygym.util.Formatters
import com.gustavobento.mygym.util.applySystemBarsPadding
import java.time.LocalDate

/**
 * TELA 3 — Detalhe do treino.
 *
 * Recebe o id e o nome do treino pela Intent explícita. O usuário pode marcar
 * exercícios como concluídos, adicionar exercícios e finalizar o treino.
 *
 * Estados: conteúdo, vazio (treino sem exercícios) e erro (id inexistente).
 */
class WorkoutDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkoutDetailBinding
    private lateinit var userId: String
    private var workout: Workout? = null

    /** Ids dos exercícios concluídos. É salvo ao girar a tela. */
    private val completedIds = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityWorkoutDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()

        val user = appContainer.authRepository.currentUser()
        if (user == null) {
            finish()
            return
        }
        userId = user.id

        // Restaura os exercícios marcados antes da rotação
        val savedIds = savedInstanceState?.getIntArray(KEY_COMPLETED)
        if (savedIds != null) {
            completedIds.addAll(savedIds.toList())
        }

        // 1) Lê os dados enviados pela Intent
        val workoutId = intent.getIntExtra(EXTRA_WORKOUT_ID, -1)
        val workoutName = intent.getStringExtra(EXTRA_WORKOUT_NAME)

        binding.toolbar.title = workoutName ?: ""
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.buttonErrorBack.setOnClickListener { finish() }
        binding.buttonFinish.setOnClickListener { finishWorkout() }
        binding.buttonAddExercise.setOnClickListener {
            AddExerciseDialog(this, userId, workoutId) { updated ->
                workout = updated
                showWorkout(updated)
            }.show()
        }

        // 2) Busca o treino. Se não existir, mostra o estado de erro (sem travar o app)
        val found = appContainer.workoutRepository.workout(userId, workoutId)
        workout = found
        if (found == null) {
            showError()
        } else {
            showWorkout(found)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray(KEY_COMPLETED, completedIds.toIntArray())
    }

    private fun showWorkout(workout: Workout) {
        binding.textBadge.text = workout.label
        binding.textMuscleGroup.text = workout.muscleGroup

        // Estado VAZIO: sem exercícios → mostra a mensagem no lugar da lista
        val isEmpty = workout.isEmpty
        binding.layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerExercises.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.layoutStats.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.layoutProgress.visibility = if (isEmpty) View.GONE else View.VISIBLE

        if (isEmpty) {
            binding.textSummary.text = "Nenhum exercício ainda"
            binding.buttonFinish.isEnabled = false
            return
        }

        binding.textSummary.text = "${workout.exercises.size} exercícios • ~${workout.estimatedMinutes} min"
        showStat(binding.statExercises, workout.exercises.size.toString(), getString(R.string.detail_stat_exercises))
        showStat(binding.statSets, workout.totalSets.toString(), getString(R.string.detail_stat_sets))
        showStat(binding.statVolume, Formatters.integer(workout.totalVolumeKg), getString(R.string.detail_stat_volume))

        binding.recyclerExercises.adapter = ExerciseAdapter(workout.exercises, completedIds) { exercise ->
            toggleExercise(exercise.id, workout)
        }
        updateProgress(workout, animate = false)
    }

    /** Preenche o componente reutilizável view_stat.xml. */
    private fun showStat(stat: ViewStatBinding, value: String, label: String) {
        stat.textValue.text = value
        stat.textLabel.text = label
        stat.root.contentDescription = "$label: $value"
    }

    private fun toggleExercise(exerciseId: Int, workout: Workout) {
        if (completedIds.contains(exerciseId)) {
            completedIds.remove(exerciseId)
        } else {
            completedIds.add(exerciseId)
        }
        // Redesenha só o item que mudou
        val position = workout.exercises.indexOfFirst { it.id == exerciseId }
        binding.recyclerExercises.adapter?.notifyItemChanged(position)
        updateProgress(workout, animate = true)
    }

    private fun updateProgress(workout: Workout, animate: Boolean) {
        val done = completedIds.size
        val total = workout.exercises.size
        val percent = if (total == 0) 0 else done * 100 / total

        binding.textProgress.text = "$done de $total concluídos"
        binding.textProgressPercent.text = "$percent%"
        binding.progressWorkout.setProgressCompat(percent, animate)
        binding.buttonFinish.isEnabled = done > 0
    }

    /** Salva o treino no histórico e devolve uma mensagem para o Diário. */
    private fun finishWorkout() {
        val workout = workout ?: return
        val done = completedIds.size
        val total = workout.exercises.size

        appContainer.workoutRepository.saveLog(userId, WorkoutLog(LocalDate.now(), workout.id, done, total))

        val message = if (done == total) {
            "Treino concluído! 💪 Bom trabalho."
        } else {
            "Treino salvo: $done de $total exercícios."
        }
        val result = Intent()
        result.putExtra(RESULT_MESSAGE, message)
        setResult(Activity.RESULT_OK, result)
        finish()
    }

    private fun showError() {
        binding.layoutContent.visibility = View.GONE
        binding.layoutBottomBar.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE
    }

    companion object {
        private const val EXTRA_WORKOUT_ID = "extra_workout_id"
        private const val EXTRA_WORKOUT_NAME = "extra_workout_name"
        private const val KEY_COMPLETED = "completed_ids"
        const val RESULT_MESSAGE = "result_message"

        /** Cria a Intent EXPLÍCITA com os dados que esta tela precisa. */
        fun newIntent(context: Context, workoutId: Int, workoutName: String): Intent {
            val intent = Intent(context, WorkoutDetailActivity::class.java)
            intent.putExtra(EXTRA_WORKOUT_ID, workoutId)
            intent.putExtra(EXTRA_WORKOUT_NAME, workoutName)
            return intent
        }
    }
}
