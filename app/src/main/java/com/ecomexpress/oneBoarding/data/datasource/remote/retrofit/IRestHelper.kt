package com.ecomexpress.oneBoarding.data.datasource.remote.retrofit

import com.ecomexpress.oneBoarding.data.model.CommonRequest
import com.ecomexpress.oneBoarding.data.model.CommonResponse
import com.ecomexpress.oneBoarding.data.model.onBoarding.DcLocation
import com.ecomexpress.oneBoarding.data.model.onBoarding.IfscResponse
import com.ecomexpress.oneBoarding.data.model.onBoarding.OtpRequest
import com.ecomexpress.oneBoarding.utils.comman.Results

interface IRestHelper {
    suspend fun getBanner(): Results<CommonResponse>
    suspend fun getOTP(otpRequest: OtpRequest): Results<CommonResponse>
    suspend fun verifyOTP(otpVerifyRequest: OtpRequest): Results<CommonResponse>
    suspend fun getDCLocations(token: String, commonRequest: CommonRequest): Results<CommonResponse>
    suspend fun setManualDocUpdate(
        token: String,
        commonRequest: CommonRequest
    ): Results<CommonResponse>

    suspend fun uploadImages(token: String, commonRequest: CommonRequest): Results<CommonResponse>
    suspend fun cityUpdate(token: String, request: DcLocation): Results<CommonResponse>
    suspend fun referral(token: String, request: CommonRequest): Results<CommonResponse>
    suspend fun documentDetails(token: String): Results<CommonResponse>
    suspend fun approvalStatus(token: String, request: CommonRequest?): Results<CommonResponse>
    suspend fun checkIfscCode(url: String): Results<IfscResponse>
    suspend fun updateTermsConditions(
        token: String,
        request: CommonRequest?
    ): Results<CommonResponse>

    suspend fun updateFcmToken(
        token: String,
        request: CommonRequest?
    ):Results<CommonResponse>

    suspend fun getReferenceList(token: String): Results<CommonResponse>

    suspend fun sendReferenceList(token: String, request: CommonRequest?): Results<CommonResponse>
}