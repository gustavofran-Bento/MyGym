package com.gustavobento.mygym.model

import java.time.LocalDate

/**
 * Exercício de uma ficha de treino.
 * @property weightKg carga em kg. 0.0 significa exercício com o peso do corpo.
 */
data class Exercise(
    val id: Int,
    val name: String,
    val sets: Int,
    val repetitions: Int,
    val weightKg: Double,
    val notes: String? = null,
) {
    val isBodyweight: Boolean
        get() = weightKg <= 0.0

    /** Volume de carga = séries × repetições × kg (peso corporal conta como 0). */
    val volumeKg: Double
        get() {
            if (isBodyweight || sets <= 0 || repetitions <= 0) return 0.0
            return sets * repetitions * weightKg
        }
}

/** Ficha de treino (ex.: Treino A — Peito + Tríceps). Sem exercícios = treino ainda vazio. */
data class Workout(
    val id: Int,
    val label: String,
    val name: String,
    val muscleGroup: String,
    val exercises: List<Exercise>,
) {
    /** Tempo estimado: cerca de 3 minutos por série (execução + descanso). */
    val estimatedMinutes: Int
        get() = totalSets * 3

    val isEmpty: Boolean
        get() = exercises.isEmpty()

    val totalSets: Int
        get() = exercises.sumOf { if (it.sets > 0) it.sets else 0 }

    val totalVolumeKg: Double
        get() = exercises.sumOf { it.volumeKg }
}

/** Registro de um treino finalizado em uma data. */
data class WorkoutLog(
    val date: LocalDate,
    val workoutId: Int,
    val completedExercises: Int,
    val totalExercises: Int,
)
