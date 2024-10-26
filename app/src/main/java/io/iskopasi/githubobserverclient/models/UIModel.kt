package io.iskopasi.githubobserverclient.models

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.iskopasi.githubobserverclient.MarkupActivity
import io.iskopasi.githubobserverclient.pojo.RepositoryContentData
import io.iskopasi.githubobserverclient.pojo.RepositoryData
import io.iskopasi.githubobserverclient.pojo.Status
import io.iskopasi.githubobserverclient.pojo.UserData
import io.iskopasi.githubobserverclient.repo.Repo
import io.iskopasi.githubobserverclient.service.DLService
import io.iskopasi.githubobserverclient.utils.GitHierarchy
import io.iskopasi.githubobserverclient.utils.bg
import io.iskopasi.githubobserverclient.utils.openDownloadsFolder
import io.iskopasi.githubobserverclient.utils.ui
import io.iskopasi.simplymotion.utils.ServiceCommunicator
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

enum class SearchStatus {
    Loading,
    Idle,
    Error
}

@HiltViewModel
class UIModel @Inject constructor(
    context: Application,
    private val repo: Repo
) : AndroidViewModel(context) {
    private var hierarchy: GitHierarchy = GitHierarchy(listOf())
    val userDataList = mutableStateOf(listOf<UserData>())
    val repoDataList = mutableStateOf(listOf<RepositoryData>())
    val selectedRepoContentList = mutableStateOf(listOf<RepositoryContentData>())
    val currentUser =
        mutableStateOf(UserData.empty)
    val selectedRepo = mutableStateOf(RepositoryData.empty)
    val searchStatus = mutableStateOf(SearchStatus.Idle)
    val repoLoaderStatus = mutableStateOf(SearchStatus.Idle)
    val contentLoaderStatus = mutableStateOf(SearchStatus.Idle)
    val dlStatus = mutableStateOf(SearchStatus.Idle)
    val errorFlow = MutableStateFlow<String?>(null)

    private val serviceCommunicator by lazy {
        ServiceCommunicator("UIModel") { data, obj, comm ->
            when (data) {
                SearchStatus.Idle.name -> {
                    dlStatus.value = SearchStatus.Idle

                    openDownloadsFolder(getApplication())
                }

                SearchStatus.Loading.name -> {
                    dlStatus.value = SearchStatus.Loading
                }

                SearchStatus.Error.name -> {
                    ui { errorFlow.emit(obj as String) }
                    dlStatus.value = SearchStatus.Idle
                }
            }
        }
    }

    fun selectUser(userData: UserData) {
        selectedRepo.value = RepositoryData.empty
        currentUser.value = userData

        // Clear search UI
        clearUserSearchResults()

        // Request list of repositories for selected user
        requestRepositories()
    }

    fun selectRepository(repositoryData: RepositoryData) {
        if (selectedRepo.value.name != repositoryData.name) {
            selectedRepo.value = repositoryData
            requestInitialRepositoryContent()
        }
    }

    fun searchUser(user: String) = bg {
        if (user.isEmpty()) {
            userDataList.value = listOf()
            return@bg
        }

        // Set search icon to loading indication
        searchStatus.value = SearchStatus.Loading

        // Request users
        val result = repo.searchUser(user)

        // Parse response
        when (result.status) {
            Status.OK -> userDataList.value = result.data!!
            Status.Error -> {
                userDataList.value = listOf()
                errorFlow.emit("Error -> ${result.error}")
            }

            Status.Unknown -> {}
        }

        // Reset search state
        searchStatus.value = SearchStatus.Idle
    }

    fun clearUserSearchResults() {
        userDataList.value = listOf()
    }

    fun requestRepositories() = bg {
        if (currentUser.value.isEmpty()) return@bg

        // Set repository loading indication
        repoLoaderStatus.value = SearchStatus.Loading

        // Request repositories
        val result = repo.getRepositories(currentUser.value.login)

        // Parse response
        when (result.status) {
            Status.OK -> repoDataList.value = result.data!!
            Status.Error -> {
                repoDataList.value = listOf()
                errorFlow.emit("Error -> ${result.error}")
            }

            Status.Unknown -> {}
        }

        // Reset repository loader state
        repoLoaderStatus.value = SearchStatus.Idle
    }

    fun requestInitialRepositoryContent() = bg {
        if (currentUser.value.isEmpty()) return@bg
        if (selectedRepo.value.isEmpty()) return@bg

        // Set content loading indication
        contentLoaderStatus.value = SearchStatus.Loading

        // Request repositories
        val result = repo.getRepositoryContent(
            currentUser.value.login,
            selectedRepo.value.name,
        )

        // Parse response
        when (result.status) {
            Status.OK -> {
                hierarchy = GitHierarchy(result.data!!)
                selectedRepoContentList.value = hierarchy.data
            }

            Status.Error -> {
                selectedRepoContentList.value = listOf<RepositoryContentData>()
                errorFlow.emit("Error -> ${result.error}")
            }

            Status.Unknown -> {}
        }

        // Reset content loader state
        contentLoaderStatus.value = SearchStatus.Idle
    }

    fun expandContent(data: RepositoryContentData) = bg {
        if (currentUser.value.isEmpty()) return@bg
        if (selectedRepo.value.isEmpty()) return@bg

        // Just remove content from node
        if (hierarchy.hasContent(data)) {
            val flatData = hierarchy.removeContentForNode(data)
            selectedRepoContentList.value = flatData
            return@bg
        }

        // Updating loader indicator for list item
        data.loadingState.value = SearchStatus.Loading

        // Request nested data
        val result = repo.getRepositoryContent(
            currentUser.value.login,
            selectedRepo.value.name,
            data.path
        )

        // Parse response
        when (result.status) {
            Status.OK -> {
                // Add content and remap inner structure
                val flatData = hierarchy.addNestedContent(data, result.data!!)
                // Update state with new content
                selectedRepoContentList.value = flatData
            }

            Status.Error -> {
                errorFlow.emit("Error -> ${result.error}")
            }

            Status.Unknown -> {}
        }

        data.loadingState.value = SearchStatus.Idle
    }

    fun openFile(context: Context, downloadUrl: String) {
        context.startActivity(Intent(context, MarkupActivity::class.java).apply {
            putExtra("downloadUrl", downloadUrl)
        })
    }

    fun startDownloadService() {
        val context = getApplication<Application>()
        val intent = Intent(
            context,
            DLService::class.java
        ).apply {
            putExtra("repoName", selectedRepo.value.name)
            putExtra("owner", currentUser.value.login)
            putExtra("defaultBranch", currentUser.value.defaultBranch)
        }

        context.bindService(
            intent,
            serviceCommunicator.serviceConnection,
            Context.BIND_AUTO_CREATE
        )
        ContextCompat.startForegroundService(
            context,
            intent
        )
    }

    fun showError(error: String) = ui {
        errorFlow.emit(error)
    }
}