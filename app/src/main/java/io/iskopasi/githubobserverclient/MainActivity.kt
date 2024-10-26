package io.iskopasi.githubobserverclient

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import io.iskopasi.githubobserverclient.models.UIModel
import io.iskopasi.githubobserverclient.ui.Content
import io.iskopasi.githubobserverclient.ui.DrawerContent
import io.iskopasi.githubobserverclient.ui.theme.GithubObserverClientTheme
import io.iskopasi.githubobserverclient.ui.theme.drawerContentColor
import io.iskopasi.githubobserverclient.ui.theme.drawerMenuColor
import io.iskopasi.githubobserverclient.ui.theme.purpleColor4
import io.iskopasi.githubobserverclient.ui.theme.textColor2
import io.iskopasi.githubobserverclient.utils.fontFamily
import io.iskopasi.githubobserverclient.utils.openDownloadsFolder
import kotlin.getValue

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val model: UIModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Lock orientation
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED

        enableEdgeToEdge()

        setContent {
            val focusManager = LocalFocusManager.current
            val snackState = SnackbarHostState()
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
            val errorFlow by model.errorFlow.collectAsState()

            // Show snackbar with error and close keyboard on error
            if (errorFlow != null) {
                LaunchedEffect(errorFlow) {
                    focusManager.clearFocus()
                    snackState.showSnackbar(errorFlow!!)
                }
            }

            GithubObserverClientTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        SmallFloatingActionButton(
                            onClick = {
                                openDownloadsFolder(this@MainActivity)
                            },
                            containerColor = purpleColor4,
                            contentColor = MaterialTheme.colorScheme.secondary
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                            ) {
                                Text(
                                    stringResource(R.string.download),
                                    fontFamily = fontFamily,
                                    textAlign = TextAlign.Start,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = textColor2,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.W400
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    Icons.Filled.Downloading,
                                    "Downloads",
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    },
                    snackbarHost = {
                        SnackbarHost(hostState = snackState) {
                            if (snackState.currentSnackbarData != null) {
                                Snackbar(
                                    actionColor = Color.Yellow,
                                    contentColor = Color.White,
                                    containerColor = Color.Red,
                                    snackbarData = snackState.currentSnackbarData!!
                                )
                            }
                        }
                    },
                ) { innerPadding ->
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        ModalNavigationDrawer(
                            drawerState = drawerState,
                            drawerContent = {
                                ModalDrawerSheet(
                                    modifier = Modifier.width(450.dp),
                                    drawerContainerColor = drawerMenuColor,
                                    drawerContentColor = drawerContentColor,
                                ) {
                                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                        DrawerContent(model, drawerState)
                                    }
                                }
                            },
                        ) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Content(innerPadding, model)
                            }
                        }
                    }
                }
            }
        }
    }
}