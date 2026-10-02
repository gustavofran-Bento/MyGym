package com.gustavobento.mygym.di

import android.content.Context
import com.gustavobento.mygym.data.local.LocalStorage
import com.gustavobento.mygym.data.mock.DemoData
import com.gustavobento.mygym.data.repository.AuthRepository
import com.gustavobento.mygym.data.repository.DiaryRepository
import com.gustavobento.mygym.data.repository.FoodRepository
import com.gustavobento.mygym.data.repository.WorkoutRepository

/**
 * Injeção de dependências MANUAL: cria uma única instância de cada repositório
 * e as telas pegam daqui (sem cada tela criar o seu próprio).
 */
class AppContainer(context: Context) {

    private val storage = LocalStorage(context)

    val authRepository = AuthRepository(storage)
    val foodRepository = FoodRepository(storage)
    val diaryRepository = DiaryRepository(storage)
    val workoutRepository = WorkoutRepository(storage)

    init {
        DemoData.createIfNeeded(storage, authRepository, diaryRepository, workoutRepository)
    }
}
