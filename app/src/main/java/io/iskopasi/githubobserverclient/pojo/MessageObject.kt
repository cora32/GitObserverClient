package io.iskopasi.githubobserverclient.pojo

enum class MessageType() {
    Info,
    Error
}

data class MessageObject(
    val type: MessageType,
    val data: String
)