package hoods.com.jetai

import android.content.Context
import hoods.com.jetai.repository.AuthRepository
import hoods.com.jetai.repository.AuthRepositoryImpl
import hoods.com.jetai.repository.GoogleAuthClient
import com.google.android.gms.auth.api.identity.Identity

object Graph {

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl()
    }

    lateinit var googleAuthClient:GoogleAuthClient
    fun provide(context: Context){
        googleAuthClient = GoogleAuthClient(
            oneTapClient = Identity.getSignInClient(context)
        )
    }
}