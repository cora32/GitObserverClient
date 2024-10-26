package io.iskopasi.githubobserverclient.modules

import android.content.Context
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.iskopasi.githubobserverclient.repo.Repo
import io.iskopasi.githubobserverclient.repo.Rest
import io.iskopasi.githubobserverclient.utils.AuthInterceptor
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class HiltModules {
    @Provides
    @Singleton
    fun getRepo(@ApplicationContext context: Context): Repo = Repo(getRest(context))

    @Provides
    @Singleton
    fun getRest(@ApplicationContext context: Context): Rest =
        getRetrofit(context).create(Rest::class.java)

    @Provides
    @Singleton
    fun getRetrofit(@ApplicationContext context: Context): Retrofit = Retrofit.Builder()
        .client(getClient(context.cacheDir))
        .addConverterFactory(GsonConverterFactory.create(gson))
        .baseUrl("http://10.0.0.11:8089/").build()
}

val gson = Gson()

fun getClient(cacheDir: File): OkHttpClient {
    val loggingInterceptor = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.NONE)
    }
    return OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .addInterceptor(loggingInterceptor)
        .cache(
            Cache(
                directory = File(cacheDir, "http_cache"),
                maxSize = 10L * 1024L * 1024L // 50 MiB
            )
        )
        .build()
}