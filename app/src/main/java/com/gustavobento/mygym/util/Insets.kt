package com.gustavobento.mygym.util

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * No Android 15+ o app desenha atrás da barra de status e de navegação
 * (edge-to-edge). Esta função adiciona o espaço dessas barras como padding
 * para o conteúdo não ficar escondido. O teclado (IME) também é considerado.
 */
fun View.applySystemBarsPadding(applyTop: Boolean = true, applyBottom: Boolean = true) {
    val startLeft = paddingLeft
    val startTop = paddingTop
    val startRight = paddingRight
    val startBottom = paddingBottom

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val types = WindowInsetsCompat.Type.systemBars() or
            WindowInsetsCompat.Type.displayCutout() or
            WindowInsetsCompat.Type.ime()
        val bars = insets.getInsets(types)
        view.setPadding(
            startLeft + bars.left,
            if (applyTop) startTop + bars.top else startTop,
            startRight + bars.right,
            if (applyBottom) startBottom + bars.bottom else startBottom,
        )
        insets
    }
}
