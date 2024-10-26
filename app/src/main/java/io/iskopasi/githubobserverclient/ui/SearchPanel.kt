@file:OptIn(ExperimentalGlideComposeApi::class)

package io.iskopasi.githubobserverclient.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import io.iskopasi.githubobserverclient.R
import io.iskopasi.githubobserverclient.models.SearchStatus
import io.iskopasi.githubobserverclient.models.UIModel
import io.iskopasi.githubobserverclient.pojo.UserData
import io.iskopasi.githubobserverclient.ui.theme.borderColor1
import io.iskopasi.githubobserverclient.ui.theme.borderColor2
import io.iskopasi.githubobserverclient.ui.theme.borderColor3
import io.iskopasi.githubobserverclient.ui.theme.cursorColor
import io.iskopasi.githubobserverclient.ui.theme.purpleColor3
import io.iskopasi.githubobserverclient.ui.theme.textColor1
import io.iskopasi.githubobserverclient.ui.theme.textColor2
import io.iskopasi.githubobserverclient.utils.clickDelay
import io.iskopasi.githubobserverclient.utils.fontFamily
import io.iskopasi.githubobserverclient.utils.invokeDelayed
import java.util.Locale
import kotlin.math.roundToInt


@Composable
fun SearchPanel(model: UIModel, isUserSelected: Boolean) {
    val focusManager = LocalFocusManager.current
    val conf = LocalConfiguration.current
    var screenHeight by remember { mutableStateOf(conf.screenHeightDp.dp) }
    val scope = rememberCoroutineScope()
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    var searchFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = "",
                selection = TextRange(0)
            )
        )
    }
    val offsetTarget = with(LocalDensity.current) {
        if (isUserSelected)
            IntOffset(
                0,
                (screenHeight.toPx() / 20f).roundToInt()
            )
        else
            IntOffset(
                0,
                (screenHeight.toPx() / 3f).roundToInt()
            )
    }
    val offset = animateIntOffsetAsState(
        targetValue = offsetTarget, label = "offset"
    )

    // Attempt to start search request after user finished entering username
    fun onValueChanged(value: TextFieldValue) {
        searchFieldValue = value
        scope.invokeDelayed("search_username", 700L) {
            model.searchUser(searchFieldValue.text)
        }
    }

    Box(
        modifier = Modifier
            .padding(horizontal = 32.dp)
            .heightIn(0.dp, 270.dp)
//            .offset {
//                offset
//            }
            .layout { measurable, constraints ->
                val offsetValue = if (isLookingAhead) offsetTarget else offset.value
                val placeable = measurable.measure(constraints)
                layout(placeable.width + offsetValue.x, placeable.height + offsetValue.y) {
                    placeable.placeRelative(offsetValue)
                }
            }
            .fillMaxWidth()
            .border(
                0.5.dp,
                if (isFocused) borderColor2 else borderColor1,
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(purpleColor3)
            .animateContentSize()
    ) {
        // Dropdown list
        DropDownList(model) {
            searchFieldValue = TextFieldValue(text = "", selection = TextRange.Zero)
        }
        // Search field
        OutlinedTextField(
            value = searchFieldValue,
            onValueChange = ::onValueChanged,
            textStyle = TextStyle(
                fontFamily = fontFamily,
            ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            placeholder = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.search_placeholder),
                        fontFamily = fontFamily,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = textColor1,
                            fontWeight = FontWeight.Thin
                        )
                    )
                }
            },
            maxLines = 1,
            shape = RoundedCornerShape(24.dp),
            leadingIcon = {
                LeadingIcon(model)
            },
            trailingIcon = {
                PasteButton(searchFieldValue, focusRequester, ::onValueChanged)
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusEvent {
                    isFocused = it.isFocused
                },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedContainerColor = purpleColor3,
                focusedContainerColor = purpleColor3,
                cursorColor = cursorColor
            )
        )
    }
}

@Composable
fun PasteButton(
    searchFieldValue: TextFieldValue,
    focusRequester: FocusRequester,
    pasteText: (TextFieldValue) -> Unit
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    Text(stringResource(R.string.paste).uppercase(Locale.US),
        style = MaterialTheme.typography.bodyMedium.copy(
            color = textColor1,
            fontSize = 10f.sp
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable {
                focusRequester.requestFocus()
                val text = searchFieldValue.text + (clipboardManager
                    .getText() ?: "")
                    .toString()
                pasteText(
                    TextFieldValue(
                        text = text,
                        selection = TextRange(text.length)
                    )
                )
            }
            .padding(horizontal = 4.dp, vertical = 3.dp))
}

@Composable
fun LeadingIcon(model: UIModel) {
    val searchStatus = model.searchStatus.value

    when (searchStatus) {
        SearchStatus.Loading -> Loader(30.dp, 2.dp)

        SearchStatus.Idle -> Icon(Icons.Outlined.Search, contentDescription = "")
        SearchStatus.Error -> {}
    }
}

@Composable
inline fun DropDownList(model: UIModel, crossinline block: () -> Unit) {
    val userDataList = model.userDataList.value
    val listState = rememberLazyListState()

    if (userDataList.isNotEmpty()) {
        LaunchedEffect(userDataList) {
            listState.animateScrollToItem(0)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(top = 54.dp)
    ) {
        items(userDataList.size, key = { it }) {
            val userData = userDataList[it]
            UserListItem(userData, model) {
                model.selectUser(userData)
                block()
            }
        }
    }
}

@Composable
inline fun UserListItem(
    userData: UserData,
    model: UIModel,
    crossinline onClick: (UserData) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(start = 12.dp, end = 12.dp)
            .height(64.dp)
            .fillMaxWidth()
            .clickable {
                clickDelay(300L) {
                    model.clearUserSearchResults()
                    focusManager.clearFocus()
                    onClick(userData)
                }
            }
    ) {
        GlideImage(
            model = userData.avatarUrl,
            contentDescription = userData.login,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(24.dp)
                .border(1.dp, borderColor3, CircleShape)
                .clip(CircleShape)
        )
        Box(modifier = Modifier.padding(start = 16.dp)) {
            Text(
                userData.login,
                fontFamily = fontFamily,
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = textColor2
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
