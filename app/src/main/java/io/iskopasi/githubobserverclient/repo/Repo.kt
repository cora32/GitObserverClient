package io.iskopasi.githubobserverclient.repo

import io.iskopasi.githubobserverclient.pojo.GOResult
import io.iskopasi.githubobserverclient.pojo.RepositoryContentData
import io.iskopasi.githubobserverclient.pojo.RepositoryData
import io.iskopasi.githubobserverclient.pojo.UserData
import io.iskopasi.githubobserverclient.utils.asError
import io.iskopasi.githubobserverclient.utils.asOk
import io.iskopasi.githubobserverclient.utils.getNewFileInDownloads
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class Repo @Inject constructor(private val rest: Rest) {
    suspend fun searchUser(owner: String): GOResult<List<UserData>> {
        try {
            return rest.searchUser(owner).toGOResult()
        } catch (e: HttpException) {
            e.printStackTrace()
            return "Network exception: ${e.message}".asError()
        } catch (e: Throwable) {
            e.printStackTrace()
            return "General exception: ${e.message}".asError()
        }
    }

    suspend fun getRepositories(owner: String): GOResult<List<RepositoryData>> {
        try {
            return rest.getRepositories(owner).toGOResult()
        } catch (e: HttpException) {
            e.printStackTrace()
            return "Network exception: ${e.message}".asError()
        } catch (e: Throwable) {
            e.printStackTrace()
            return "General exception: ${e.message}".asError()
        }
    }

    suspend fun getRepositoryContent(
        owner: String,
        repoName: String,
        path: String? = null,
    ): GOResult<List<RepositoryContentData>> {
        try {
            return rest.getRepositoryContent(owner, repoName, path).let {
                if (it.isSuccessful) {
                    it.body()!!.sortedBy { it.type }.asOk()
                } else {
                    "Code: ${it.code()}".asError()
                }
            }
        } catch (e: HttpException) {
            e.printStackTrace()
            return "Network exception: ${e.message}".asError()
        } catch (e: Throwable) {
            e.printStackTrace()
            return "General exception: ${e.message}".asError()
        }
    }

    suspend fun downloadZip(owner: String, repoName: String, branch: String): GOResult<File> {
        try {
            return rest.downloadZip(owner, repoName, branch).body()?.byteStream()?.use {
                val file = getNewFileInDownloads(owner, repoName, ".zip")

                FileOutputStream(file).use { targetOutputStream ->
                    it.copyTo(targetOutputStream)
                }

                file.asOk()
            } ?: throw RuntimeException("failed to download: $repoName")
        } catch (e: HttpException) {
            e.printStackTrace()
            return "Network exception: ${e.message}".asError()
        } catch (e: Throwable) {
            e.printStackTrace()
            return "General exception: ${e.message}".asError()
        }
    }
}

private fun <T> Response<T>.toGOResult(): GOResult<T> =
    if (isSuccessful) {
        body()!!.asOk()
    } else {
        "Code: ${code()}".asError()
    }
