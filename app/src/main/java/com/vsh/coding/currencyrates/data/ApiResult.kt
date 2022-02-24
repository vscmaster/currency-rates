package com.vsh.coding.currencyrates.data

sealed class ApiResult<out R> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val exception: Exception) : ApiResult<Nothing>()
}

fun <T> ApiResult<T>.successOr(fallback: T): T {
    return (this as? ApiResult.Success<T>)?.data ?: fallback
}
