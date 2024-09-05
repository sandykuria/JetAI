package hoods.com.jetai.authentication.register

import android.text.Layout
import androidx.compose.ui.Alignment
import android.widget.Button
import androidx.compose.material3.Button
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.provider.FontsContractCompat.Columns
import hoods.com.jetai.authentication.components.HeaderText
import hoods.com.jetai.authentication.components.LoadingView
import hoods.com.jetai.authentication.components.LoginTextField

val defaultPadding = 16.dp
val itemSpacing = 8.dp


@Composable
fun SignUpScreen(
    onLoginClick:() -> Unit,
    onNavigateToLoginScreen: (Boolean) -> Unit,
    onBackButtonClicked:() -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignUpViewModel = viewModel()
) {
    val signUpState = viewModel.signUpState
    val context = LocalContext.current
    LaunchedEffect(signUpState.isVerificationEmailSent) {
        if (signUpState.isVerificationEmailSent){
            onNavigateToLoginScreen(true)
            viewModel.signUpEvents(SignUpEvents.OnIsEmailVerificationChange)
        }
    }
    BackHandler {
        onBackButtonClicked()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(defaultPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        //Handle potential error messages
        AnimatedVisibility(signUpState.loginErrorMsg!=null) {
            Text(
                text = signUpState.loginErrorMsg ?: "Unknown error",
                color = MaterialTheme.colorScheme.error
            )
        }
        //Header Text
        HeaderText(
            text = "Sign Up",
            modifier = Modifier
                .padding(vertical = defaultPadding)
                .align(alignment = Alignment.Start)
        )
        //First Name TextField
        LoginTextField(
            value = signUpState.firstName,
            onValueChange = {viewModel.signUpEvents(SignUpEvents.onFirstNameChange(it))} ,
            labelText = "First Name",
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(defaultPadding))

        //Privacy and policy agreement row
        Row (
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ){
            val privacyText = "Privacy"
            val policyText = "Policy"
            val annotatedString = buildAnnotatedString {
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.onBackground)) {
                    append("I agree with")
                }
                append("")
                withStyle(
                    SpanStyle(
                    color = MaterialTheme.colorScheme.primary
                )
                ) {
                    pushStringAnnotation(tag = privacyText, privacyText)
                    append(privacyText)
                }


                withStyle(SpanStyle(color = MaterialTheme.colorScheme.onBackground)) {
                    append("And")
                }

                withStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.primary
                    )
                ) {
                    pushStringAnnotation(tag = policyText, policyText)
                    append(policyText)
                }

            }
            Checkbox(
                checked = signUpState.agreeTerms,
                onCheckedChange = {viewModel.signUpEvents(SignUpEvents.onAgreeTermsChange(it))}
                )
            ClickableText(
                text = annotatedString,
                onClick = { offset ->
                    annotatedString.getStringAnnotations(offset, offset).forEach {
                        when (it.tag) {
                            privacyText -> {
                                Toast.makeText(context, "Privacy Text Clicked", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            policyText -> {
                                Toast.makeText(context, "Policy Text Clicked", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                }
            )

        }

        }
        Spacer(Modifier.height(defaultPadding + 8.dp))
        Button(onClick = {
            viewModel.signUpEvents(SignUpEvents.SignUp)
        },
            modifier = Modifier.fillMaxWidth()
        ){
            Text(text = "Sign Up")
        }
        Spacer(Modifier.height(defaultPadding))
        val signTxt = "Sign In"
    val signInAnnotation = buildAnnotatedString {
        withStyle(
            SpanStyle(
                color = MaterialTheme.colorScheme.onBackground
            )
        ) {
            append("Already have an account?")
        }
        append("")
        withStyle(
            SpanStyle(color = MaterialTheme.colorScheme.primary)
        ) {
            pushStringAnnotation(signTxt, signTxt)
            append(signTxt)
        }
    }

    ClickableText(
        text = signInAnnotation,
        onClick = { offset ->
            signInAnnotation.getStringAnnotations(start = offset, end = offset).firstOrNull()?.let { annotation ->
                if (annotation.tag == signTxt) {
                    onLoginClick()
                }
            }
        }


    )
Column(
    modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(defaultPadding),
    horizontalAlignment = Alignment.CenterHorizontally){
    LoadingView(signUpState.isLoading)
}

}

