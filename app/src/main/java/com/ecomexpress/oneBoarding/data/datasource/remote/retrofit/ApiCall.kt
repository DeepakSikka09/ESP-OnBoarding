package com.ecomexpress.oneBoarding.data.datasource.remote.retrofit

import com.ecomexpress.oneBoarding.utils.comman.AppConstants
import com.ecomexpress.oneBoarding.utils.comman.AppConstants.UNEXPECTED_ERROR
import com.ecomexpress.oneBoarding.utils.comman.DataSourceException
import com.ecomexpress.oneBoarding.utils.comman.Results
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface ApiCall {
    suspend fun <T> apiCall(apiCall: suspend () -> T): Results<T> {
        return withContext(Dispatchers.IO) {
            var message = UNEXPECTED_ERROR
            try {
                Results.Success(apiCall.invoke())
            } catch (e: Throwable) {
                val errorData = e.localizedMessage?.split(" ")
                if (errorData!!.size >= 2) {
                    when (errorData[1]) {
                        "504" -> {
                            message = AppConstants.NO_INTERNET
                        }

                        "107" -> {
                            message = AppConstants.SESSION_EXPIRED
                        }

                        "400" -> {
                            message = AppConstants.BAD_GATEWAY
                        }

                        "401" -> {
                            message = AppConstants.UNAUTHORIZED
                        }

                        "402" -> {
                            message = AppConstants.PAYMENT_REQUIRED
                        }

                        "403" -> {
                            message = AppConstants.FORBIDDEN
                        }

                        "404" -> {
                            message = AppConstants.SERVER_ERROR
                        }

                        "408" -> {
                            message = AppConstants.REQUEST_NOT_FOUND
                        }

                        "414" -> {
                            message = AppConstants.URI_TO_LONG
                        }

                        "500" -> {
                            message = AppConstants.INTERNAL_SERVER_ERROR
                        }

                        "502" -> {
                            message = AppConstants.BAD_GATEWAY
                        }

                        "505" -> {
                            message = AppConstants.HTTP_VERSION
                        }

                        else -> {
                            if (e.localizedMessage!!.contains("Unable to resolve host")) {
                                message = AppConstants.NO_INTERNET
                            } else if (e.toString().contains("JsonSyntaxException")) {
                                message = AppConstants.PARSING_ISSUE
                            }

                        }
                    }
                }
                Results.Error(DataSourceException.Unexpected(message))
            }
        }
    }
}