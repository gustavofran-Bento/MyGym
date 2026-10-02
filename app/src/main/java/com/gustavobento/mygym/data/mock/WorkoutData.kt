package com.gustavobento.mygym.data.mock

import com.gustavobento.mygym.model.Exercise
import com.gustavobento.mygym.model.Workout
import java.time.DayOfWeek

/**
 * Fichas de treino (dados simulados). Os valores variam de propósito:
 * carga decimal (22,5 kg), peso corporal (0 kg), observações nulas e preenchidas.
 */
object WorkoutData {

    val workouts: List<Workout> = listOf(
        Workout(
            id = 1, label = "A", name = "Peito + Tríceps", muscleGroup = "Empurrar",
            exercises = listOf(
                Exercise(101, "Supino Reto", 4, 10, 30.0),
                Exercise(102, "Supino Inclinado com Halteres", 3, 12, 24.0),
                Exercise(103, "Crucifixo", 3, 12, 12.0, notes = "Controle a descida em 3 segundos"),
                Exercise(104, "Crossover na Polia", 3, 15, 15.0),
                Exercise(105, "Tríceps Corda", 4, 10, 25.0),
                Exercise(106, "Mergulho no Banco", 3, 12, 0.0, notes = "Última série até a falha"),
            ),
        ),
        Workout(
            id = 2, label = "B", name = "Costas + Bíceps", muscleGroup = "Puxar",
            exercises = listOf(
                Exercise(201, "Puxada Frontal", 4, 10, 50.0),
                Exercise(202, "Remada Curvada com Barra", 4, 10, 40.0),
                Exercise(203, "Remada Baixa no Triângulo", 3, 12, 45.0),
                Exercise(204, "Barra Fixa", 3, 8, 0.0),
                Exercise(205, "Rosca Direta", 3, 10, 22.5),
                Exercise(206, "Rosca Martelo Alternada com Halteres", 3, 12, 12.0),
            ),
        ),
        Workout(
            id = 3, label = "C", name = "Pernas", muscleGroup = "Membros inferiores",
            exercises = listOf(
                Exercise(301, "Agachamento Livre", 4, 8, 60.0, notes = "Aqueça com 2 séries leves"),
                Exercise(302, "Leg Press 45°", 4, 12, 150.0),
                Exercise(303, "Cadeira Extensora", 3, 12, 45.0),
                Exercise(304, "Mesa Flexora", 3, 12, 35.0),
                Exercise(305, "Stiff", 3, 10, 40.0),
                Exercise(306, "Panturrilha em Pé", 4, 15, 0.0),
            ),
        ),
    )

    /** Divisão semanal das fichas padrão: A/B/C, repete, e domingo é descanso (null). */
    fun workoutIdFor(day: DayOfWeek): Int? {
        return when (day) {
            DayOfWeek.MONDAY, DayOfWeek.THURSDAY -> 1
            DayOfWeek.TUESDAY, DayOfWeek.FRIDAY -> 2
            DayOfWeek.WEDNESDAY, DayOfWeek.SATURDAY -> 3
            DayOfWeek.SUNDAY -> null
        }
    }
}
