package io.iskopasi.githubobserverclient.pojo

enum class Status() {
    OK,
    Error,
    Unknown,
}

data class GOResult<T>(
    val data: T? = null,
    val status: Status = Status.Unknown,
    val error: String = ""
) {
    companion object {
        fun <T> error(error: String): GOResult<T> = GOResult<T>(
            status = Status.Error,
            error = error
        )
    }
}