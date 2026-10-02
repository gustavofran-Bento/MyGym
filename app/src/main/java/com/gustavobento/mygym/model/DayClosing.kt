package com.gustavobento.mygym.model

import java.time.LocalDate
import java.time.LocalTime

/** Resultado do dia ao finalizar. */
enum class DayResult {
    ALL_GOALS_MET,   // bateu todas as metas
    ALMOST_THERE,    // faltou só um pouco
    FAR_FROM_GOALS,  // ainda falta bastante
}

/**
 * "Fechamento" de um dia: um resumo salvo quando o usuário toca em
 * "Finalizar o dia" (o que comeu, água, treino e o resultado).
 */
data class DayClosing(
    val date: LocalDate,
    val finishedAt: LocalTime,
    val consumed: Macros,
    val goals: Macros,
    val waterGlasses: Int,
    val workoutDone: Boolean,
    val goalsMet: Int,
    val totalGoals: Int,
    val result: DayResult,
)
