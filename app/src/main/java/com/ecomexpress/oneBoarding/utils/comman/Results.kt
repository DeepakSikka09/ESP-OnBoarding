package com.ecomexpress.oneBoarding.utils.comman

sealed class Results<out T> {
    data class Success<out T>(val response: T) : Results<T>()
    data class Error(val exception: DataSourceException) : Results<Nothing>()
    object Loading : Results<Nothing>()
    object Empty: Results<Nothing>()
}

inline fun <T : Any> Results<T>.onSuccess(action: (T) -> Unit): Results<T> {
    if (this is Results.Success) action(response)
    return this
}

inline fun <T : Any> Results<T>.onError(action: (DataSourceException) -> Unit): Results<T> {
    if (this is Results.Error) action(exception)
    return this
}

inline fun <T : Any> Results<T>.onLoading(action: () -> Unit): Results<T> {
    if (this is Results.Loading) action()
    return this
}
