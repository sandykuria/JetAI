package hoods.com.jetai.repository

import android.content.Intent
import android.content.IntentSender
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import hoods.com.jetai.BuildConfig
import hoods.com.jetai.utils.Response
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException

class GoogleAuthClient (
    private val oneTapClient:SignInClient
) {
    companion object {
        const val TAG = "google_auth"
    }

    suspend fun signIn():IntentSender?{
        val result = try {
            oneTapClient.beginSignIn(buildSignInRequest()).await()

        }catch (e:Exception){
            e.printStackTrace()
            if (e is CancellationException) throw e
            null
        }
        return result?.pendingIntent?.intentSender
    }

    fun signInWithIntent(intent: Intent)
    :Flow<Result<AuthResult?>> = callbackFlow{
        val credential  = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(
            googleIdToken, null
        )
        try{
            Firebase.auth.signInWithCredential(googleCredentials)
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        trySend(Result.success(it.result))
                    }else{
                        trySend(Result.failure(it.exception ?:Exception("Unknown error")))
                    }
                }
        }catch (e:Exception){
            e.printStackTrace()

        }
        awaitClose()
    }
    private fun buildSignInRequest():BeginSignInRequest{
        return BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(BuildConfig.clientID)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }

}