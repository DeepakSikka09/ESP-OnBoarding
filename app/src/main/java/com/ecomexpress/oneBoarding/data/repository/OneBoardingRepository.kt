package com.ecomexpress.oneBoarding.data.repository

import android.content.Context
import com.ecomexpress.oneBoarding.data.datasource.local.preference.SharedPreference
import com.ecomexpress.oneBoarding.data.datasource.remote.RemoteDataSource
import com.ecomexpress.oneBoarding.data.model.CommonRequest
import com.ecomexpress.oneBoarding.data.model.CommonResponse
import com.ecomexpress.oneBoarding.data.model.onBoarding.DcLocation
import com.ecomexpress.oneBoarding.data.model.onBoarding.IfscResponse
import com.ecomexpress.oneBoarding.data.model.onBoarding.OtpRequest
import com.ecomexpress.oneBoarding.domain.repository.RepositoryInterface
import com.ecomexpress.oneBoarding.utils.comman.Results
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class OneBoardingRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    @ApplicationContext private val context: Context,
    private val dispatcher: CoroutineDispatcher
) : RepositoryInterface {

    fun getDataStoreContext() = SharedPreference(context = context)

    override suspend fun getBanner(): Flow<Results<CommonResponse>> {
        return flow {
            emit(remoteDataSource.getBanner())
        }.flowOn(dispatcher)
    }


    override suspend fun getOTP(requestOtp: OtpRequest): Flow<Results<CommonResponse>> {
        return flow {
            emit(remoteDataSource.getOTP(requestOtp))
        }.flowOn(dispatcher)
    }

    override suspend fun verifyOTP(verifyOtp: OtpRequest): Flow<Results<CommonResponse>> {
        return flow {
            emit(remoteDataSource.verifyOTP(verifyOtp))
        }.flowOn(dispatcher)
    }

    override suspend fun getDCLocations(
        token: String,
        commonRequest: CommonRequest
    ): Flow<Results<CommonResponse>> {
        return flow {
            emit(remoteDataSource.getDCLocations(token, commonRequest))
        }.flowOn(dispatcher)
    }

    override suspend fun setManualDocUpdate(
        token: String,
        commonRequest: CommonRequest
    ): Flow<Results<CommonResponse>> {
        return flow {
            emit(remoteDataSource.setManualDocUpdate(token, commonRequest))
        }.flowOn(dispatcher)
    }

    override suspend fun uploadImages(
        token: String,
        commonRequest: CommonRequest
    ): Flow<Results<CommonResponse>> {
        return flow {
            emit(remoteDataSource.uploadImages(token, commonRequest))
        }.flowOn(dispatcher)
    }

    override suspend fun cityUpdate(
        token: String,
        request: DcLocation
    ): Flow<Results<CommonResponse>> {
        return flow {
            emit(remoteDataSource.cityUpdate(token, request))
        }.flowOn(dispatcher)
    }

    override suspend fun referral(
        token: String,
        request: CommonRequest
    ): Flow<Results<CommonResponse>> {
        return flow {
            emit(remoteDataSource.referral(token, request))
        }.flowOn(dispatcher)
    }

    override suspend fun documentdetails(
        token: String
    ): Flow<Results<CommonResponse>> {
        return flow {
            emit(remoteDataSource.documentDetails(token))
        }.flowOn(dispatcher)
    }

    override suspend fun approvalStatus(
        token: String,
        request: CommonRequest?
    ): Flow<Results<CommonResponse>> {
        return flow {
            emit(remoteDataSource.approvalStatus(token, request))
        }.flowOn(dispatcher)
    }

    override suspend fun checkIfscCode(url: String): Flow<Results<IfscResponse>> {
        return flow {
            emit(remoteDataSource.checkIfscCode(url))
        }.flowOn(dispatcher)
    }

    override suspend fun updateTermsConditions(
        token: String,
        request: CommonRequest?
    ): Flow<Results<CommonResponse>> {
        return flow {
            emit(remoteDataSource.updateTermsConditions(token, request))
        }.flowOn(dispatcher)
    }

    override suspend fun updateFcmToken(
        token: String,
        request: CommonRequest?
    ): Flow<Results<CommonResponse>> {
        return flow {
            emit(remoteDataSource.updateFcmToken(token, request))
        }.flowOn(dispatcher)
    }

    override suspend fun getReferenceList(token: String): Flow<Results<CommonResponse>> {
        return flow {
            emit(remoteDataSource.getReferenceList(token))
        }.flowOn(dispatcher)
    }

    override suspend fun sendReferenceList(
        token: String,
        request: CommonRequest?
    ): Flow<Results<CommonResponse>> {
        return flow {
            emit(remoteDataSource.sendReferenceList(token, request))
        }.flowOn(dispatcher)
    }
}

