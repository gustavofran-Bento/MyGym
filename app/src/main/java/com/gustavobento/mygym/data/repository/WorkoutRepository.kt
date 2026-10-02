package com.gustavobento.mygym.data.repository

import com.gustavobento.mygym.data.local.LocalStorage
import com.gustavobento.mygym.data.mock.WorkoutData
import com.gustavobento.mygym.model.Exercise
import com.gustavobento.mygym.model.Workout
import com.gustavobento.mygym.model.WorkoutLog
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/**
 * Treinos do usuário (salvos) e histórico de treinos finalizados.
 * Na primeira vez, o usuário recebe as fichas padrão A, B e C (dados mock).
 */
class WorkoutRepository(private val storage: LocalStorage) {

    fun workouts(userId: String): List<Workout> {
        val json = storage.readString(workoutsKey(userId)) ?: return WorkoutData.workouts
        return workoutsFromJson(json)
    }

    /** Retorna o treino ou null se o id não existir. */
    fun workout(userId: String, id: Int): Workout? {
        return workouts(userId).find { it.id == id }
    }

    /** Treino programado para a data (null = dia de descanso). */
    fun workoutFor(userId: String, date: LocalDate): Workout? {
        val id = WorkoutData.workoutIdFor(date.dayOfWeek) ?: return null
        return workout(userId, id)
    }

    /** Cria um treino novo, vazio. A letra é a próxima da sequência (D, E, F…). */
    fun addWorkout(userId: String, name: String, muscleGroup: String): Workout {
        val workouts = workouts(userId).toMutableList()
        val nextId = (workouts.maxOfOrNull { it.id } ?: 0) + 1
        val label = ('A' + workouts.size).toString()
        val workout = Workout(nextId, label, name.trim(), muscleGroup.trim(), emptyList())
        workouts.add(workout)
        saveWorkouts(userId, workouts)
        return workout
    }

    /** Adiciona um exercício no fim do treino. Como Workout é imutável, criamos uma cópia. */
    fun addExercise(userId: String, workoutId: Int, name: String, sets: Int, repetitions: Int, weightKg: Double): Workout? {
        val workouts = workouts(userId).toMutableList()
        val index = workouts.indexOfFirst { it.id == workoutId }
        if (index == -1) return null

        val nextExerciseId = (workouts.flatMap { it.exercises }.maxOfOrNull { it.id } ?: 0) + 1
        val exercise = Exercise(nextExerciseId, name.trim(), sets, repetitions, weightKg)
        val updated = workouts[index].copy(exercises = workouts[index].exercises + exercise)
        workouts[index] = updated
        saveWorkouts(userId, workouts)
        return updated
    }

    // ---------- Histórico ----------

    fun saveLog(userId: String, log: WorkoutLog) {
        val logs = loadLogs(userId).toMutableList()
        logs.add(log)
        val array = JSONArray()
        for (item in logs) {
            val json = JSONObject()
            json.put("date", item.date.toString())
            json.put("workoutId", item.workoutId)
            json.put("completed", item.completedExercises)
            json.put("total", item.totalExercises)
            array.put(json)
        }
        storage.saveString(logsKey(userId), array.toString())
    }

    fun logsBetween(userId: String, start: LocalDate, end: LocalDate): List<WorkoutLog> {
        return loadLogs(userId).filter { !it.date.isBefore(start) && !it.date.isAfter(end) }
    }

    // ---------- JSON ----------

    private fun workoutsKey(userId: String) = "fichas_$userId"
    private fun logsKey(userId: String) = "treinos_$userId"

    private fun saveWorkouts(userId: String, workouts: List<Workout>) {
        val array = JSONArray()
        for (workout in workouts) {
            val exercisesJson = JSONArray()
            for (exercise in workout.exercises) {
                val item = JSONObject()
                item.put("id", exercise.id)
                item.put("name", exercise.name)
                item.put("sets", exercise.sets)
                item.put("repetitions", exercise.repetitions)
                item.put("weightKg", exercise.weightKg)
                if (exercise.notes != null) item.put("notes", exercise.notes)
                exercisesJson.put(item)
            }
            val json = JSONObject()
            json.put("id", workout.id)
            json.put("label", workout.label)
            json.put("name", workout.name)
            json.put("muscleGroup", workout.muscleGroup)
            json.put("exercises", exercisesJson)
            array.put(json)
        }
        storage.saveString(workoutsKey(userId), array.toString())
    }

    private fun workoutsFromJson(json: String): List<Workout> {
        val array = JSONArray(json)
        val workouts = mutableListOf<Workout>()
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val exercisesJson = item.getJSONArray("exercises")
            val exercises = mutableListOf<Exercise>()
            for (j in 0 until exercisesJson.length()) {
                val ex = exercisesJson.getJSONObject(j)
                exercises.add(
                    Exercise(
                        id = ex.getInt("id"),
                        name = ex.getString("name"),
                        sets = ex.getInt("sets"),
                        repetitions = ex.getInt("repetitions"),
                        weightKg = ex.getDouble("weightKg"),
                        notes = if (ex.has("notes")) ex.getString("notes") else null,
                    ),
                )
            }
            workouts.add(Workout(item.getInt("id"), item.getString("label"), item.getString("name"), item.getString("muscleGroup"), exercises))
        }
        return workouts
    }

    private fun loadLogs(userId: String): List<WorkoutLog> {
        val json = storage.readString(logsKey(userId)) ?: return emptyList()
        val array = JSONArray(json)
        val logs = mutableListOf<WorkoutLog>()
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            logs.add(
                WorkoutLog(
                    date = LocalDate.parse(item.getString("date")),
                    workoutId = item.getInt("workoutId"),
                    completedExercises = item.getInt("completed"),
                    totalExercises = item.getInt("total"),
                ),
            )
        }
        return logs
    }
}
