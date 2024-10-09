package hoods.com.jetai.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import hoods.com.jetai.Graph
import hoods.com.jetai.data.models.ChatMessage
import hoods.com.jetai.data.models.ChatRoom
import hoods.com.jetai.data.models.ModelName
import hoods.com.jetai.data.models.Participant
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

//Sealed class to represent different types of responses ie loading...success
sealed class Response<out T> {
    object Loading : Response<Nothing>()
    data class Success<out T>(val dataSuccess: T) : Response<T>()
    data class Error(val throwable: Throwable?) : Response<Nothing>()
}
//Repository to handle chat operations
class ChatRepository(
    //Generative AI model for handling chat messages
    private val generativeModel: GenerativeModel = Graph.generativeModel(ModelName.TEXT.modelNae), // fixed modelName
    private val auth: FirebaseAuth = Firebase.auth,       //Firebase auth to get current user
    db: FirebaseFirestore = Firebase.firestore,          //Firestore db instance
) {
    private var chat = generativeModel.startChat()                   //start a new session with generative ai model
    private val chatRef = db.collection(USER_CHATS_COLLECTION)        /* Reference to the user_chats collection in Firestore*/

    companion object {
        const val USER_CHATS_COLLECTION = "user_chats"              /* collection for user chats*/
        const val CHAT_COLLECTION = "chats"                         /* *sub-collection for chats*/
        const val MESSAGE_SUB_COLLECTION = "messages"               /*sub-collection for chat messages */
    }

    suspend fun createChatRoom(): String? {
        val documentId = chatRef.document().id
        return try {
            auth.currentUser?.uid?.let {
                chatRef.document(documentId)
                    .set(ChatRoom(id = documentId, userId = it))
                    .await()
                documentId
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getChatRoomList(): Flow<List<ChatRoom>> = callbackFlow {
        var snapshotListener: ListenerRegistration? = null
        try {
            snapshotListener = chatRef
                .orderBy("timestamp")
                .whereEqualTo("userId", auth.currentUser?.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList()).isSuccess
                        return@addSnapshotListener
                    }

                    val chatRooms = if (snapshot != null && !snapshot.isEmpty) {
                        snapshot.toObjects(ChatRoom::class.java)
                    } else {
                        emptyList()
                    }

                    trySend(chatRooms).isSuccess
                }
        } catch (e: Exception) {
            trySend(emptyList()).isSuccess
        }

        awaitClose { snapshotListener?.remove() }
    }

    suspend fun sendMessage(userPrompt: String, chatId: String): Flow<Response<Unit>> = flow {
        try {
            saveChat(ChatMessage(text = userPrompt), chatId)
            emit(Response.Loading)

            val response = chat.sendMessage(userPrompt)
            if (response.text != null) {
                val chatMessage = ChatMessage(
                    text = response.text!!,
                    participant = Participant.MODEL
                )
                saveChat(chatMessage, chatId)
                emit(Response.Success(Unit))
            } else {
                emit(Response.Error(Throwable("Response text was null")))
            }
        } catch (e: Exception) {
            saveChat(
                ChatMessage(
                    text = e.localizedMessage ?: "An error occurred",
                    participant = Participant.ERROR
                ),
                chatId = chatId
            )
            emit(Response.Error(e))
            e.printStackTrace()
        }
    }

    suspend fun fetchHistoryMsg(chatId: String, title: String) {
        if (title == "New Chat") {
            try {
                val messages = chatRef.document(chatId)
                    .collection(MESSAGE_SUB_COLLECTION)
                    .orderBy("timestamp")
                    .limit(5)
                    .get()
                    .await()
                    .toObjects(ChatMessage::class.java)

                if (messages.isNotEmpty()) {
                    val chatTitle = chat.sendMessage("Give me one best title for this content $messages")
                    chatRef.document(chatId)
                        .update("title", chatTitle)
                        .await()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun getMessage(chatId: String): Flow<Response<List<ChatMessage>>> = callbackFlow {
        var snapshotListener: ListenerRegistration? = null
        trySend(Response.Loading)
        try {
            snapshotListener = chatRef.document(chatId)
                .collection(MESSAGE_SUB_COLLECTION)
                .orderBy("timestamp")
                .addSnapshotListener { snapshot, error ->
                    val response = if (snapshot != null) {
                        val messages = snapshot.toObjects(ChatMessage::class.java)
                        Response.Success(messages)
                    } else {
                        Response.Error(error?.cause)
                    }
                    trySend(response).isSuccess
                }
        } catch (e: Exception) {
            trySend(Response.Error(e.cause)).isSuccess
        }

        awaitClose { snapshotListener?.remove() }
    }

    suspend fun saveChat(chatMessage: ChatMessage, chatId: String) {
        chatRef.document(chatId)
            .collection(MESSAGE_SUB_COLLECTION)
            .add(chatMessage)
            .await()
    }
}
