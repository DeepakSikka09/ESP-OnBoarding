package com.ecomexpress.oneBoarding.data.model.onBoarding

import androidx.annotation.Keep

@Keep
data class Data(
    // banner api
    val banner_data: List<BannerData>?= listOf(),

    // verify otp api
    val auth_key: String? = "",
    val all_documents_upload: Int? = 0,
    val aadhaar_status: Int? = 0,
    val bank_account_status: Int? = 0,
    val pan_status: Int? = 0,
    val vender_code: Int? = 0,
    val temp_empl_code: String? = "",
    val driving_status: Int? = 0,
    val empl_type: String? = "",
    val bank_status: Int? = 0,
    val dc_location_details: String = "",
    val is_profile: Int? = 0,
    val dc_number: String? = "",
    // dc locations
    val dc_location: List<DcLocation>? = listOf(),

    //send otp timer
    val delay_time: Int? =0,
    val time_difference: Int? =0,
    //aadhaar card response
    val doc_type: String? = "",
    val doc_number: String? = "",
    val doc_name: String? = "",
    val DOB: String? = "",
    val gender: String? = "",
    val address: String? = "",
    val expiry_date: String? = "",
    val ifsc_code: String? = "",
    val account_holder_name: String? = "",
    val permanent_address: String? = "",
    val current_address: String? = "",
    val retry: Boolean = false,
    val permanent_pincode: String? = "",
    val permanent_state: String? = "",
    val all_documents_status: Int? = 0,

    //document details response
    val aadhar_number: String? = "",
    val aadhar_name: String? = "",
    val pan_number: String? = "",
    val is_pan: Boolean = false,
    val dl_number: String? = "",
    val is_dl: Boolean?=false,
    val dob: String? = "",
    val bank_number: String? = "",
    val current_state: String? = "",
    val current_pincode: String? = "",
    val is_dc_location: Boolean?= false,

    //approvalStatus
    val dc_status: Int? = 0,
    val hr_status: Int? = 0,
    val reasons: List<String>? = listOf(),
    var img_aadhar_first: String? = "",
    var img_aadhar_second: String? = "",
    var img_profile: String? = "",
    var img_cheque: String? = "",
    var username: String? = "",
    var dc_lat: Double? = 0.0,
    var dc_long: Double? = 0.0,
    var reapply: Boolean? = null,
    val app_url_sathi: String? = null,
    val app_url_sruti: String? = null,
    val employee_terms_conditions_status: Int? = 0,
    val employee_terms_conditions: String? = null,
    val employment_type: String? = null,

    //docs linking
    var isLinking: Boolean? = false,
    var docLink: Boolean? = false,

    var invalid_users:List<String> = listOf(""),
    val referral: List<ReferenceModel> = listOf(),
)