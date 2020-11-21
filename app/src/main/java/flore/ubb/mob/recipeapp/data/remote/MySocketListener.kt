package flore.ubb.mob.recipeapp.data.remote

import android.util.JsonToken
import android.util.Log
import android.util.Log.d
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import flore.ubb.mob.recipeapp.auth.data.TokenHolder
import flore.ubb.mob.recipeapp.core.Api
import flore.ubb.mob.recipeapp.core.TAG
import flore.ubb.mob.recipeapp.data.Recipe
import flore.ubb.mob.recipeapp.listcomp.RecipesViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default.encodeToString
import okhttp3.*

import okio.ByteString

class Payload(token: String){
    val token:String = token
}

class Message(token: String){
   val type: String = "authorization";
    val payload: Payload = Payload(token)
}

//{ type: 'authorization', payload: { token }
object WebsocketCreator {
    val eventChannel = Channel<String>()
    var message: Message?

    init {
        message = Api.tokenInterceptor.token?.let { Message(it) }
        val request = Request.Builder().url("ws://192.168.100.2:3000").addHeader(
            "authorization", "Bearer " + Api.tokenInterceptor.token
        ).build()
        val webSocket = RecipeApi.client.newWebSocket(request, MyWebSocketListener())
    }

    private class MyWebSocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            //Log.d("WebSocket", message.toString()+ " "+ Gson().toJson(message))
            webSocket.send(Gson().toJson(message));
            Log.d("WebSocket", "onOpen")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d("WebSocket", "onMessage$text")
            runBlocking { eventChannel.send(text) }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            Log.d("WebSocket", "onMessage bytes")
            output("Receiving bytes : " + bytes!!.hex())
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e("WebSocket", "onFailure", t)
            t.printStackTrace()
        }

        private fun output(txt: String) {
            Log.d("WebSocket", txt)
        }
    }
}

