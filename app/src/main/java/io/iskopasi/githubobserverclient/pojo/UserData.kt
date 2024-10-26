package io.iskopasi.githubobserverclient.pojo

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.google.gson.annotations.SerializedName
import io.iskopasi.githubobserverclient.models.SearchStatus

data class UserData(
    val login: String = "",
    @SerializedName("avatar_url")
    val avatarUrl: String = "",
    @SerializedName("default_branch")
    val defaultBranch: String = ""
) {
    companion object {
        val empty = UserData()
    }

    fun isEmpty() = login.isEmpty()
}

data class RepositoryData(
    val name: String = "",
    @SerializedName("html_url")
    val htmlUrl: String = "",
) {
    companion object {
        val empty = RepositoryData()
    }

    fun isEmpty() = name.isEmpty()
}

enum class ContentType {
    @SerializedName("dir")
    Directory,

    @SerializedName("file")
    File,

    @SerializedName("unknown")
    Unknown
}

data class RepositoryContentData(
    val name: String = "",
    val path: String? = null,
    val type: ContentType = ContentType.Unknown,
    val size: Int = -1,
    val sha: String = "",
    @SerializedName("download_url")
    val downloadUrl: String = "",
    @Transient val content: MutableList<RepositoryContentData> = mutableListOf<RepositoryContentData>(),
    @Transient val loadingState: MutableState<SearchStatus> = mutableStateOf(SearchStatus.Idle),
    @Transient var level: Int = 0
) {
    companion object {
        val empty = RepositoryContentData()
    }

    fun isEmpty() = name.isEmpty()

    fun flatStructured(level: Int): List<RepositoryContentData> {
        val newList = mutableListOf<RepositoryContentData>()

        content.forEach {
            // Add top-level node
            newList.add(it.apply { this.level = level })

            // Add nested nodes
            newList.addAll(it.flatStructured(level + 1))
        }

        return newList
    }
}