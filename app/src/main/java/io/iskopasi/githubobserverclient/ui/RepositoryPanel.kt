@file:OptIn(ExperimentalGlideComposeApi::class)

package io.iskopasi.githubobserverclient.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import io.iskopasi.githubobserverclient.R
import io.iskopasi.githubobserverclient.models.SearchStatus
import io.iskopasi.githubobserverclient.models.UIModel
import io.iskopasi.githubobserverclient.pojo.RepositoryData
import io.iskopasi.githubobserverclient.pojo.UserData
import io.iskopasi.githubobserverclient.ui.theme.borderColor3
import io.iskopasi.githubobserverclient.ui.theme.horizontalDrawePadding
import io.iskopasi.githubobserverclient.ui.theme.selectedItemBg
import io.iskopasi.githubobserverclient.ui.theme.textColor1
import io.iskopasi.githubobserverclient.ui.theme.textColor2
import io.iskopasi.githubobserverclient.utils.clickDelay
import io.iskopasi.githubobserverclient.utils.fontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnimatedVisibilityScope.RepositoryPanel(model: UIModel, drawerState: DrawerState) {
    val userData = model.currentUser.value

    when {
        userData.isEmpty() -> Loader(100.dp, 3.dp)

        else -> Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 140.dp)
                .animateEnterExit()
        ) {
            RepoContent(model, userData, drawerState)
        }
    }
}

@Composable
fun RepoListPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            stringResource(R.string.no_repo_found),
            fontFamily = fontFamily,
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.headlineLarge.copy(
                color = textColor1,
                fontSize = 18.sp,
                fontWeight = FontWeight.Thin
            ),
            maxLines = 1,
        )
    }
}

@Composable
fun RepoContent(model: UIModel, userData: UserData, drawerState: DrawerState) {
    val selectedRepo = model.selectedRepo.value

    Column(modifier = Modifier.fillMaxSize()) {
        // User avatar and name block
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                start = horizontalDrawePadding.dp,
                end = horizontalDrawePadding.dp
            )
        ) {
            GlideImage(
                model = userData.avatarUrl,
                contentDescription = userData.login,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(70.dp)
                    .border(2.dp, borderColor3, CircleShape)
                    .clip(CircleShape)
            )
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .animateContentSize()
            ) {
                Text(
                    userData.login,
                    fontFamily = fontFamily,
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = textColor2,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.W400
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!selectedRepo.isEmpty())
                    Text(
                        selectedRepo.name,
                        fontFamily = fontFamily,
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = textColor2,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.W300
                        ),
                        maxLines = 1,
                        modifier = Modifier.padding(top = 4.dp),
                        overflow = TextOverflow.Ellipsis
                    )
            }
        }
        HorizontalDivider(
            thickness = 0.5.dp, color = Color.White,
            modifier = Modifier.padding(vertical = 32.dp)
        )
//        // Repository label
//        Text(
//            stringResource(R.string.repo_label),
//            fontFamily = fontFamily,
//            textAlign = TextAlign.Start,
//            style = MaterialTheme.typography.headlineLarge.copy(
//                color = textColor2,
//                fontSize = 25.sp,
//                fontWeight = FontWeight.W300
//            ),
//            maxLines = 1,
//            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
//        )
//        HorizontalDivider(
//            thickness = 0.5.dp, color = Color.White,
//            modifier = Modifier.padding(vertical = 16.dp)
//        )
        // List of repositories
        RepoList(model, drawerState)
    }
}

@Composable
fun RepoList(model: UIModel, drawerState: DrawerState) {
    val loadingStatus = model.repoLoaderStatus.value
    val repoDataList = model.repoDataList.value
    var selectedRepoIndex by remember { mutableIntStateOf(-1) }
    val scope = rememberCoroutineScope()

    when {
        else -> when (loadingStatus) {
            SearchStatus.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { Loader(150.dp, 4.dp) }

            SearchStatus.Idle -> when {
                repoDataList.isEmpty() -> RepoListPlaceholder()
                else -> LazyColumn {
                    items(repoDataList.size, key = { it }) {
                        val repoData = repoDataList[it]
                        Column {
                            RepoListItem(repoData, isSelected = selectedRepoIndex == it) {
                                clickDelay(400L) {
                                    selectedRepoIndex = it
                                    model.selectRepository(repoData)

                                    scope.launch {
                                        delay(300L)
                                        drawerState.close()
                                    }
                                }
                            }
                            if (it != repoDataList.size - 1) HorizontalDivider(
                                thickness = 0.2.dp, color = textColor1,
                            )
                        }
                    }
                }
            }

            SearchStatus.Error -> {}
        }
    }
}

@Composable
inline fun RepoListItem(
    repositoryData: RepositoryData,
    isSelected: Boolean,
    crossinline block: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(80.dp)
            .fillMaxWidth()
            .background(if (isSelected) selectedItemBg else Color.Transparent)
            .clickable {
                block()
            }
            .padding(top = 8.dp, bottom = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = horizontalDrawePadding.dp, end = horizontalDrawePadding.dp)
        ) {
            Text(
                repositoryData.name,
                fontFamily = fontFamily,
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = textColor2,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Light
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                Icons.AutoMirrored.Outlined.ArrowForwardIos,
                contentDescription = "",
                tint = textColor1
            )
        }
    }
}