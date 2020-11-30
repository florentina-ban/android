package flore.ubb.mob.recipeapp.auth.data

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import flore.ubb.mob.recipeapp.MainActivity
import flore.ubb.mob.recipeapp.R
import flore.ubb.mob.recipeapp.auth.data.remote.RemoteAuthDataSource
import flore.ubb.mob.recipeapp.core.Result;
import flore.ubb.mob.recipeapp.core.Api
import flore.ubb.mob.recipeapp.data.remote.RecipeApi
import android.content.SharedPreferences
import flore.ubb.mob.recipeapp.core.TAG

object AuthRepository {
    var user: User? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        user = null
    }

    fun logout() {
        user = null
        Api.tokenInterceptor.token = null
        RemoteAuthDataSource.logout()
        Api.tokenInterceptor.token?.let { Log.v(TAG, it) }
    }

    suspend fun login(username: String, password: String): Result<TokenHolder> {
        val user = User(username, password)
        val result = RemoteAuthDataSource.login(user)
        if (result is Result.Success<TokenHolder>) {
            setLoggedInUser(user, result.data)
        }
        return result
    }
    suspend fun register(username: String, password: String): Result<TokenHolder> {
        val user = User(username, password)
        val result = RemoteAuthDataSource.register(user)
        if (result is Result.Success<TokenHolder>) {
            setLoggedInUser(user, result.data)
        }
        return result
    }

    private fun setLoggedInUser(user: User, tokenHolder: TokenHolder) {
        this.user = user
        Api.tokenInterceptor.token = tokenHolder.token


    }
}
