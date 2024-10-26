package io.iskopasi.githubobserverclient.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import io.iskopasi.githubobserverclient.models.UIModel

@Composable
fun DrawerContent(model: UIModel, drawerState: DrawerState) {
    val focusManager = LocalFocusManager.current
    val currentUser = model.currentUser.value

    Box(
        Modifier
            .fillMaxSize()
            .clickable {
                // Clear user list if clicked outside of search panel
                model.clearUserSearchResults()
                focusManager.clearFocus()
            }) {
        // Repository list panel
        AnimatedVisibility(!currentUser.isEmpty(), enter = fadeIn(), exit = fadeOut()) {
            RepositoryPanel(model, drawerState)
        }
        // Search field with dropdown items
        SearchPanel(model, !currentUser.isEmpty())
    }
}