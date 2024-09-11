package hoods.com.jetai.authentication.register.login

import android.app.Activity.RESULT_OK
import android.provider.Telephony.Mms.Sent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import hoods.com.jetai.authentication.register.components.AlternativeLoginOptions
import hoods.com.jetai.authentication.register.components.HeaderText
import hoods.com.jetai.authentication.register.components.LoadingView
import hoods.com.jetai.authentication.register.components.LoginTextField
import hoods.com.jetai.authentication.register.defaultPadding
import hoods.com.jetai.authentication.register.itemSpacing
import kotlinx.coroutines.launch
import org.w3c.dom.Text

@Composable
fun LoginScreen (
    viewModel: LoginViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    isVerificationEmailSent:Boolean,
    onSignUpClick: () -> Unit,
    navigateToHomeScreen:() -> Unit,
    onForgotPasswordClick: () -> Unit,
    modifier: Modifier = Modifier,
){
    val loginState = viewModel.loginState
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState()}

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = {result ->
            if (result.resultCode == RESULT_OK)
                scope.launch {
                    viewModel.loginEvent(
                        LoginEvents.SignInWithGoogle(result.data ?: return@launch)
                    )
                }
        }
    )
    LaunchedEffect(isVerificationEmailSent ) {
        if (isVerificationEmailSent){
            snackbarHostState.showSnackbar(
                "Verification Email Sent to ${loginState.currentUser?.email}"
            )
        }
    }
    LaunchedEffect (viewModel.hasUserVerified()){
        if (viewModel.hasUserVerified()){
            navigateToHomeScreen()
        }
    }

  Scaffold(
      snackbarHost = {
          SnackbarHost(hostState = snackbarHostState)
      }
  ) { innerPadding ->
      Box(
          modifier = modifier
              .padding(defaultPadding)
              .padding(innerPadding),
          contentAlignment = Alignment.Center
      ){
          Column {
              HeaderText(
                  text = "Login",
                  modifier = Modifier
                      .align(Alignment.Start)
                      .padding(vertical = defaultPadding)
              )
              AnimatedVisibility(
                  loginState.loginErrorMsg != null
              ) {
                  Text (
                      loginState.loginErrorMsg ?: "Unknown",
                      color = MaterialTheme.colorScheme.error
                  )
              }
              AnimatedVisibility(loginState.showResendBtn) {
                  TextButton(
                      onClick = {
                          viewModel.loginEvent(
                              LoginEvents.onResendVerification
                          )
                  }
                  ){
                      Text("Resend Verification")

                  }
              }
              LoginTextField(
                  value = loginState.email ,
                  onValueChange = {
                      viewModel.loginEvent(LoginEvents.onEmailChange(it))
                  },
                  labelText = "UserName",
                  leadingIcon = Icons.Default.Person,
                  modifier = Modifier.fillMaxWidth()
                  )
              Spacer(Modifier.height(itemSpacing))
              LoginTextField(
                  value = loginState.password ,
                  onValueChange = {
                      viewModel.loginEvent(LoginEvents.onPasswordChange(it))
                  },
                  labelText = "UserName",
                  leadingIcon = Icons.Default.Lock,
                  modifier = Modifier.fillMaxWidth(),
                  visualTransformation = PasswordVisualTransformation(),
                  keyboardType = KeyboardType.Password
              )
              Spacer(Modifier.height(itemSpacing))

              Button(
                  onClick = onForgotPasswordClick ,
                  modifier = Modifier.fillMaxWidth()
              ){
                  Text("Login")
              }
              Spacer(Modifier.height(itemSpacing))
              AlternativeLoginOptions(
                  onIconClick = {
                      scope.launch {
                          val googleIntentSender = viewModel.signInWithGoogleIntentSender()
                          launcher.launch(
                              IntentSenderRequest.Builder (
                                  googleIntentSender ?: return@launch
                              ).build()
                          )
                      }

                  },
                  onSignUpClick,
                  modifier
                      .fillMaxSize()
                      .wrapContentSize(
                          align = Alignment.BottomCenter
                      )

              )

          }
      }
      LoadingView(loginState.isLoading)
  }

}