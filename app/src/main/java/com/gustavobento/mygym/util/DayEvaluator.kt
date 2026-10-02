package com.gustavobento.mygym.util

import com.gustavobento.mygym.model.DayResult
import com.gustavobento.mygym.model.Macros

/** Situação de UMA meta do dia (ex.: Água: 6 de 8 copos). */
data class GoalCheck(
    val label: String,
    val detail: String,
    val isMet: Boolean,
    val isNear: Boolean,
)

/** Avaliação completa do dia: as 4 metas e o resultado final. */
data class DayEvaluation(
    val checks: List<GoalCheck>,
    val result: DayResult,
) {
    val metCount: Int
        get() = checks.count { it.isMet }
}

/**
 * Regra de negócio do "Finalizar o dia".
 *
 * Metas avaliadas:          bateu            quase lá
 * - Calorias                90% a 110%       75% a 120%
 * - Proteína                90% ou mais      75% ou mais
 * - Água                    meta de copos    até 2 copos a menos
 * - Treino                  feito (ou descanso)
 *
 * Resultado: todas batidas → ALL_GOALS_MET; todas pelo menos "quase" → ALMOST_THERE;
 * senão → FAR_FROM_GOALS.
 */
object DayEvaluator {

    fun evaluate(
        consumed: Macros,
        goals: Macros,
        waterGlasses: Int,
        waterGoal: Int,
        workoutScheduled: Boolean,
        workoutDone: Boolean,
    ): DayEvaluation {
        val checks = listOf(
            checkCalories(consumed.calories, goals.calories),
            checkProtein(consumed.protein, goals.protein),
            checkWater(waterGlasses, waterGoal),
            checkWorkout(workoutScheduled, workoutDone),
        )

        val result = when {
            checks.all { it.isMet } -> DayResult.ALL_GOALS_MET
            checks.all { it.isNear } -> DayResult.ALMOST_THERE
            else -> DayResult.FAR_FROM_GOALS
        }
        return DayEvaluation(checks, result)
    }

    private fun ratio(value: Double, goal: Double): Double {
        return if (goal <= 0.0) 0.0 else value / goal
    }

    private fun checkCalories(consumed: Double, goal: Double): GoalCheck {
        val r = ratio(consumed, goal)
        return GoalCheck(
            label = "Calorias",
            detail = "${Formatters.integer(consumed)} de ${Formatters.integer(goal)} kcal",
            isMet = r >= 0.9 && r <= 1.1,
            isNear = r >= 0.75 && r <= 1.2,
        )
    }

    private fun checkProtein(consumed: Double, goal: Double): GoalCheck {
        val r = ratio(consumed, goal)
        return GoalCheck(
            label = "Proteína",
            detail = "${Formatters.integer(consumed)} de ${Formatters.integer(goal)} g",
            isMet = r >= 0.9,
            isNear = r >= 0.75,
        )
    }

    private fun checkWater(glasses: Int, goal: Int): GoalCheck {
        return GoalCheck(
            label = "Água",
            detail = "$glasses de $goal copos",
            isMet = glasses >= goal,
            isNear = glasses >= goal - 2,
        )
    }

    private fun checkWorkout(scheduled: Boolean, done: Boolean): GoalCheck {
        val detail = when {
            done -> "Treino finalizado"
            scheduled -> "Treino do dia não foi feito"
            else -> "Dia de descanso"
        }
        val ok = done || !scheduled
        return GoalCheck(label = "Treino", detail = detail, isMet = ok, isNear = ok)
    }
}
