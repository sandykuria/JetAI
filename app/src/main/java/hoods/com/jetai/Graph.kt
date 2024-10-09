package hoods.com.jetai

import android.content.Context
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import hoods.com.jetai.repository.AuthRepository
import hoods.com.jetai.repository.AuthRepositoryImpl
import hoods.com.jetai.repository.GoogleAuthClient
import com.google.android.gms.auth.api.identity.Identity
import hoods.com.jetai.repository.ChatRepository

object Graph {

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl()
    }
    val chatRepository: ChatRepository by lazy {
        ChatRepository()
    }

    lateinit var googleAuthClient: GoogleAuthClient
    private val config = generationConfig {
        temperature = .7f
    }

    // Define the generative model that we can use throughout the app
    fun generativeModel(modelName : String) = GenerativeModel (
        modelName = modelName,
        apiKey = BuildConfig.api_key,
        generationConfig = config
    )

    fun provide(context: Context){
        googleAuthClient = GoogleAuthClient(
            oneTapClient = Identity.getSignInClient(context)
        )
    }
}