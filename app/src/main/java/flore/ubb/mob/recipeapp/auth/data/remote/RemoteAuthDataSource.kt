package flore.ubb.mob.recipeapp.auth.data.remote

import android.util.Log
import com.google.gson.Gson
import flore.ubb.mob.recipeapp.TAG
import flore.ubb.mob.recipeapp.auth.data.TokenHolder
import flore.ubb.mob.recipeapp.auth.data.User
import flore.ubb.mob.recipeapp.core.Api
import flore.ubb.mob.recipeapp.core.Result
import flore.ubb.mob.recipeapp.data.remote.RecipeApi
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST


object RemoteAuthDataSource {
    interface AuthService {
        @Headers("Content-Type: application/json")
        @POST("api/auth/login")
        suspend fun login(@Body user: User): TokenHolder

        @Headers("Content-Type: application/json")
        @POST("api/auth/signup")
        suspend fun register(@Body user: User): TokenHolder
    }
    private var authService: AuthService = Api.retrofit.create(AuthService::class.java)

    suspend fun login(user: User): Result<TokenHolder> {
        try {
            Log.v(TAG, "in login")
            var result = Result.Success(authService.login(user))
            return result;

        } catch (e: Exception) {
            return Result.Error(e)
        }
    }

    suspend fun register(user: User): Result<TokenHolder> {
        try {
            Log.v(TAG, "in login")
            var result = Result.Success(authService.register(user))
            return result;

        } catch (e: Exception) {
            return Result.Error(e)
        }
    }

    fun logout(){
        try{
            Log.v(TAG,"in logout" )

            Api.tokenInterceptor.token?.let { Log.v(TAG, it) }
        }catch (ex: Exception){
            Log.v(TAG, ex.toString())
        }
    }
}