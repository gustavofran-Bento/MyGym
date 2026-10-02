package com.gustavobento.mygym.ui.diary

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.gustavobento.mygym.R
import com.gustavobento.mygym.databinding.DialogDayResultBinding
import com.gustavobento.mygym.databinding.ItemGoalCheckBinding
import com.gustavobento.mygym.model.DayResult
import com.gustavobento.mygym.util.DayEvaluation

/**
 * Janelas do "Finalizar o dia":
 * - [showResult]: parabéns (bateu tudo / faltou só um pouco)
 * - [askConfirmation]: "tem certeza?" quando ainda falta bastante
 */
class DayResultDialog(private val context: Context) {

    fun showResult(evaluation: DayEvaluation) {
        val view = if (evaluation.result == DayResult.ALL_GOALS_MET) {
            buildView(evaluation, "🏆", "Parabéns! Você bateu todas as suas metas!", "Dia perfeito. Seu dia foi salvo — continue assim!")
        } else {
            buildView(evaluation, "💪", "Parabéns! Hoje faltou só um pouco!", "Você ficou muito perto das metas. Seu dia foi salvo.")
        }
        MaterialAlertDialogBuilder(context)
            .setView(view)
            .setPositiveButton("Fechar", null)
            .show()
    }

    fun askConfirmation(evaluation: DayEvaluation, onConfirm: () -> Unit) {
        val view = buildView(
            evaluation,
            "⚠️",
            "Tem certeza que deseja finalizar?",
            "Ainda falta bastante para concluir suas metas de hoje.",
        )
        MaterialAlertDialogBuilder(context)
            .setView(view)
            .setNegativeButton("Continuar registrando", null)
            .setPositiveButton("Finalizar mesmo assim") { _, _ -> onConfirm() }
            .show()
    }

    /** Monta o conteúdo: emoji, título, mensagem e uma linha (item_goal_check.xml) por meta. */
    private fun buildView(evaluation: DayEvaluation, emoji: String, title: String, message: String): View {
        val inflater = LayoutInflater.from(context)
        val binding = DialogDayResultBinding.inflate(inflater)
        binding.textEmoji.text = emoji
        binding.textTitle.text = title
        binding.textMessage.text = message

        for (check in evaluation.checks) {
            val row = ItemGoalCheckBinding.inflate(inflater, binding.checksContainer, true)
            row.textLabel.text = check.label
            row.textDetail.text = check.detail

            // Ícone + texto + cor: o status não depende só da cor
            var statusText = "Faltou"
            var colorRes = R.color.mg_error
            var iconRes = R.drawable.ic_error_outline
            if (check.isMet) {
                statusText = "Bateu"
                colorRes = R.color.mg_primary
                iconRes = R.drawable.ic_check_circle
            } else if (check.isNear) {
                statusText = "Quase"
                colorRes = R.color.mg_carbs
                iconRes = R.drawable.ic_circle_outline
            }
            val color = ContextCompat.getColor(context, colorRes)
            row.textStatus.text = statusText
            row.textStatus.setTextColor(color)
            row.imageStatus.setImageResource(iconRes)
            row.imageStatus.setColorFilter(color)
            row.root.contentDescription = "${check.label}: ${check.detail}. $statusText"
        }
        return binding.root
    }
}
