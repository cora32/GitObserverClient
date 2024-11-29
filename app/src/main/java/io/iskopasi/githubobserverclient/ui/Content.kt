@file:OptIn(ExperimentalGlideComposeApi::class)

package io.iskopasi.githubobserverclient.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import io.iskopasi.githubobserverclient.R
import io.iskopasi.githubobserverclient.models.SearchStatus
import io.iskopasi.githubobserverclient.models.UIModel
import io.iskopasi.githubobserverclient.pojo.ContentType
import io.iskopasi.githubobserverclient.pojo.RepositoryContentData
import io.iskopasi.githubobserverclient.pojo.RepositoryData
import io.iskopasi.githubobserverclient.pojo.UserData
import io.iskopasi.githubobserverclient.ui.theme.borderColor3
import io.iskopasi.githubobserverclient.ui.theme.purpleColor3
import io.iskopasi.githubobserverclient.ui.theme.textColor1
import io.iskopasi.githubobserverclient.ui.theme.textColor2
import io.iskopasi.githubobserverclient.utils.clickDelay
import io.iskopasi.githubobserverclient.utils.fontFamily


@Composable
fun Content(innerPadding: PaddingValues, model: UIModel) {
    val selectedRepo by model.selectedRepo
    val contentLoaderStatus by model.contentLoaderStatus

    Box(
        modifier = Modifier
            .background(purpleColor3)
            .padding(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding()
            )
            .fillMaxSize()
    ) {
        when {
            selectedRepo.isEmpty() -> ContentPlaceholder()
            contentLoaderStatus == SearchStatus.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Loader(100.dp, 3.dp)
            }

            else -> ContentValue(model, selectedRepo)
        }
    }
}

@Composable
fun DLButton(model: UIModel) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultMap ->
        if (resultMap.values.all { it }) {
            model.startDownloadService()
        } else {
            model.showError("Permission denied")
        }
    }
    val context = LocalContext.current
    val status by model.dlStatus

    // For >= 33
    // WRITE_EXTERNAL_STORAGE has no effect for versions >= R
    fun requestPermissionsGE33() = ContextCompat.checkSelfPermission(
        context, Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED

    // For < 33
    fun requestPermissionsL33() = ContextCompat.checkSelfPermission(
        context, Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    Box(modifier = Modifier.height(70.dp), contentAlignment = Alignment.Center) {
        OutlinedButton(
            onClick = {
                when {
                    // >=33
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                        when (requestPermissionsGE33()) {
                            true -> model.startDownloadService()
                            else -> launcher.launch(
                                arrayOf(
                                    Manifest.permission.POST_NOTIFICATIONS
                                )
                            )
                        }

                    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ->
                        when (requestPermissionsL33()) {
                            true -> model.startDownloadService()
                            else -> launcher.launch(
                                arrayOf(
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                )
                            )
                        }
                }
            },
            border = BorderStroke(1.dp, Color.Transparent),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.Yellow,
                containerColor = purpleColor3
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.dl_zip),
                    fontFamily = fontFamily,
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = textColor2,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Light,
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                when (status) {
                    SearchStatus.Idle ->
                        Icon(
                            Icons.Default.Download,
                            contentDescription = "",
                            tint = textColor2,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(24.dp)
                        )

                    SearchStatus.Loading -> Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(24.dp),
                        contentAlignment = Alignment.Center
                    ) { Loader(40.dp, 2.dp) }

                    SearchStatus.Error -> {}
                }
            }
        }
    }
}

@Composable
fun OpenInBrowserButton(model: UIModel) {
    val uriHandler = LocalUriHandler.current

    Box(modifier = Modifier.height(70.dp), contentAlignment = Alignment.Center) {
        OutlinedButton(
            onClick = {
                uriHandler.openUri(model.selectedRepo.value.htmlUrl)
            },
            border = BorderStroke(1.dp, Color.Transparent),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.Yellow,
                containerColor = purpleColor3
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.open_in_browser),
                    fontFamily = fontFamily,
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = textColor2,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Light,
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    Icons.Default.OpenInBrowser,
                    contentDescription = "",
                    tint = textColor2,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(24.dp)
                )
            }
        }
    }
}

