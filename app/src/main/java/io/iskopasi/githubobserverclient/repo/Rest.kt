package io.iskopasi.githubobserverclient.repo

import android.content.Context
import com.google.gson.Gson
import io.iskopasi.githubobserverclient.pojo.RepositoryContentData
import io.iskopasi.githubobserverclient.pojo.RepositoryData
import io.iskopasi.githubobserverclient.pojo.UserData
import io.iskopasi.githubobserverclient.utils.AuthInterceptor
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import java.io.File

fun getRetrofit(context: Context): Retrofit = Retrofit.Builder()
    .client(getClient(context.cacheDir))
    .addConverterFactory(GsonConverterFactory.create(gson))
    .baseUrl("http://85.130.224.235/").build()

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
                maxSize = 10L * 1024L * 1024L // 10 MiB
            )
        )
        .build()
}

interface Rest {
    @GET("search")
    suspend fun searchUser(@Query("owner") owner: String): Response<List<UserData>>

    @GET("repos")
    suspend fun getRepositories(@Query("owner") owner: String): Response<List<RepositoryData>>

    @GET("content")
    suspend fun getRepositoryContent(
        @Query("owner") owner: String,
        @Query("repo") repo: String,
        @Query("path") path: String? = null,
    ): Response<List<RepositoryContentData>>

    @GET("https://api.github.com/repos/{owner}/{repo}/zipball/{ref}")
    @Streaming
    @Headers("No-Authentication: true")
    suspend fun downloadZip(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("ref") ref: String,
    ): Response<ResponseBody>
}