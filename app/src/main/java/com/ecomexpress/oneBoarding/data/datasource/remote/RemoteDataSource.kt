package com.ecomexpress.oneBoarding.data.datasource.remote

import com.ecomexpress.oneBoarding.data.datasource.remote.retrofit.ApiCall
import com.ecomexpress.oneBoarding.data.datasource.remote.retrofit.IRestHelper
import com.ecomexpress.oneBoarding.data.datasource.remote.retrofit.RestApi
import com.ecomexpress.oneBoarding.data.model.CommonRequest
import com.ecomexpress.oneBoarding.data.model.CommonResponse
import com.ecomexpress.oneBoarding.data.model.onBoarding.DcLocation
import com.ecomexpress.oneBoarding.data.model.onBoarding.IfscResponse
import com.ecomexpress.oneBoarding.data.model.onBoarding.OtpRequest
import com.ecomexpress.oneBoarding.utils.comman.Results
import javax.inject.Inject

class RemoteDataSource @Inject constructor(private val restApi: RestApi) : IRestHelper, ApiCall {

    override suspend fun getBanner(): Results<CommonResponse> {
        return apiCall { restApi.getBanner() }
    }

    override suspend fun getOTP(otpRequest: OtpRequest): Results<CommonResponse> {
        return apiCall { restApi.getOTP(otpRequest) }
    }

    override suspend fun verifyOTP(otpVerifyRequest: OtpRequest): Results<CommonResponse> {
        return apiCall { restApi.verifyOTP(otpVerifyRequest) }
    }

    override suspend fun getDCLocations(
        token: String,
        commonRequest: CommonRequest
    ): Results<CommonResponse> {
        return apiCall { restApi.getDCLocations(token, commonRequest) }
    }

    override suspend fun setManualDocUpdate(
        token: String,
        commonRequest: CommonRequest
    ): Results<CommonResponse> {
        return apiCall { restApi.setManualDocUpdate(token, commonRequest) }
    }

    override suspend fun uploadImages(
        token: String,
        commonRequest: CommonRequest
    ): Results<CommonResponse> {
        return apiCall { restApi.uploadImages(token, commonRequest) }
    }

    override suspend fun cityUpdate(token: String, request: DcLocation): Results<CommonResponse> {
        return apiCall { restApi.cityUpdate(token, request) }
    }

    override suspend fun referral(token: String, request: CommonRequest): Results<CommonResponse> {
        return apiCall { restApi.referral(token, request) }
    }

    override suspend fun documentDetails(
        token: String
    ): Results<CommonResponse> {
        return apiCall { restApi.documentDetails(token) }
    }

    override suspend fun approvalStatus(
        token: String,
        request: CommonRequest?
    ): Results<CommonResponse> {
        return apiCall { restApi.approvalStatus(token, request) }
    }

    override suspend fun checkIfscCode(url: String): Results<IfscResponse> {
        return apiCall { restApi.checkIfscCode(url) }
    }

    override suspend fun updateTermsConditions(
        token: String,
        request: CommonRequest?
    ): Results<CommonResponse> {
        return apiCall { restApi.updateTermsConditions(token, request) }
    }

    override suspend fun updateFcmToken(
        token: String,
        request: CommonRequest?
    ): Results<CommonResponse> {
        return apiCall { restApi.updateFcmToken(token, request) }
    }

    override suspend fun getReferenceList(token: String): Results<CommonResponse> {
        return apiCall { restApi.getReferenceList(token) }
    }

    override suspend fun sendReferenceList(
        token: String,
        request: CommonRequest?
    ): Results<CommonResponse> {
        return apiCall { restApi.sendReferenceList(token, request) }
    }
}
