package nick.mirosh.repository

sealed class Result<out T> {
    /**
     * Represents successful data.
     *
     * @property data Data of generic type T.
     */
    data class Success<out T>(val data: T) : Result<T>()

    /**
     * Represents a failure.
     *
     * @property error The type of error, represented by ErrorType.
     */
    data class Error(val throwable: Throwable) : Result<Nothing>()
}