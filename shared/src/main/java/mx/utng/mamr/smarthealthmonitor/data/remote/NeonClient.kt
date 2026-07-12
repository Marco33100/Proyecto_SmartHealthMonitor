package mx.utng.mamr.smarthealthmonitor.data.remote

import mx.utng.mamr.smarthealthmonitor.shared.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NeonClient {
    private const val BASE_URL = "https://${BuildConfig.NEON_HOST}/"

    val CONN_STRING = "postgresql://${BuildConfig.NEON_USER}:${BuildConfig.NEON_PASSWORD}@${BuildConfig.NEON_HOST}/${BuildConfig.NEON_DB}?sslmode=require"

    val api: NeonApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .header("Neon-Connection-String", CONN_STRING)
                            .build()
                        chain.proceed(request)
                    }
                    .addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }).build()
            )
            .build()
            .create(NeonApiService::class.java)
    }
}