@Composable
fun ContentPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            stringResource(R.string.select_repo_placeholder),
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
fun ContentValue(model: UIModel, selectedRepo: RepositoryData) {
    val currentUser by model.currentUser

    Column(
        modifier = Modifier
            .padding(vertical = 24.dp)
            .fillMaxSize()
    ) {
        // Username + Repository name + Icon
        UserPanel(currentUser, selectedRepo, model)
        HorizontalDivider(
            thickness = 0.5.dp, color = Color.White,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        // Content list
        ContentList(model)
    }
}

@Composable
fun UserPanel(currentUser: UserData, selectedRepo: RepositoryData, model: UIModel) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(100.dp)
                .padding(horizontal = 18.dp)
        ) {
            GlideImage(
                model = currentUser.avatarUrl,
                contentDescription = currentUser.login,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(90.dp)
                    .border(2.dp, borderColor3, CircleShape)
                    .clip(CircleShape)
            )
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .padding(start = 18.dp, end = 18.dp)
                    .fillMaxHeight()
            ) {
                Text(
                    currentUser.login,
                    fontFamily = fontFamily,
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = textColor2,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Light,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    selectedRepo.name,
                    fontFamily = fontFamily,
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = textColor2,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Light
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

        }
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()

        ) {
            Box() { OpenInBrowserButton(model) }
            Box(
                modifier = Modifier
                    .padding(end = 54.dp)
            ) {
                DLButton(
                    model
                )
            }
        }
    }
}

@Composable
fun ContentList(model: UIModel) {
    val contentList by model.selectedRepoContentList
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .animateContentSize()
    ) {
        items(contentList.size, key = { it }) {
            val contentData = contentList[it]
            Column(modifier = Modifier.animateItem()) {
                TopItemContent(contentData) {
                    clickDelay(400L) {
                        when (contentData.type) {
                            ContentType.Directory -> model.expandContent(contentData)
                            ContentType.File -> model.openFile(context, contentData.downloadUrl)
                            ContentType.Unknown -> {}
                        }

                    }
                }
                if (it != contentList.size - 1) HorizontalDivider(
                    thickness = 0.2.dp, color = textColor1,
                )
            }
        }
    }
}


@Composable
inline fun TopItemContent(repositoryData: RepositoryContentData, crossinline block: () -> Unit) {
    val loadingState = repositoryData.loadingState.value
    val typeIcon =
        when (repositoryData.type) {
            ContentType.File -> Icons.Default.FilePresent
            ContentType.Directory -> Icons.Default.Folder
            ContentType.Unknown -> Icons.Default.Quiz
        }

    Box(
        modifier = Modifier
            .height(40.dp)
            .fillMaxWidth()
            .background(Color.Transparent)
            .clickable {
                block()
            }
            .padding(top = 8.dp, bottom = 8.dp, start = (repositoryData.level * 8).dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 56.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxHeight()
            ) {
                Icon(
                    typeIcon,
                    contentDescription = "",
                    tint = textColor1
                )
                Text(
                    repositoryData.name,
                    fontFamily = fontFamily,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = textColor2,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Light,
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
            if (repositoryData.type == ContentType.Directory) {
                when (loadingState) {
                    SearchStatus.Idle -> Icon(
                        Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "",
                        tint = textColor1,
                        modifier = Modifier.size(14.dp)
                    )

                    SearchStatus.Loading -> Loader(30.dp, 2.dp)
                    SearchStatus.Error -> {}
                }
            } else if (repositoryData.type == ContentType.File) {

                Text(
                    "${repositoryData.size}b",
                    fontFamily = fontFamily,
                    textAlign = TextAlign.Center,

                    style = MaterialTheme.typography.bodySmall.copy(
                        color = textColor1,
                        fontSize = 11.sp,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Thin,
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }
    }
}