package io.iskopasi.githubobserverclient

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.mukesh.MarkDown
import io.iskopasi.githubobserverclient.ui.theme.GithubObserverClientTheme
import io.iskopasi.githubobserverclient.ui.theme.purpleColor3
import java.net.URL

class MarkupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val downloadUrl = intent.getStringExtra("downloadUrl") ?: ""

        // Lock orientation
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED

        enableEdgeToEdge()

        setContent {
            GithubObserverClientTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = purpleColor3
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .background(purpleColor3)
                            .padding(innerPadding)
                            .background(purpleColor3)
                    ) {
                        MarkDown(
                            url = URL(downloadUrl),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}