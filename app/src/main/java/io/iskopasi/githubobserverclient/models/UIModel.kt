package io.iskopasi.githubobserverclient.models

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import dagger.hilt.android.lifecycle.HiltViewModel
import io.iskopasi.githubobserverclient.MarkupActivity
import io.iskopasi.githubobserverclient.modules.IoDispatcher
import io.iskopasi.githubobserverclient.modules.MainDispatcher
import io.iskopasi.githubobserverclient.pojo.RepositoryContentData
import io.iskopasi.githubobserverclient.pojo.RepositoryData
import io.iskopasi.githubobserverclient.pojo.UserData
import io.iskopasi.githubobserverclient.repo.Repo
import io.iskopasi.githubobserverclient.service.DLService
import io.iskopasi.githubobserverclient.utils.GitHierarchy
import io.iskopasi.githubobserverclient.utils.openDownloadsFolder
import io.iskopasi.simplymotion.utils.ServiceCommunicator
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

enum class SearchStatus {
    Loading,
    Idle,
    Error
}

@HiltViewModel
class UIModel @Inject constructor(
    context: Application,
    private val repo: Repo,
    @IoDispatcher private val ioDispatcher: CoroutineContext,
    @MainDispatcher private val mainDispatcher: CoroutineContext
) : BaseViewModel(
    context,
    ioDispatcher = ioDispatcher,
    mainDispatcher = mainDispatcher
) {
    private var hierarchy: GitHierarchy = GitHierarchy(listOf())
    private val _userDataList = mutableStateOf(listOf<UserData>())
    private val _repoDataList = mutableStateOf(listOf<RepositoryData>())
    private val _selectedRepoContentList = mutableStateOf(listOf<RepositoryContentData>())
    private val _currentUser = mutableStateOf(UserData.empty)
    private val _selectedRepo = mutableStateOf(RepositoryData.empty)
    private val _searchStatus = mutableStateOf(SearchStatus.Idle)
    private val _repoLoaderStatus = mutableStateOf(SearchStatus.Idle)
    private val _contentLoaderStatus = mutableStateOf(SearchStatus.Idle)
    private val _dlStatus = mutableStateOf(SearchStatus.Idle)

    // Public states
    val userDataList: State<List<UserData>> = _userDataList
    val repoDataList: State<List<RepositoryData>> = _repoDataList
    val selectedRepoContentList: State<List<RepositoryContentData>> = _selectedRepoContentList
    val currentUser: State<UserData> = _currentUser
    val selectedRepo: State<RepositoryData> = _selectedRepo
    val searchStatus: State<SearchStatus> = _searchStatus
    val repoLoaderStatus: State<SearchStatus> = _repoLoaderStatus
    val contentLoaderStatus: State<SearchStatus> = _contentLoaderStatus
    val dlStatus: State<SearchStatus> = _dlStatus

    private val serviceCommunicator by lazy {
        ServiceCommunicator("UIModel") { data, obj, comm ->
            when (data) {
                SearchStatus.Idle.name -> {
                    _dlStatus.value = SearchStatus.Idle

                    openDownloadsFolder(getApplication())
                }

                SearchStatus.Loading.name -> {
                    _dlStatus.value = SearchStatus.Loading
                }

                SearchStatus.Error.name -> {
                    error(obj as String)
                    _dlStatus.value = SearchStatus.Idle
                }
            }
        }
    }

    fun selectUser(userData: UserData) {
        _selectedRepo.value = RepositoryData.empty
        _currentUser.value = userData

        // Clear search UI
        clearUserSearchResults()

        // Request list of repositories for selected user
        requestRepositories()
    }

    fun selectRepository(repositoryData: RepositoryData) {
        if (_selectedRepo.value.name != repositoryData.name) {
            _selectedRepo.value = repositoryData
            requestInitialRepositoryContent()
        }
    }

    fun searchUser(user: String) = bg {
        if (user.isEmpty()) {
            _userDataList.value = listOf()
            return@bg
        }

        // Set search icon to loading indication
        _searchStatus.value = SearchStatus.Loading

        // Request users
        val result = repo.searchUser(user)

        // Parse response
        _userDataList.value = result!!

        // Reset search state
        _searchStatus.value = SearchStatus.Idle
    }

    fun clearUserSearchResults() {
        _userDataList.value = listOf()
    }

    fun requestRepositories() = bg {
        if (_currentUser.value.isEmpty()) return@bg

        // Set repository loading indication
        _repoLoaderStatus.value = SearchStatus.Loading

        // Request repositories
        val result = repo.getRepositories(_currentUser.value.login)

        // Parse response
        _repoDataList.value = result

        // Reset repository loader state
        _repoLoaderStatus.value = SearchStatus.Idle
    }

    fun requestInitialRepositoryContent() = bg {
        if (_currentUser.value.isEmpty()) return@bg
        if (_selectedRepo.value.isEmpty()) return@bg

        // Set content loading indication
        _contentLoaderStatus.value = SearchStatus.Loading

        // Request repositories
        val result = repo.getRepositoryContent(
            _currentUser.value.login,
            _selectedRepo.value.name,
        )

        hierarchy = GitHierarchy(result)
        _selectedRepoContentList.value = hierarchy.data

        // Reset content loader state
        _contentLoaderStatus.value = SearchStatus.Idle
    }

    fun expandContent(data: RepositoryContentData) = bg {
        if (_currentUser.value.isEmpty()) return@bg
        if (_selectedRepo.value.isEmpty()) return@bg

        // Just remove content from node
        if (hierarchy.hasContent(data)) {
            val flatData = hierarchy.removeContentForNode(data)
            _selectedRepoContentList.value = flatData
            return@bg
        }

        // Updating loader indicator for list item
        data.loadingState.value = SearchStatus.Loading

        // Request nested data
        val result = repo.getRepositoryContent(
            _currentUser.value.login,
            _selectedRepo.value.name,
            data.path
        )

        // Add content and remap inner structure
        val flatData = hierarchy.addNestedContent(data, result)
        // Update state with new content
        _selectedRepoContentList.value = flatData

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
            putExtra("repoName", _selectedRepo.value.name)
            putExtra("owner", _currentUser.value.login)
            putExtra("defaultBranch", _currentUser.value.defaultBranch)
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

    fun showError(error: String) = error(error)
}