package hoods.com.jetai.utils.ext

import android.util.Log
import hoods.com.jetai.utils.Response
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import java.lang.Error

suspend fun <T> Flow<Response<T>>.collectAndHandle (
    onError : (Throwable?) -> Unit = {
        Log.e("collectAndHandle", "collectAndHandle: error", it)
    },
    onLoading: () -> Unit = {},
    stateReducer:(T) -> Unit
    ){
    collectLatest {response ->
        when(response){
            is Response.Error -> {
                onError(response.error)
            }
            is Response.Success -> {
                stateReducer(response.dataSuccess)
            }
            is Response.Loading -> {
                onLoading()
            }
        }
    }
}