package com.ecomexpress.oneBoarding.data.datasource.remote.network

import android.content.Context
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import com.ecomexpress.oneBoarding.BuildConfig
import com.ecomexpress.oneBoarding.utils.comman.CommonUtils.getDeviceID
import com.ecomexpress.oneBoarding.utils.comman.CommonUtils.getDeviceModel
import com.ecomexpress.oneBoarding.utils.comman.CommonUtils.getDeviceName
import com.ecomexpress.oneBoarding.utils.comman.CommonUtils.getDeviceVersion
import java.util.concurrent.TimeUnit

class RetrofitClient @Inject constructor() {

    fun <Api> buildApi(
        api: Class<Api>,
        context: Context,
    ): Api {
        val authenticator = ApiAuthenticator(context)
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(getRetrofitClient(authenticator, context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(api)
    }

    private fun getRetrofitClient(
        authenticator: Authenticator? = null,
        context: Context
    ): OkHttpClient {

        return OkHttpClient.Builder().connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(2, TimeUnit.MINUTES)
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder().also
                    {
                        it.addHeader("device-id", getDeviceID(context.contentResolver!!))
                        it.addHeader("device-name", getDeviceName())
                        it.addHeader("device-model", getDeviceModel())
                        it.addHeader("device-version", getDeviceVersion())
                        it.addHeader("app-version",BuildConfig.VERSION_NAME)
                    }.build()
                )
            }.also { client ->
                authenticator?.let { client.authenticator(it) }
                if (BuildConfig.DEBUG) {
                    val logging = HttpLoggingInterceptor()
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY)
                    client.addInterceptor(logging)
                    client.addNetworkInterceptor(CacheInterceptor())
                }
            }.build()
    }
}

