package com.ecomexpress.oneBoarding.di

import android.annotation.SuppressLint
import android.content.Context
import com.ecomexpress.oneBoarding.data.datasource.remote.network.RetrofitClient
import com.ecomexpress.oneBoarding.data.datasource.remote.retrofit.RestApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideAuthApi(
        remoteDataSource: RetrofitClient, @ApplicationContext context: Context
    ): RestApi {
        return remoteDataSource.buildApi(RestApi::class.java, context)
    }

    @SuppressLint("MissingPermission", "HardwareIds")
    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    fun dispatchers(): CoroutineDispatcher = Dispatchers.IO

}
