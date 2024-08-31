package hoods.com.jetai.authentication.register

import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import hoods.com.jetai.Graph
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import hoods.com.jetai.repository.AuthRepository
import hoods.com.jetai.utils.Response
import hoods.com.jetai.utils.ext.collectAndHandle
import kotlinx.coroutines.launch

class SignUpViewModel(
    private val repository: AuthRepository = Graph.authRepository
) :ViewModel(){
    var signUpState by mutableStateOf(SignUpState())
        private set

    fun signUpEvents(signUpEvents: SignUpEvents) {
        when (signUpEvents){

            is SignUpEvents.onEmailChange -> {
                signUpState = signUpState.copy(
                    email = signUpEvents.email
                )
            }
            is SignUpEvents.onFirstNameChange -> {
                signUpState = signUpState.copy(
                    firstName = signUpEvents.firstName
                )
            }
            is SignUpEvents.onLastNameChange -> {
                signUpState = signUpState.copy(
                    lastName = signUpEvents.lastName
                )
            }
            is SignUpEvents.onPasswordChange -> {
                signUpState = signUpState.copy(
                    password = signUpEvents.password
                )
            }
            is SignUpEvents.onConfirmPaswordChange -> {
                signUpState = signUpState.copy(
                    confirmPassword = signUpEvents.confirmPassword
                )
            }
            is SignUpEvents.onAgreeTermsChange -> {
                signUpState = signUpState.copy(
                    agreeTerms = signUpEvents.agree
                )
            }
            is SignUpEvents.SignUp -> {
                //todo create user
            }
            is SignUpEvents.OnIsEmailVerificationChange -> {
                signUpState = signUpState.copy(
                    isVerificationEmailSent = false
                )
            }
        }
    }
    private fun validateSignUpForm() = signUpState.run {
        firstName.isNotEmpty() && lastName.isNotEmpty() && email.isNotEmpty()
                && password.isNotEmpty() &&confirmPassword.isNotEmpty() &&agreeTerms
    }


    private fun createUser() = viewModelScope.launch {
        try {
            val isNotSamePassword: Boolean = signUpState.password != signUpState.confirmPassword
            if (!validateSignUpForm()) throw IllegalArgumentException("Fields cannot be empty")
            if (isNotSamePassword) throw IllegalArgumentException("Password do not match")
            signUpState = signUpState.copy(
                isLoading = true,
                loginErrorMsg = null
            )
            repository.createUser(signUpState.email, signUpState.password).collectAndHandle(
                onError = {
                    signUpState = signUpState.copy(
                        isSuccessLogin = false,
                        isLoading = false
                    )
                    throw it ?: Throwable("Unknown Error")
                },
                onLoading = {
                    signUpState = signUpState.copy(
                        isLoading = true
                    )
                }
            ) {
                signUpState = signUpState.copy(isSuccessLogin = true)
                sendVerificationEmail()
            }
        } catch (e: Exception) {
            signUpState = signUpState.copy(
                loginErrorMsg = e.localizedMessage
            )
        }finally {
            signUpState = signUpState.copy(
                isLoading = false
            )
        }
    }
    private fun sendVerificationEmail() = repository.sendVerificationEmail(
        onSuccess = {signUpState = signUpState.copy(isVerificationEmailSent = true)},
        onError = {throw it ?: Throwable("Unknown Error")}
    )
}

data class SignUpState (
    val firstName:String = "",
    val lastName:String = " ",
    val email:String = "",
    val password:String ="",
    val confirmPassword:String = "",
    val agreeTerms:Boolean = false,
    val isLoading:Boolean = false,
    val isSuccessLogin:Boolean = false,
    val isVerificationEmailSent:Boolean = false,
    val loginErrorMsg:String? = null,
)

sealed class SignUpEvents {
    data class onEmailChange(val email:String):SignUpEvents()
    data class onFirstNameChange(val firstName: String):SignUpEvents()
    data class onLastNameChange(val lastName:String):SignUpEvents()
    data class onPasswordChange(val password:String):SignUpEvents()
    data class onConfirmPaswordChange(val confirmPassword:String):SignUpEvents()
    data class onAgreeTermsChange(val agree:Boolean):SignUpEvents()
    data object SignUp :SignUpEvents()
    data object OnIsEmailVerificationChange :SignUpEvents()

}