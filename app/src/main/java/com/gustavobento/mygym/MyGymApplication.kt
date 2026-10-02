package com.gustavobento.mygym

import android.app.Application
import android.content.Context
import com.gustavobento.mygym.di.AppContainer

/**
 * Classe Application: criada uma única vez quando o app inicia.
 * Guarda o [AppContainer] com os repositórios usados pelas telas.
 */
class MyGymApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

/** Atalho: `appContainer` dentro de qualquer Activity ou Fragment (via context). */
val Context.appContainer: AppContainer
    get() = (applicationContext as MyGymApplication).container
