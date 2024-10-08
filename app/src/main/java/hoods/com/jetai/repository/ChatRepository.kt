package hoods.com.jetai.repository


import android.app.VoiceInteractor.Prompt
import android.icu.text.CaseMap.Title
import android.location.GnssAntennaInfo.Listener
import android.view.textclassifier.ConversationActions.Message
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.android.gms.common.api.Response
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.firestore.toObjects
import com.google.rpc.context.AttributeContext
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

//Sealed class to rep different states
sealed class Result<out T> {
    object Loading : Result<Nothing>()
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
}


class ChatRepository(
    private val generativeModel: GenerativeModel = Graph.generativeModel(ModelName.TEXT.modelNae),
    private val auth : FirebaseAuth = Firebase.auth,
    db: FirebaseFirestore = Firebase.firestore
){
    private val chat = generativeModel.startChat()
    private val chatRef =db.collection(USER_CHATS_COLLECTION)
    companion object{
        const val USER_CHATS_COLLECTION = "User_chats"
        const val CHAT_COLLECTION = "chats"
        const val MESSAGE_SUB_COLLECTION = "messages"
    }
    //creating a new chatroom
    suspend fun createChatRoom (): String? {
        val documentId = chatRef.document().id
        return try {
            auth.currentUser?.uid?.let {
               chatRef.document(documentId)
                   .set(ChatRoom(id = documentId, userId = it))
                   .await()
                documentId
            }
            ""
        }catch (e:Exception){
            e.printStackTrace()
            null
        }
    }

    //List of chat rooms
   suspend fun getChatRoomList(): Flow<Result<List<ChatRoom>>> = callbackFlow {
       var snapshotListener : ListenerRegistration? = null
       try {
           trySend(Result.Loading)  //send loading state
           snapshotListener = chatRef
               .orderBy("timestamp")
               .whereEqualTo("userId", auth.currentUser?.uid)
               .addSnapshotListener {snapShot,error ->
                   val response = if (snapShot != null){
                       val chatRooms = snapShot.toObjects(ChatRoom::class.java)
                       Result.Success(chatRooms)
                   }else {
                       Result.Error(error ?:Exception("Unknown error"))
                   }
               }
       }catch (e:Exception){
           trySend(Result.Error(e)).isSuccess
       }
       awaitClose{
           snapshotListener?.remove()
       }
   }

    suspend fun sendMessage(userPrompt: String, chatId: String): Flow <Result<Unit>> = flow {
        try{
            saveChat(
                ChatMessage(text = userPrompt),
                chatId
            )
            emit(Result.Loading)
            val response = chat.sendMessage(userPrompt)
            response.text?.let {modelResponse ->
                val chatMessage = ChatMessage (
                    text = modelResponse,
                    participant = Participant.MODEL,
                )
                saveChat(chatMessage, chatId)
                emit(Result.Success(Unit))

            }

        }catch (e:Exception){
            saveChat(
                ChatMessage(
                    text = e.localizedMessage ?: "Error Occured",
                    participant = Participant.ERROR
                ),
                chatId
            )
            emit(Result.Error(e))
            e.printStackTrace()

        }
    }

    suspend fun fetchHistoryMsg(chatId: String, title: String){
        if (title == "New Chat"){
            try {
                val msg = chatRef.document(chatId)
                    .collection(MESSAGE_SUB_COLLECTION)
                    .orderBy("timestamp")
                    .limit(5).get().await().toObjects(ChatMessage :: class.java)
                if(msg.isNotEmpty()){
                    val chatTitle = chat.sendMessage("Give me one best title for this content $msg")
                    chatRef.document(chatId)
                        .update("title", chatTitle)
                        .await()
                }
            } catch (e:Exception){
                e.printStackTrace()
            }
        }
    }
    //Get messages from firestore
    suspend fun getMessage(chatId: String): Flow<Result<List<ChatMessage>>> = callbackFlow {
        var snapShotListener : ListenerRegistration? = null
        trySend(Result.Loading).isSuccess
        try {
            snapShotListener = chatRef.document(chatId)
                .collection(MESSAGE_SUB_COLLECTION)
                .orderBy("timestamp")
                .addSnapshotListener {snapShot, error ->
                    val result = if ( snapShot != null){
                        val message = snapShot.toObjects(ChatMessage::class.java)
                        Result.Success(message)
                    } else {
                        Result.Error(error?:Exception ("Unknown error") )
                    }
                    trySend(result)
                }

        }catch (e:Exception){
            trySend(Result.Error(e)).isSuccess
        }
        awaitClose { snapShotListener?.remove() }

    }


    suspend fun saveChat(chatMessage: ChatMessage,chatId: String){
        chatRef.document(chatId)
            .collection(MESSAGE_SUB_COLLECTION)
            .add(chatMessage)
            .await()
    }

}
