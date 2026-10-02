package com.gustavobento.mygym.model

/** Refeições do dia, na ordem em que aparecem no diário. */
enum class MealType(val label: String, val emoji: String) {
    BREAKFAST("Café da manhã", "☕"),
    LUNCH("Almoço", "🍛"),
    SNACK("Lanche", "🍎"),
    DINNER("Jantar", "🌙"),
}
