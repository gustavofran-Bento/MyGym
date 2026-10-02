package com.gustavobento.mygym.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Armazenamento local do app, usando SharedPreferences (arquivo privado do app).
 *
 * Os repositórios guardam listas convertidas para texto JSON. Assim os dados
 * continuam salvos depois que o app é fechado. Na Etapa 2 isto será trocado
 * pelo banco Room, sem mudar as telas.
 */
class LocalStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    fun readString(key: String): String? {
        return prefs.getString(key, null)
    }

    fun saveString(key: String, value: String) {
        prefs.edit { putString(key, value) }
    }

    fun readBoolean(key: String): Boolean {
        return prefs.getBoolean(key, false)
    }

    fun saveBoolean(key: String, value: Boolean) {
        prefs.edit { putBoolean(key, value) }
    }

    fun remove(key: String) {
        prefs.edit { remove(key) }
    }

    companion object {
        private const val FILE_NAME = "mygym_dados"
    }
}
