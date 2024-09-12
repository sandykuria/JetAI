package hoods.com.jetai.authentication.register.forgot_password

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hoods.com.jetai.authentication.register.components.LoginTextField
import hoods.com.jetai.authentication.register.defaultPadding
import hoods.com.jetai.navigation.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Route.ForgotPasswordScreen(
    forgotPasswordViewmodel: ForgotPasswordViewmodel = viewModel(),
    onBackClick: () -> Unit
){
    val state = forgotPasswordViewmodel.forgotPasswordState
    Scaffold (
        topBar = {
            TopAppBar(
                title =  { Text(text = "Forgot Password")},
                navigationIcon = { IconButton(onClick = onBackClick) {
                    androidx.compose.material3.Icon(Icons.Default.ArrowBack, contentDescription = "Navigate Back" )
                    
                }}
            )
        }
    ){innerPadding ->
        Column (
            modifier = Modifier
                .padding(defaultPadding)
                .padding(innerPadding)
        ) {
            AnimatedVisibility(state.errorMsg != null) {
                Text(
                    state.errorMsg ?: "Unknown",
                    color = MaterialTheme.colorScheme.error,
                )
            }
            LoginTextField(
                value = state.email ,
                onValueChange = {
                    forgotPasswordViewmodel.forgotPasswordEvent(
                        ForgotPasswordEvent.onEmailChange(it)
                    )
                },
                labelText = "Email",
                modifier = Modifier.fillMaxWidth(),
                keyboardType = KeyboardType.Email
                )
            Text(
                "Your confirmation link will be sent to your email",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = .5f
                )
            )
            Spacer(Modifier.height(32.dp))
            Button(onClick = {
                forgotPasswordViewmodel.forgotPasswordEvent(
                    ForgotPasswordEvent.SendForgotPasswordLink
                )
            },
                modifier = Modifier.fillMaxSize()
            ) {
                Text("Send")

            }
        }

    }
}