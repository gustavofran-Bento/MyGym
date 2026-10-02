package com.gustavobento.mygym.model

/** Sexo biológico: usado apenas no cálculo do gasto calórico (fórmula Mifflin-St Jeor). */
enum class Sex(val label: String) {
    MALE("Masculino"),
    FEMALE("Feminino"),
}

/** Objetivo do usuário e o ajuste de calorias que ele causa na meta diária. */
enum class Goal(val label: String, val calorieAdjustment: Int) {
    LOSE_WEIGHT("Perder peso", -500),
    MAINTAIN("Manter peso", 0),
    GAIN_MUSCLE("Ganhar massa", 300),
}

/**
 * Usuário do app. A senha NÃO fica aqui: ela é guardada separadamente,
 * apenas como hash, dentro do AuthRepository.
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val sex: Sex,
    val age: Int,
    val weightKg: Double,
    val heightCm: Double,
    val goal: Goal,
) {
    val firstName: String
        get() {
            val first = name.trim().split(" ").first()
            return if (first.isBlank()) "atleta" else first
        }

    /** Iniciais para o avatar do perfil. Ex.: "Gustavo Bento" → "GB". */
    val initials: String
        get() {
            val parts = name.trim().split(" ").filter { it.isNotBlank() }
            if (parts.isEmpty()) return "?"
            if (parts.size == 1) return parts[0].take(1).uppercase()
            return (parts.first().take(1) + parts.last().take(1)).uppercase()
        }
}
