package com.ecomexpress.oneBoarding.utils.comman


sealed class DataSourceException(override val message: String?) : RuntimeException() {
    class Unexpected(override val message: String) : DataSourceException(message)
}
