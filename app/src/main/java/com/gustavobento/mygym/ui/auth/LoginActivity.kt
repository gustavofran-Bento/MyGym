package com.gustavobento.mygym.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.gustavobento.mygym.R
import com.gustavobento.mygym.appContainer
import com.gustavobento.mygym.data.mock.DemoData
import com.gustavobento.mygym.databinding.ActivityLoginBinding
import com.gustavobento.mygym.model.Goal
import com.gustavobento.mygym.model.Sex
import com.gustavobento.mygym.ui.diary.DiaryActivity
import com.gustavobento.mygym.util.Formatters
import com.gustavobento.mygym.util.NutritionCalculator
import com.gustavobento.mygym.util.Validators
import com.gustavobento.mygym.util.applySystemBarsPadding

/**
 * TELA 1 — Login e cadastro na mesma tela.
 * O seletor "Entrar / Criar conta" mostra ou esconde os campos do cadastro.
 * Se já existir um usuário logado, vai direto para o Diário.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    /** true = modo "Criar conta"; false = modo "Entrar". */
    private var isRegisterMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Sessão salva: não precisa logar de novo
        if (appContainer.authRepository.currentUser() != null) {
            openDiary()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.scrollView.applySystemBarsPadding()

        binding.toggleMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) showMode(checkedId == R.id.buttonModeRegister)
        }
        binding.buttonSubmit.setOnClickListener { submit() }
        binding.buttonDemo.setOnClickListener {
            binding.toggleMode.check(R.id.buttonModeLogin)
            binding.inputEmail.setText(DemoData.DEMO_EMAIL)
            binding.inputPassword.setText(DemoData.DEMO_PASSWORD)
        }

        // Botão "✓" do teclado no campo de senha também envia (no modo Entrar)
        binding.inputPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE && !isRegisterMode) {
                submit()
                true
            } else {
                false
            }
        }

        // Prévia da meta atualiza a cada mudança nos dados do corpo ou objetivo
        binding.inputAge.doAfterTextChanged { updateGoalPreview() }
        binding.inputWeight.doAfterTextChanged { updateGoalPreview() }
        binding.inputHeight.doAfterTextChanged { updateGoalPreview() }
        binding.toggleSex.addOnButtonCheckedListener { _, _, _ -> updateGoalPreview() }
        binding.toggleGoal.addOnButtonCheckedListener { _, _, _ -> updateGoalPreview() }

        // Limpa a mensagem de erro assim que o usuário volta a digitar
        binding.inputName.doAfterTextChanged { binding.layoutName.error = null }
        binding.inputEmail.doAfterTextChanged { binding.layoutEmail.error = null }
        binding.inputPassword.doAfterTextChanged { binding.layoutPassword.error = null }
        binding.inputPasswordConfirm.doAfterTextChanged { binding.layoutPasswordConfirm.error = null }

        // Depois de girar a tela, o seletor já volta marcado; só ajustamos os campos
        showMode(binding.toggleMode.checkedButtonId == R.id.buttonModeRegister)
    }

    /** Alterna entre "Entrar" e "Criar conta". */
    private fun showMode(register: Boolean) {
        isRegisterMode = register
        val visibility = if (register) View.VISIBLE else View.GONE
        binding.layoutName.visibility = visibility
        binding.layoutRegister.visibility = visibility
        binding.buttonDemo.visibility = if (register) View.GONE else View.VISIBLE

        if (register) {
            binding.textTitle.text = getString(R.string.register_title)
            binding.textSubtitle.text = getString(R.string.register_subtitle)
            binding.buttonSubmit.text = getString(R.string.register_button)
            binding.layoutPassword.helperText = "Mínimo de ${Validators.MIN_PASSWORD_LENGTH} caracteres"
        } else {
            binding.textTitle.text = getString(R.string.login_title)
            binding.textSubtitle.text = getString(R.string.login_subtitle)
            binding.buttonSubmit.text = getString(R.string.login_button)
            binding.layoutPassword.helperText = null
        }
        binding.layoutEmail.error = null
        binding.layoutPassword.error = null
        updateGoalPreview()
    }

    private fun submit() {
        if (isRegisterMode) register() else login()
    }

    // ================= Entrar =================

    private fun login() {
        val email = binding.inputEmail.text.toString()
        val password = binding.inputPassword.text.toString()

        // 1) Valida os campos e mostra o erro embaixo de cada um
        binding.layoutEmail.error = Validators.validateEmail(email)
        binding.layoutPassword.error = Validators.validatePassword(password)
        if (binding.layoutEmail.error != null || binding.layoutPassword.error != null) return

        // 2) Confere e-mail e senha
        val user = appContainer.authRepository.login(email, password)
        if (user == null) {
            binding.layoutPassword.error = getString(R.string.login_error)
            return
        }
        openDiary()
    }

    // ================= Criar conta =================

    private fun selectedSex(): Sex {
        return if (binding.toggleSex.checkedButtonId == R.id.buttonFemale) Sex.FEMALE else Sex.MALE
    }

    private fun selectedGoal(): Goal {
        return when (binding.toggleGoal.checkedButtonId) {
            R.id.buttonGoalLose -> Goal.LOSE_WEIGHT
            R.id.buttonGoalGain -> Goal.GAIN_MUSCLE
            else -> Goal.MAINTAIN
        }
    }

    /** Calcula e mostra a meta diária quando idade, peso e altura são válidos. */
    private fun updateGoalPreview() {
        val ageText = binding.inputAge.text.toString()
        val weightText = binding.inputWeight.text.toString()
        val heightText = binding.inputHeight.text.toString()

        val allValid = Validators.validateAge(ageText) == null &&
            Validators.validateWeight(weightText) == null &&
            Validators.validateHeight(heightText) == null

        if (!allValid) {
            binding.textGoalCalories.text = "— kcal"
            binding.textGoalMacros.text = getString(R.string.register_preview_empty)
            return
        }

        val goals = NutritionCalculator.dailyGoals(
            sex = selectedSex(),
            age = Validators.toNumber(ageText).toInt(),
            weightKg = Validators.toNumber(weightText),
            heightCm = Validators.toNumber(heightText),
            goal = selectedGoal(),
        )
        binding.textGoalCalories.text = "${Formatters.integer(goals.calories)} kcal"
        binding.textGoalMacros.text = "Proteína ${Formatters.integer(goals.protein)} g • " +
            "Carbo ${Formatters.integer(goals.carbs)} g • Gordura ${Formatters.integer(goals.fat)} g"
    }

    private fun register() {
        val name = binding.inputName.text.toString()
        val email = binding.inputEmail.text.toString()
        val password = binding.inputPassword.text.toString()
        val confirmation = binding.inputPasswordConfirm.text.toString()
        val ageText = binding.inputAge.text.toString()
        val weightText = binding.inputWeight.text.toString()
        val heightText = binding.inputHeight.text.toString()

        // Mostra o erro de TODOS os campos de uma vez
        binding.layoutName.error = Validators.validateName(name)
        binding.layoutEmail.error = Validators.validateEmail(email)
        binding.layoutPassword.error = Validators.validatePassword(password)
        binding.layoutPasswordConfirm.error = Validators.validatePasswordConfirmation(password, confirmation)
        binding.layoutAge.error = Validators.validateAge(ageText)
        binding.layoutWeight.error = Validators.validateWeight(weightText)
        binding.layoutHeight.error = Validators.validateHeight(heightText)

        val hasError = binding.layoutName.error != null ||
            binding.layoutEmail.error != null ||
            binding.layoutPassword.error != null ||
            binding.layoutPasswordConfirm.error != null ||
            binding.layoutAge.error != null ||
            binding.layoutWeight.error != null ||
            binding.layoutHeight.error != null
        if (hasError) return

        val user = appContainer.authRepository.register(
            name = name,
            email = email,
            password = password,
            sex = selectedSex(),
            age = Validators.toNumber(ageText).toInt(),
            weightKg = Validators.toNumber(weightText),
            heightCm = Validators.toNumber(heightText),
            goal = selectedGoal(),
        )
        if (user == null) {
            binding.layoutEmail.error = getString(R.string.register_email_taken)
            return
        }
        openDiary()
    }

    /** Intent explícita para a tela principal. Limpa a pilha: "voltar" no Diário fecha o app. */
    private fun openDiary() {
        val intent = Intent(this, DiaryActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
