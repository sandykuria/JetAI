package hoods.com.jetai.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavAction
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import hoods.com.jetai.authentication.register.SignUpScreen
import hoods.com.jetai.authentication.register.login.LoginScreen
import hoods.com.jetai.authentication.register.login.LoginViewModel

@Composable
fun JetAiNavGraph (
    modifier: Modifier = Modifier,
    navController: NavHostController,
    navAction: JetAiNavigationActions,
    loginViewModel: LoginViewModel,
    startDestination: String,
){
    NavHost(navController = navController, startDestination = startDestination ){


    }

}

fun NavGraphBuilder.authGraph(
    navAction: JetAiNavigationActions,
    navController: NavHostController,
    loginViewModel: LoginViewModel,
    modifier: Modifier
){
    navigation(
        startDestination = Route.LoginScreen().routeWithArgs,
        route = Route.NESTED_AUTH_ROUTE,
    ){
        composable(
            route = Route.LoginScreen().routeWithArgs,
            arguments = listOf(
                navArgument(name = Route.isEmailSentArg){}
            )
            ) { entry ->
            LoginScreen(
               onSignUpClick = {
                   navAction.navigateToSignUpScreen()
               },
                isVerificationEmailSent = entry.arguments?.getString(Route.isEmailSentArg).toBoolean(),
                onForgotPasswordClick = {
                    navAction.navigateToForgotPasswordScreen()
                },
                navigateToHomeScreen = {
                    navAction.navigateToHomeGraph()
                },
                modifier = modifier

            )

        }
        composable(route = Route.SignupScreen().route){
            SignUpScreen(
                onLoginClick = {
                    navAction.navigateToLoginScreenWithArgs(false)
                },
                onNavigateToLoginScreen = {
                    navAction.navigateToLoginScreenWithArgs(it)
                },
                onBackButtonClicked = {
                    navAction.navigateToLoginScreenWithArgs(false)
                },
                modifier = modifier

            )
        }
        composable(route = Route.ForgotPasswordScreen().route){
            // TODO: Forgot password screen

        }


    }
}