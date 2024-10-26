package io.iskopasi.githubobserverclient.utils

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.iskopasi.githubobserverclient.BuildConfig
import io.iskopasi.githubobserverclient.pojo.GOResult
import io.iskopasi.githubobserverclient.pojo.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

// Logging utils
val String.e: Unit
    get() {
        if (BuildConfig.DEBUG) Log.e("--> ERR:", this)
    }

// Delayed start
val delayedJobs = mutableMapOf<String, Job>()

inline fun CoroutineScope.scheduleNewTask(delay: Long, crossinline block: () -> Unit) =
    launch(Dispatchers.IO) {
        delay(delay)
        block()
    }

inline fun CoroutineScope.invokeDelayed(
    key: String,
    delay: Long,
    crossinline block: () -> Unit
) {
    // Cancel old task
    if (delayedJobs.containsKey(key)) {
        delayedJobs[key]!!.cancel()
        delayedJobs.remove(key)
    }

    // Reschedule new task
    delayedJobs[key] = scheduleNewTask(delay, block)
}

// Anti click-spam helper
var lastClickTime = 0L
inline fun clickDelay(
    delay: Long,
    block: () -> Unit
) {
    val diff = System.currentTimeMillis() - lastClickTime
    if (diff > delay) {
        lastClickTime = System.currentTimeMillis()
        block()
    }
}

// Response wrappers
fun <T> T.asOk(): GOResult<T> = GOResult(this, Status.OK)

fun <T> String.asError() = GOResult.error<T>(error = this)


// Async utils
fun ViewModel.ui(block: suspend (CoroutineScope) -> Unit): Job = viewModelScope.launch(
    Dispatchers.Main
) {
    block(this)
}

fun ViewModel.bg(block: suspend (CoroutineScope) -> Unit): Job = viewModelScope.launch(
    Dispatchers.IO
) {
    block(this)
}

fun bg(block: suspend (CoroutineScope) -> Unit): Job = CoroutineScope(Dispatchers.IO).launch {
    block(this)
}

fun ui(block: suspend (CoroutineScope) -> Unit): Job = CoroutineScope(Dispatchers.Main).launch {
    block(this)
}

fun getNewFileInDownloads(owner: String, repoName: String, ext: String): File {
    val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    return File(path, "${owner}_${repoName}_${System.currentTimeMillis()}$ext")
}

fun openDownloadsFolder(context: Context) {
    context.startActivity(
        Intent(DownloadManager.ACTION_VIEW_DOWNLOADS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    )
}