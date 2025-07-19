package com.ecomexpress.oneBoarding.data.datasource.remote.network

import android.util.Log
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

class CacheInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response: Response = chain.proceed(chain.request())
        val cacheControl = CacheControl.Builder()
            .maxAge(10, TimeUnit.DAYS)
            .build()
        val finalResponse = response.newBuilder()
            .header("Cache-Control", "public," + cacheControl.toString())
            .build()
        Log.d("reponseCachenetwork", finalResponse.toString())
        return finalResponse
    }
}