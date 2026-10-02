package com.gustavobento.mygym.ui.components

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.gustavobento.mygym.R

/**
 * Mostra uma janela (diálogo) com um formulário.
 * O botão de confirmar só fecha a janela quando [onConfirm] retorna true,
 * ou seja, quando os dados digitados são válidos.
 */
fun showFormDialog(context: Context, title: String, content: View, confirmText: String, onConfirm: () -> Boolean) {
    val dialog = MaterialAlertDialogBuilder(context)
        .setTitle(title)
        .setView(content)
        .setNegativeButton(R.string.cancel, null)
        .setPositiveButton(confirmText, null) // o clique é tratado abaixo para poder validar
        .create()
    dialog.show()
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
        if (onConfirm()) dialog.dismiss()
    }
}
