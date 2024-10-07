package hoods.com.jetai.data.models

import android.icu.text.CaseMap.Title
import com.google.firebase.Timestamp
import java.util.UUID

enum class ModelName(val modelNae: String){
    TEXT ("gemini-pro"),
    MULTIMODAL("gemini-pro-vision")
}

enum class Participant {
    USER,MODEL,ERROR
}

data class ChatMessage (
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",   //what the user will input
    val participant: Participant = Participant.USER,
    val timestamp: Timestamp = Timestamp.now(),

    )

data class ChatRoom (
    val id: String = "",
    val title: String = "New Chat",
    val timestamp: Timestamp = Timestamp.now(),
    val userId : String = "",

)