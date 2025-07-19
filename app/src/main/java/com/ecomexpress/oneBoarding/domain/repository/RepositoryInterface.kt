package com.ecomexpress.oneBoarding.domain.repository

import com.ecomexpress.oneBoarding.data.model.CommonRequest
import com.ecomexpress.oneBoarding.data.model.CommonResponse
import com.ecomexpress.oneBoarding.data.model.onBoarding.DcLocation
import com.ecomexpress.oneBoarding.data.model.onBoarding.IfscResponse
import com.ecomexpress.oneBoarding.data.model.onBoarding.OtpRequest
import com.ecomexpress.oneBoarding.utils.comman.Results
import kotlinx.coroutines.flow.Flow

interface RepositoryInterface {
    suspend fun getBanner(): Flow<Results<CommonResponse>>
    suspend fun getOTP(requestOtp: OtpRequest): Flow<Results<CommonResponse>>
    suspend fun verifyOTP(verifyOtp: OtpRequest): Flow<Results<CommonResponse>>
    suspend fun getDCLocations(
        token: String, commonRequest: CommonRequest
    ): Flow<Results<CommonResponse>>

    suspend fun setManualDocUpdate(
        token: String, commonRequest: CommonRequest
    ): Flow<Results<CommonResponse>>

    suspend fun uploadImages(
        token: String, commonRequest: CommonRequest
    ): Flow<Results<CommonResponse>>

    suspend fun cityUpdate(token: String, request: DcLocation): Flow<Results<CommonResponse>>
    suspend fun referral(token: String, request: CommonRequest): Flow<Results<CommonResponse>>
    suspend fun documentdetails(token: String): Flow<Results<CommonResponse>>
    suspend fun approvalStatus(
        token: String,
        request: CommonRequest?
    ): Flow<Results<CommonResponse>>

    suspend fun checkIfscCode(url: String): Flow<Results<IfscResponse>>
    suspend fun updateTermsConditions(
        token: String,
        request: CommonRequest?
    ): Flow<Results<CommonResponse>>

    suspend fun updateFcmToken(
        token: String,
        request: CommonRequest?
    ): Flow<Results<CommonResponse>>

    suspend fun getReferenceList(token: String):Flow<Results<CommonResponse>>

    suspend fun sendReferenceList(
        token: String,
        request: CommonRequest?
    ): Flow<Results<CommonResponse>>
}
