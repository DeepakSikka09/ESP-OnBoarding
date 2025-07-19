package com.ecomexpress.oneBoarding.data.datasource.remote.retrofit

import com.ecomexpress.oneBoarding.data.model.CommonRequest
import com.ecomexpress.oneBoarding.data.model.CommonResponse
import com.ecomexpress.oneBoarding.data.model.onBoarding.DcLocation
import com.ecomexpress.oneBoarding.data.model.onBoarding.IfscResponse
import com.ecomexpress.oneBoarding.data.model.onBoarding.OtpRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url


interface RestApi {

    @POST("/v2/onboarding/getBanner/")
    suspend fun getBanner(): CommonResponse

    @POST("/v2/onboarding/generateOtp/")
    suspend fun getOTP(@Body request: OtpRequest?): CommonResponse

    @POST("/v2/onboarding/verifyOtp/")
    suspend fun verifyOTP(@Body request: OtpRequest?): CommonResponse

    @POST("/v2/onboarding/deliverypartnerserviceability/")
    suspend fun getDCLocations(
        @Header("Authorization") token: String, @Body request: CommonRequest?
    ): CommonResponse

    @POST("/v2/onboarding/manualDocUpdate/")
    suspend fun setManualDocUpdate(
        @Header("Authorization") token: String, @Body request: CommonRequest?
    ): CommonResponse

    @POST("/v2/onboarding/uploadDocuments/")
    suspend fun uploadImages(
        @Header("Authorization") token: String, @Body request: CommonRequest?
    ): CommonResponse

    @POST("/v2/onboarding/cityupdate/")
    suspend fun cityUpdate(
        @Header("Authorization") token: String, @Body request: DcLocation?
    ): CommonResponse

    @POST("/v2/onboarding/referral/")
    suspend fun referral(
        @Header("Authorization") token: String, @Body request: CommonRequest?
    ): CommonResponse

    @POST("/v2/onboarding/document_details/")
    suspend fun documentDetails(@Header("Authorization") token: String): CommonResponse

    @POST("/v2/onboarding/approval_status/")
    suspend fun approvalStatus(
        @Header("Authorization") token: String, @Body request: CommonRequest?
    ): CommonResponse

    @GET
    suspend fun checkIfscCode(@Url url: String): IfscResponse

    @POST("/v2/onboarding/terms-accepted/")
    suspend fun updateTermsConditions(
        @Header("Authorization") token: String, @Body request: CommonRequest?
    ): CommonResponse

    @POST("/v2/onboarding/update_fcm_token/")
    suspend fun updateFcmToken(
        @Header("Authorization") token: String, @Body request: CommonRequest?
    ) :CommonResponse

    @GET("/v2/onboarding/get_referral/")
    suspend fun getReferenceList(
        @Header("Authorization") token: String
    ) :CommonResponse

    @POST("/v2/onboarding/send_referral/")
    suspend fun sendReferenceList(
        @Header("Authorization") token: String, @Body request: CommonRequest?
    ) :CommonResponse
}