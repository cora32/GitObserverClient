package io.iskopasi.githubobserverclient.models


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.iskopasi.githubobserverclient.modules.IoDispatcher
import io.iskopasi.githubobserverclient.modules.MainDispatcher
import io.iskopasi.githubobserverclient.pojo.MessageObject
import io.iskopasi.githubobserverclient.pojo.MessageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

open class BaseViewModel(
    context: Application,
    @IoDispatcher private val ioDispatcher: CoroutineContext,
    @MainDispatcher private val mainDispatcher: CoroutineContext
) : AndroidViewModel(context) {
    private val _messageFlow = MutableSharedFlow<MessageObject?>()
    val messageFlow: SharedFlow<MessageObject?> = _messageFlow

    private fun emitMessage(message: MessageObject) = viewModelScope.launch {
        _messageFlow.emit(message)
    }

    protected fun info(message: String) = viewModelScope.launch {
        emitMessage(MessageObject(MessageType.Info, message))
    }

    protected fun error(message: String) = viewModelScope.launch {
        emitMessage(MessageObject(MessageType.Error, message))
    }

    // Runs block in a default coroutine and handle all uncaught exceptions
    protected fun bg(block: suspend (CoroutineScope) -> Unit): Job =
        viewModelScope.launch(ioDispatcher) {
            try {
                block(this)
            } catch (ex: Exception) {
                ex.printStackTrace()
                error("Error: ${ex.message}")
            }
        }
}