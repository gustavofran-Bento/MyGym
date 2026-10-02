package com.gustavobento.mygym.ui.diary

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.gustavobento.mygym.R
import com.gustavobento.mygym.appContainer
import com.gustavobento.mygym.data.repository.DiaryRepository
import com.gustavobento.mygym.databinding.ActivityDiaryBinding
import com.gustavobento.mygym.databinding.ItemDiaryEntryBinding
import com.gustavobento.mygym.databinding.ViewDayBarBinding
import com.gustavobento.mygym.databinding.ViewMealSectionBinding
import com.gustavobento.mygym.databinding.ViewStatBinding
import com.gustavobento.mygym.databinding.ViewWeekDayBinding
import com.gustavobento.mygym.model.DayClosing
import com.gustavobento.mygym.model.DayResult
import com.gustavobento.mygym.model.DiaryEntry
import com.gustavobento.mygym.model.MealType
import com.gustavobento.mygym.model.User
import com.gustavobento.mygym.model.Workout
import com.gustavobento.mygym.ui.auth.LoginActivity
import com.gustavobento.mygym.ui.food.AddFoodSheet
import com.gustavobento.mygym.ui.workout.NewWorkoutDialog
import com.gustavobento.mygym.ui.workout.WorkoutAdapter
import com.gustavobento.mygym.ui.workout.WorkoutDetailActivity
import com.gustavobento.mygym.util.DateUtils
import com.gustavobento.mygym.util.DayEvaluation
import com.gustavobento.mygym.util.DayEvaluator
import com.gustavobento.mygym.util.Formatters
import com.gustavobento.mygym.util.NutritionCalculator
import com.gustavobento.mygym.util.applySystemBarsPadding
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * TELA 2 — Diário (tela principal).
 *
 * Seções (de cima para baixo):
 * 1. Cabeçalho com saudação, objetivo e botão de sair
 * 2. Filtro semanal (setas + 7 dias)
 * 3. Resumo da semana com gráfico de barras
 * 4. Resumo do dia escolhido (DaySummaryFragment)
 * 5. Água
 * 6. Refeições (adicionar e remover alimentos)
 * 7. Fechamento do dia ("Finalizar o dia")
 * 8. Treinos (abre a TELA 3 por Intent explícita)
 */
class DiaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDiaryBinding
    private lateinit var user: User
    private lateinit var workoutAdapter: WorkoutAdapter

    /** Dia escolhido no filtro. É salvo ao girar a tela. */
    private var selectedDate: LocalDate = LocalDate.now()

    /** Abre o detalhe do treino e recebe a mensagem quando o usuário finaliza. */
    private val workoutLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val message = result.data?.getStringExtra(WorkoutDetailActivity.RESULT_MESSAGE)
            if (message != null) showMessage(message)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Proteção: sem usuário logado, volta para o login
        val currentUser = appContainer.authRepository.currentUser()
        if (currentUser == null) {
            openLogin()
            return
        }
        user = currentUser

        binding = ActivityDiaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()

        if (savedInstanceState != null) {
            selectedDate = LocalDate.ofEpochDay(savedInstanceState.getLong(KEY_SELECTED_DATE))
        } else {
            // Adiciona o Fragment só na primeira vez (depois da rotação ele é recriado sozinho)
            supportFragmentManager.beginTransaction()
                .add(R.id.daySummaryContainer, DaySummaryFragment.newInstance(selectedDate))
                .commitNow()
        }

        binding.buttonLogout.setOnClickListener { confirmLogout() }
        binding.buttonPreviousWeek.setOnClickListener {
            selectedDate = selectedDate.minusWeeks(1)
            refresh()
        }
        binding.buttonNextWeek.setOnClickListener {
            val nextWeekDay = selectedDate.plusWeeks(1)
            selectedDate = if (nextWeekDay.isAfter(LocalDate.now())) LocalDate.now() else nextWeekDay
            refresh()
        }
        binding.buttonFinishDay.setOnClickListener { finishDay() }
        binding.buttonWaterAdd.setOnClickListener { changeWater(+1) }
        binding.buttonWaterRemove.setOnClickListener { changeWater(-1) }

        workoutAdapter = WorkoutAdapter { workout -> openWorkout(workout) }
        binding.recyclerWorkouts.adapter = workoutAdapter
        binding.buttonNewWorkout.setOnClickListener {
            NewWorkoutDialog(this, user.id) { workout -> openWorkout(workout) }.show()
        }

        showHeader()
    }

    /** onResume: a tela volta a aparecer (ex.: depois do detalhe do treino) → atualiza tudo. */
    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(KEY_SELECTED_DATE, selectedDate.toEpochDay())
    }

    /** Atualiza todas as seções a partir dos dados salvos. */
    private fun refresh() {
        val monday = DateUtils.startOfWeek(selectedDate)
        val days = DateUtils.daysOfWeek(monday)
        val weekEntries = appContainer.diaryRepository.entriesBetween(user.id, monday, monday.plusDays(6))

        showWeekHeader(monday)
        showDays(days, weekEntries)
        showWeekSummary(days, weekEntries)

        binding.textSelectedDay.text = Formatters.relativeDate(selectedDate)
        val summary = supportFragmentManager.findFragmentById(R.id.daySummaryContainer) as? DaySummaryFragment
        summary?.showDay(selectedDate)

        showWater()
        showMeals(weekEntries.filter { it.date == selectedDate })
        showDayClosing()
        showWorkouts()
    }

    // ================= 1. Cabeçalho =================

    private fun showHeader() {
        val goals = NutritionCalculator.dailyGoals(user)
        binding.textDate.text = Formatters.fullDate(LocalDate.now())
        binding.textGreeting.text = "Olá, ${user.firstName}! 👋"
        binding.textGoal.text = "🎯 ${user.goal.label} • ${Formatters.integer(goals.calories)} kcal/dia"
    }

    // ================= 2. Filtro semanal =================

    private fun showWeekHeader(monday: LocalDate) {
        val currentMonday = DateUtils.startOfWeek(LocalDate.now())
        binding.textWeekRange.text = Formatters.weekRange(monday, monday.plusDays(6))
        binding.textWeekLabel.text = when (monday) {
            currentMonday -> "Esta semana"
            currentMonday.minusWeeks(1) -> "Semana passada"
            else -> "${(currentMonday.toEpochDay() - monday.toEpochDay()) / 7} semanas atrás"
        }
        // Não existe semana futura para navegar
        val isCurrentWeek = monday == currentMonday
        binding.buttonNextWeek.isEnabled = !isCurrentWeek
        binding.buttonNextWeek.alpha = if (isCurrentWeek) 0.3f else 1f
    }

    /** Infla o componente view_week_day.xml para cada um dos 7 dias. */
    private fun showDays(days: List<LocalDate>, weekEntries: List<DiaryEntry>) {
        binding.daysContainer.removeAllViews()
        val today = LocalDate.now()

        for (day in days) {
            val item = ViewWeekDayBinding.inflate(layoutInflater, binding.daysContainer, true)
            val isSelected = day == selectedDate
            val isFuture = day.isAfter(today)
            val hasEntries = weekEntries.any { it.date == day }

            item.textDayName.text = Formatters.dayShort(day.dayOfWeek)
            item.textDayNumber.text = day.dayOfMonth.toString()
            item.dot.visibility = if (hasEntries) View.VISIBLE else View.INVISIBLE

            if (isSelected) {
                val dark = ContextCompat.getColor(this, R.color.mg_on_primary)
                item.dayRoot.setBackgroundResource(R.drawable.bg_day_selected)
                item.textDayName.setTextColor(dark)
                item.textDayNumber.setTextColor(dark)
                item.dot.background.mutate().setTint(dark)
            }
            item.dayRoot.alpha = if (isFuture) 0.35f else 1f
            item.dayRoot.isEnabled = !isFuture
            item.dayRoot.isSelected = isSelected
            item.dayRoot.contentDescription = Formatters.fullDate(day) + if (hasEntries) ", com registros" else ""

            item.dayRoot.setOnClickListener {
                selectedDate = day
                refresh()
            }
        }
    }

    // ================= 3. Resumo da semana =================

    private fun showWeekSummary(days: List<LocalDate>, weekEntries: List<DiaryEntry>) {
        val goal = NutritionCalculator.dailyGoals(user).calories

        // Calorias de cada dia da semana
        val caloriesPerDay = mutableListOf<Double>()
        for (day in days) {
            caloriesPerDay.add(NutritionCalculator.total(weekEntries.filter { it.date == day }).calories)
        }
        val daysWithData = days.filter { day -> weekEntries.any { it.date == day } }
        val workoutsDone = appContainer.workoutRepository.logsBetween(user.id, days.first(), days.last()).size

        // Estado VAZIO: semana sem nenhuma refeição
        val isEmptyWeek = daysWithData.isEmpty()
        binding.textChartEmpty.visibility = if (isEmptyWeek) View.VISIBLE else View.GONE
        binding.goalLine.visibility = if (isEmptyWeek) View.INVISIBLE else View.VISIBLE

        if (isEmptyWeek) {
            showStat(binding.statAverage, "—", "kcal/dia")
            showStat(binding.statOnGoal, "—", "dias na meta")
        } else {
            val average = NutritionCalculator.total(weekEntries).calories / daysWithData.size
            val daysOnGoal = caloriesPerDay.count { isOnGoal(it, goal) }
            showStat(binding.statAverage, Formatters.integer(average), "kcal/dia")
            showStat(binding.statOnGoal, "$daysOnGoal de ${daysWithData.size}", "dias na meta")
        }
        showStat(binding.statWorkouts, workoutsDone.toString(), "treinos")

        showChart(days, caloriesPerDay, goal)
    }

    /** "Na meta" = entre 90% e 110% das calorias planejadas. */
    private fun isOnGoal(calories: Double, goal: Double): Boolean {
        return calories >= goal * 0.9 && calories <= goal * 1.1
    }

    /** Preenche o componente reutilizável view_stat.xml. */
    private fun showStat(stat: ViewStatBinding, value: String, label: String) {
        stat.textValue.text = value
        stat.textLabel.text = label
        stat.root.contentDescription = "$label: $value"
    }

    /** Gráfico de barras: a altura de cada barra é proporcional às calorias do dia. */
    private fun showChart(days: List<LocalDate>, caloriesPerDay: List<Double>, goal: Double) {
        binding.barsContainer.removeAllViews()

        val density = resources.displayMetrics.density
        val barAreaHeight = CHART_BAR_AREA_DP * density
        val letterHeight = CHART_LETTER_DP * density
        // O topo do gráfico é o maior valor entre a meta (+25%) e o dia mais alto
        val maxValue = maxOf(goal * 1.25, caloriesPerDay.maxOrNull() ?: 0.0, 1.0)

        for (i in days.indices) {
            val day = days[i]
            val calories = caloriesPerDay[i]
            val item = ViewDayBarBinding.inflate(layoutInflater, binding.barsContainer, true)
            item.textLetter.text = Formatters.dayLetter(day.dayOfWeek)

            val barHeight = (calories / maxValue * barAreaHeight).toInt()
            item.bar.layoutParams.height = barHeight

            val colorRes = when {
                calories <= 0.0 -> R.color.mg_surface_high
                isOnGoal(calories, goal) -> R.color.mg_primary
                calories > goal -> R.color.mg_error
                else -> R.color.mg_carbs
            }
            item.bar.background.mutate().setTint(ContextCompat.getColor(this, colorRes))
            item.bar.alpha = if (day == selectedDate) 1f else 0.7f

            // Animação: a barra "cresce" de baixo para cima
            item.bar.pivotY = barHeight.toFloat()
            item.bar.scaleY = 0f
            item.bar.animate().scaleY(1f).setDuration(450).setStartDelay(i * 40L).start()

            item.root.setOnClickListener {
                if (!day.isAfter(LocalDate.now())) {
                    selectedDate = day
                    refresh()
                }
            }
        }

        // Linha da meta, na altura proporcional ao valor da meta
        val params = binding.goalLine.layoutParams as FrameLayout.LayoutParams
        params.bottomMargin = (letterHeight + goal / maxValue * barAreaHeight).toInt()
        binding.goalLine.layoutParams = params

        binding.textChartLegend.text = "Linha: sua meta de ${Formatters.integer(goal)} kcal por dia"
        binding.layoutChart.contentDescription = "Gráfico de calorias da semana. " +
            days.indices.joinToString(". ") { "${Formatters.dayShort(days[it].dayOfWeek)}: ${Formatters.integer(caloriesPerDay[it])} kcal" }
    }

    // ================= 5. Água =================

    private fun showWater() {
        val glasses = appContainer.diaryRepository.waterGlasses(user.id, selectedDate)
        val goal = DiaryRepository.WATER_GOAL_GLASSES
        val ml = glasses * DiaryRepository.ML_PER_GLASS
        binding.textWater.text = "$glasses de $goal copos • ${Formatters.integer(ml.toDouble())} ml"
        binding.progressWater.setProgressCompat(NutritionCalculator.percent(glasses.toDouble(), goal.toDouble()), true)
        binding.buttonWaterRemove.isEnabled = glasses > 0
    }

    private fun changeWater(amount: Int) {
        val repository = appContainer.diaryRepository
        val glasses = repository.waterGlasses(user.id, selectedDate) + amount
        repository.setWaterGlasses(user.id, selectedDate, glasses)
        showWater()
    }

    // ================= 6. Refeições =================

    private fun showMeals(dayEntries: List<DiaryEntry>) {
        binding.mealsContainer.removeAllViews()
        for (mealType in MealType.entries) {
            showMealSection(mealType, dayEntries.filter { it.mealType == mealType })
        }
    }

    /** Infla o card de uma refeição (view_meal_section.xml) e seus alimentos (item_diary_entry.xml). */
    private fun showMealSection(mealType: MealType, mealEntries: List<DiaryEntry>) {
        val section = ViewMealSectionBinding.inflate(layoutInflater, binding.mealsContainer, true)
        val calories = NutritionCalculator.total(mealEntries).calories

        section.textEmoji.text = mealType.emoji
        section.textMealName.text = mealType.label
        section.textMealCalories.text = "${Formatters.integer(calories)} kcal"
        section.textEmpty.visibility = if (mealEntries.isEmpty()) View.VISIBLE else View.GONE
        section.buttonAdd.contentDescription = "Adicionar alimento no ${mealType.label}"

        for (entry in mealEntries) {
            val row = ItemDiaryEntryBinding.inflate(layoutInflater, section.entriesContainer, true)
            row.textFoodName.text = entry.foodName
            row.textFoodDetail.text = "${Formatters.integer(entry.grams)} g • P ${Formatters.number(entry.macros.protein)} g • " +
                "C ${Formatters.number(entry.macros.carbs)} g • G ${Formatters.number(entry.macros.fat)} g"
            row.textFoodCalories.text = "${Formatters.integer(entry.macros.calories)} kcal"
            row.buttonDelete.contentDescription = "Remover ${entry.foodName}"
            row.buttonDelete.setOnClickListener { deleteEntry(entry) }
        }

        section.buttonAdd.setOnClickListener {
            AddFoodSheet(this, user.id, selectedDate, mealType) { entry ->
                refresh()
                showMessage("${entry.foodName} adicionado ao ${mealType.label}")
            }.show()
        }
    }

    /** Remove o alimento e oferece "Desfazer" (o registro volta igual). */
    private fun deleteEntry(entry: DiaryEntry) {
        appContainer.diaryRepository.removeEntry(user.id, entry.id)
        refresh()
        Snackbar.make(binding.root, "${entry.foodName} removido", Snackbar.LENGTH_LONG)
            .setAction(R.string.undo) {
                appContainer.diaryRepository.addEntries(user.id, listOf(entry))
                refresh()
            }
            .show()
    }

    // ================= 7. Fechamento do dia =================

    private fun showDayClosing() {
        val closing = appContainer.diaryRepository.closingFor(user.id, selectedDate)
        if (closing == null) {
            binding.textClosingStatus.text = if (selectedDate == LocalDate.now()) {
                "Quando terminar o dia, finalize para salvar o resumo de hoje."
            } else {
                "Este dia ainda não foi finalizado."
            }
            binding.buttonFinishDay.text = "Finalizar o dia"
            return
        }

        val resultText = when (closing.result) {
            DayResult.ALL_GOALS_MET -> "🏆 Todas as metas batidas"
            DayResult.ALMOST_THERE -> "💪 Faltou só um pouco"
            DayResult.FAR_FROM_GOALS -> "Metas não alcançadas"
        }
        val time = closing.finishedAt.format(DateTimeFormatter.ofPattern("HH:mm"))
        binding.textClosingStatus.text = "✅ Dia finalizado às $time • ${closing.goalsMet} de ${closing.totalGoals} metas\n$resultText"
        binding.buttonFinishDay.text = "Atualizar fechamento"
    }

    /** Avalia as 4 metas do dia escolhido (calorias, proteína, água e treino). */
    private fun evaluateSelectedDay(): DayEvaluation {
        val entries = appContainer.diaryRepository.entriesFor(user.id, selectedDate)
        val workoutRepository = appContainer.workoutRepository
        return DayEvaluator.evaluate(
            consumed = NutritionCalculator.total(entries),
            goals = NutritionCalculator.dailyGoals(user),
            waterGlasses = appContainer.diaryRepository.waterGlasses(user.id, selectedDate),
            waterGoal = DiaryRepository.WATER_GOAL_GLASSES,
            workoutScheduled = workoutRepository.workoutFor(user.id, selectedDate) != null,
            workoutDone = workoutRepository.logsBetween(user.id, selectedDate, selectedDate).isNotEmpty(),
        )
    }

    /**
     * "Finalizar o dia":
     * - bateu tudo ou quase → salva e mostra os parabéns;
     * - falta bastante → pergunta se tem certeza antes de salvar.
     */
    private fun finishDay() {
        val evaluation = evaluateSelectedDay()
        val dialog = DayResultDialog(this)

        if (evaluation.result == DayResult.FAR_FROM_GOALS) {
            dialog.askConfirmation(evaluation) {
                saveClosing(evaluation)
                showMessage("Dia finalizado e salvo.")
            }
        } else {
            saveClosing(evaluation)
            dialog.showResult(evaluation)
        }
    }

    /** Salva o resumo do dia (refeições, água, treino e resultado). */
    private fun saveClosing(evaluation: DayEvaluation) {
        val entries = appContainer.diaryRepository.entriesFor(user.id, selectedDate)
        val closing = DayClosing(
            date = selectedDate,
            finishedAt = LocalTime.now(),
            consumed = NutritionCalculator.total(entries),
            goals = NutritionCalculator.dailyGoals(user),
            waterGlasses = appContainer.diaryRepository.waterGlasses(user.id, selectedDate),
            workoutDone = appContainer.workoutRepository.logsBetween(user.id, selectedDate, selectedDate).isNotEmpty(),
            goalsMet = evaluation.metCount,
            totalGoals = evaluation.checks.size,
            result = evaluation.result,
        )
        appContainer.diaryRepository.saveClosing(user.id, closing)
        refresh()
    }

    // ================= 8. Treinos =================

    private fun showWorkouts() {
        val repository = appContainer.workoutRepository
        val todayWorkout = repository.workoutFor(user.id, LocalDate.now())
        binding.textTodayWorkout.text = if (todayWorkout == null) {
            "Hoje: dia de descanso 😴"
        } else {
            "Hoje: Treino ${todayWorkout.label} — ${todayWorkout.name}"
        }
        workoutAdapter.submitWorkouts(repository.workouts(user.id), todayWorkout?.id)
    }

    /** Intent EXPLÍCITA para a TELA 3, levando o id e o nome do treino. */
    private fun openWorkout(workout: Workout) {
        workoutLauncher.launch(WorkoutDetailActivity.newIntent(this, workout.id, workout.name))
    }

    // ================= Sair =================

    private fun confirmLogout() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.profile_logout_confirm)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.profile_logout_yes) { _, _ ->
                appContainer.authRepository.logout()
                openLogin()
            }
            .show()
    }

    private fun openLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    companion object {
        private const val KEY_SELECTED_DATE = "selected_date"
        private const val CHART_BAR_AREA_DP = 120
        private const val CHART_LETTER_DP = 24
    }
}
