package com.ecomexpress.oneBoarding.data.datasource.remote.network

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class ApiAuthenticator @Inject constructor(
    @ApplicationContext context: Context
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        return runBlocking {
            val token = ""
            token.let {
                response.request.newBuilder()
                    .header("auth_token", it)
                    .header("Accept","application/json")
                    .build()
            }
        }
    }
}