package flore.ubb.mob.recipeapp.data.remote
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import flore.ubb.mob.recipeapp.auth.LoginViewModel
import flore.ubb.mob.recipeapp.auth.data.TokenHolder
import flore.ubb.mob.recipeapp.core.Api
import flore.ubb.mob.recipeapp.data.Recipe
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import retrofit2.http.Headers

object RecipeApi {
    const val URL = "http://192.168.100.2:3000"

    interface Service {
        @GET("/api/recipe")
        suspend fun find(): List<Recipe>

        @GET("/api/recipe/{id}")
        suspend fun read(@Path("id") itemId: String): Recipe;

        @DELETE("/api/recipe/{id}")
        suspend fun remove(@Path("id") itemId: String): Recipe;

        @Headers("Content-Type: application/json")
        @POST("/api/recipe")
        suspend fun create(@Body item: Recipe): Recipe

        @Headers("Content-Type: application/json")
        @PUT("/api/recipe/{id}")
        suspend fun update(@Path("id") id:String, @Body item: Recipe): Recipe
    }

    val client: OkHttpClient = Api.client

    private var gson = GsonBuilder()
        .setLenient()
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(client)
        .build()

    val service: Service = retrofit.create(Service::class.java)

}