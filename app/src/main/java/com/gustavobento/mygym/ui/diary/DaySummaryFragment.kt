package com.gustavobento.mygym.ui.diary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.gustavobento.mygym.R
import com.gustavobento.mygym.appContainer
import com.gustavobento.mygym.databinding.FragmentDaySummaryBinding
import com.gustavobento.mygym.databinding.ViewMacroBarBinding
import com.gustavobento.mygym.util.Formatters
import com.gustavobento.mygym.util.NutritionCalculator
import java.time.LocalDate

/**
 * Fragment com o resumo nutricional de UM dia: calorias (anel) e macros (barras).
 *
 * O dia mostrado fica guardado nos "arguments" do Fragment, então sobrevive
 * à rotação da tela. A Activity troca o dia chamando [showDay].
 */
class DaySummaryFragment : Fragment() {

    // O binding só existe entre onCreateView e onDestroyView
    private var _binding: FragmentDaySummaryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDaySummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        render()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // a View foi destruída: não podemos mais acessá-la
    }

    /** Chamado pela Activity quando o usuário escolhe outro dia ou adiciona um alimento. */
    fun showDay(date: LocalDate) {
        requireArguments().putLong(ARG_DATE, date.toEpochDay())
        if (_binding != null) render()
    }

    private fun render() {
        val user = requireContext().appContainer.authRepository.currentUser() ?: return
        val date = LocalDate.ofEpochDay(requireArguments().getLong(ARG_DATE))
        val entries = requireContext().appContainer.diaryRepository.entriesFor(user.id, date)

        val goals = NutritionCalculator.dailyGoals(user)
        val consumed = NutritionCalculator.total(entries)
        val remaining = goals.calories - consumed.calories

        binding.textCaloriesConsumed.text = Formatters.integer(consumed.calories)
        binding.textCaloriesGoal.text = "${Formatters.integer(goals.calories)} kcal"
        if (remaining >= 0) {
            binding.textRemainingLabel.text = "Restante"
            binding.textRemaining.text = "${Formatters.integer(remaining)} kcal"
            binding.textRemaining.setTextColor(ContextCompat.getColor(requireContext(), R.color.mg_primary))
        } else {
            binding.textRemainingLabel.text = "Acima da meta"
            binding.textRemaining.text = "+${Formatters.integer(-remaining)} kcal"
            binding.textRemaining.setTextColor(ContextCompat.getColor(requireContext(), R.color.mg_error))
        }

        val percent = NutritionCalculator.percent(consumed.calories, goals.calories)
        binding.progressCalories.setProgressCompat(percent, true)
        binding.layoutCaloriesRing.contentDescription =
            "Calorias: ${Formatters.integer(consumed.calories)} de ${Formatters.integer(goals.calories)}, $percent por cento"

        showMacro(binding.macroProtein, getString(R.string.macro_protein), consumed.protein, goals.protein, R.color.mg_protein)
        showMacro(binding.macroCarbs, getString(R.string.macro_carbs), consumed.carbs, goals.carbs, R.color.mg_carbs)
        showMacro(binding.macroFat, getString(R.string.macro_fat), consumed.fat, goals.fat, R.color.mg_fat)
    }

    /** Preenche o componente reutilizável view_macro_bar.xml. */
    private fun showMacro(bar: ViewMacroBarBinding, label: String, consumed: Double, goal: Double, colorRes: Int) {
        val passedGoal = consumed > goal
        val color = if (passedGoal) R.color.mg_error else colorRes

        bar.textLabel.text = label
        bar.textValue.text = "${Formatters.integer(consumed)} / ${Formatters.integer(goal)} g"
        bar.progress.setIndicatorColor(ContextCompat.getColor(requireContext(), color))
        bar.progress.setProgressCompat(NutritionCalculator.percent(consumed, goal), true)
        bar.root.contentDescription = "$label: ${Formatters.integer(consumed)} de ${Formatters.integer(goal)} gramas" +
            if (passedGoal) ", acima da meta" else ""
    }

    companion object {
        private const val ARG_DATE = "date"

        /** Cria o Fragment já com o dia que deve mostrar (passado por arguments). */
        fun newInstance(date: LocalDate): DaySummaryFragment {
            val fragment = DaySummaryFragment()
            val args = Bundle()
            args.putLong(ARG_DATE, date.toEpochDay())
            fragment.arguments = args
            return fragment
        }
    }
}
