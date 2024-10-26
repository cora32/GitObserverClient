package io.iskopasi.githubobserverclient.ui

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import io.iskopasi.githubobserverclient.ui.theme.purpleColor1
import io.iskopasi.githubobserverclient.ui.theme.purpleColor3

@Composable
fun Loader(width: Dp, height: Dp) {
    LinearProgressIndicator(
        modifier = Modifier
            .width(width)
            .height(height),
        color = purpleColor1,
        trackColor = purpleColor3,
        strokeCap = StrokeCap.Round
    )
}