package otus.homework.coroutines

sealed class Result<T> {
    data class Success<T>(val data: T): Result<T>()
    data class Error<T>(val error: Throwable): Result<T>()
}
