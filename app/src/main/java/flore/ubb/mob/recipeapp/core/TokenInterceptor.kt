package flore.ubb.mob.recipeapp.core

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor constructor() : Interceptor {
    var token: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val originalUrl = original.url
        if (token == null) {
            return chain.proceed(original)
        }

        Log.v(TAG,"token: "+token);

        val requestBuilder = original.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .url(originalUrl)
        val request = requestBuilder.build()

        return chain.proceed(request)
    }
}