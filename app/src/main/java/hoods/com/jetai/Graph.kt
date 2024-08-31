package hoods.com.jetai

import android.content.Context
import hoods.com.jetai.repository.AuthRepository
import hoods.com.jetai.repository.AuthRepositoryImpl

object Graph {

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl()
    }

    fun provide(context: Context){

    }
}