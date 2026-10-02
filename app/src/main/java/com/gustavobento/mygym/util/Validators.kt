package com.gustavobento.mygym.util

/**
 * Validação dos formulários. Cada função retorna a MENSAGEM DE ERRO,
 * ou null quando o valor é válido.
 */
object Validators {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    const val MIN_PASSWORD_LENGTH = 6

    /** Converte texto em número aceitando vírgula ou ponto ("22,5" ou "22.5"). */
    fun parseNumber(text: String?): Double? {
        if (text.isNullOrBlank()) return null
        return text.trim().replace(',', '.').toDoubleOrNull()
    }

    /** Converte um texto JÁ VALIDADO em número (se não for número, devolve 0). */
    fun toNumber(text: String): Double {
        return parseNumber(text) ?: 0.0
    }

    fun validateName(name: String): String? {
        val clean = name.trim()
        if (clean.isEmpty()) return "Informe seu nome"
        if (clean.length < 2) return "Nome muito curto"
        if (clean.length > 60) return "Nome muito longo"
        return null
    }

    fun validateEmail(email: String): String? {
        val clean = email.trim()
        if (clean.isEmpty()) return "Informe seu e-mail"
        if (!EMAIL_REGEX.matches(clean)) return "E-mail inválido"
        return null
    }

    fun validatePassword(password: String): String? {
        if (password.isEmpty()) return "Informe sua senha"
        if (password.length < MIN_PASSWORD_LENGTH) return "A senha precisa ter pelo menos $MIN_PASSWORD_LENGTH caracteres"
        return null
    }

    fun validatePasswordConfirmation(password: String, confirmation: String): String? {
        if (confirmation.isEmpty()) return "Confirme sua senha"
        if (password != confirmation) return "As senhas não são iguais"
        return null
    }

    fun validateAge(text: String): String? {
        val age = parseNumber(text) ?: return "Informe sua idade"
        if (age % 1.0 != 0.0) return "Use um número inteiro"
        if (age < 12 || age > 100) return "Idade entre 12 e 100 anos"
        return null
    }

    fun validateWeight(text: String): String? {
        val weight = parseNumber(text) ?: return "Informe seu peso"
        if (weight < 30 || weight > 300) return "Peso entre 30 e 300 kg"
        return null
    }

    fun validateHeight(text: String): String? {
        val height = parseNumber(text) ?: return "Informe sua altura"
        if (height < 100 || height > 250) return "Altura entre 100 e 250 cm"
        return null
    }

    fun validateFoodName(name: String): String? {
        val clean = name.trim()
        if (clean.isEmpty()) return "Informe o nome do alimento"
        if (clean.length < 2) return "Nome muito curto"
        if (clean.length > 40) return "Máximo de 40 caracteres"
        return null
    }

    /** Quantidade em gramas de uma porção: maior que 0 e até 2 kg. */
    fun validateGrams(text: String): String? {
        val grams = parseNumber(text) ?: return "Informe a quantidade"
        if (grams <= 0) return "A quantidade deve ser maior que zero"
        if (grams > 2000) return "Máximo de 2000 g"
        return null
    }

    /** Macronutriente em 100 g: não pode ser negativo nem passar de 100 g. */
    fun validateMacroPer100g(text: String): String? {
        val value = parseNumber(text) ?: return "Obrigatório (use 0 se não tiver)"
        if (value < 0) return "Não pode ser negativo"
        if (value > 100) return "Máximo de 100 g em 100 g"
        return null
    }

    fun validateWorkoutName(name: String): String? {
        val clean = name.trim()
        if (clean.isEmpty()) return "Informe o nome do treino"
        if (clean.length > 30) return "Máximo de 30 caracteres"
        return null
    }

    fun validateExerciseName(name: String): String? {
        val clean = name.trim()
        if (clean.isEmpty()) return "Informe o exercício"
        if (clean.length > 40) return "Máximo de 40 caracteres"
        return null
    }

    /** Número inteiro dentro de um intervalo (séries, repetições). */
    fun validateWholeNumber(text: String, min: Int, max: Int): String? {
        val value = parseNumber(text) ?: return "Obrigatório"
        if (value % 1.0 != 0.0) return "Use número inteiro"
        if (value < min || value > max) return "Entre $min e $max"
        return null
    }

    /** Carga em kg: 0 (peso corporal) até 500 kg. */
    fun validateLoad(text: String): String? {
        val value = parseNumber(text) ?: return "Use 0 p/ peso corporal"
        if (value < 0) return "Não pode ser negativa"
        if (value > 500) return "Máximo de 500 kg"
        return null
    }

    /** Em 100 g de alimento, proteína + carboidrato + gordura não passam de 100 g. */
    fun validateMacroSum(protein: Double, carbs: Double, fat: Double): String? {
        if (protein + carbs + fat > 100.0) return "A soma dos macros passa de 100 g"
        return null
    }
}
