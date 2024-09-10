package hoods.com.jetai.utils

import android.util.Patterns
import java.util.regex.Pattern

fun isValidEmail (email:String):Boolean{
    if (email.isEmpty()) return false
    val emailPattern = Patterns.EMAIL_ADDRESS
    return emailPattern.matcher(email).matches()
}