package io.iskopasi.githubobserverclient.repo

import io.iskopasi.githubobserverclient.pojo.RepositoryContentData
import io.iskopasi.githubobserverclient.pojo.RepositoryData
import io.iskopasi.githubobserverclient.pojo.UserData
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

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