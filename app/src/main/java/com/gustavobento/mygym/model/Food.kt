package com.gustavobento.mygym.model

/**
 * Alimento do catálogo ou cadastrado pelo usuário.
 * Os valores nutricionais são sempre referentes a 100 g.
 *
 * @property defaultGrams porção sugerida ao adicionar (ex.: 1 ovo ≈ 50 g).
 * @property isCustom     true quando foi cadastrado pelo próprio usuário.
 */
data class Food(
    val id: String,
    val name: String,
    val per100g: Macros,
    val defaultGrams: Double = 100.0,
    val isCustom: Boolean = false,
)
