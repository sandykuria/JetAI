package hoods.com.jetai.navigation

import android.graphics.drawable.Icon
import android.icu.text.CaseMap.Title
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.PhotoFilter
import androidx.compose.ui.graphics.vector.ImageVector
import io.grpc.NameResolver.Args

sealed class Route {
    companion object {
        const val NESTED_AUTH_ROUTE = "auth_route"
        const val NESTED_HOME_ROUTE ="auth_route"
        const val isEmailSentArg = "isEmailSent"
    }

    data class LoginScreen (
        val route: String = "login",
        val routeWithArgs: String = "$route/{$isEmailSentArg}",
    ) : Route() {
        fun getRouteWithArgs(isEmailVerified:Boolean = false):String {
            return "$route/$isEmailVerified"
        }
    }

    data class SignupScreen(val route: String = "signup"):Route()
    data class ForgotPasswordScreen(val route: String = "Forgot password"):Route()

}

enum class Tabs(val title: String, val icon: ImageVector){
    Chats(title = "Chit Chat", icon = Icons.Default.ChatBubble),
    VisualIq(title = "Visual IQ", icon = Icons.Default.PhotoFilter),
}