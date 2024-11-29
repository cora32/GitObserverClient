package io.iskopasi.githubobserverclient.repo

import io.iskopasi.githubobserverclient.pojo.RepositoryContentData
import io.iskopasi.githubobserverclient.pojo.RepositoryData
import io.iskopasi.githubobserverclient.pojo.UserData
import io.iskopasi.githubobserverclient.utils.getNewFileInDownloads
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class Repo @Inject constructor(private val rest: Rest) {
    suspend fun searchUser(owner: String): List<UserData> = rest.searchUser(owner).body()!!

    suspend fun getRepositories(owner: String): List<RepositoryData> =
        rest.getRepositories(owner).body()!!

    suspend fun getRepositoryContent(
        owner: String,
        repoName: String,
        path: String? = null,
    ): List<RepositoryContentData> =
        rest.getRepositoryContent(owner, repoName, path).body()!!.sortedBy { it.type }

    suspend fun downloadZip(owner: String, repoName: String, branch: String): File =
        rest.downloadZip(owner, repoName, branch).body()!!.byteStream().use {
            val file = getNewFileInDownloads(owner, repoName, ".zip")

            FileOutputStream(file).use { targetOutputStream ->
                it.copyTo(targetOutputStream)
            }

            file
        }
}