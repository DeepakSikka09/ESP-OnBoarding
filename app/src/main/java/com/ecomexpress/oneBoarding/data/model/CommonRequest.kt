package com.ecomexpress.oneBoarding.data.model

import androidx.annotation.Keep
import com.ecomexpress.oneBoarding.data.model.onBoarding.ReferenceModel

@Keep
data class CommonRequest(

    //generate and verify OTP request
    var mobileNumber: String? = "",
    var OTP: String? = "",

    //dc location request
    var pin_code: String? = "",
    var user_lat: String? = "",
    var user_lng: String? = "",

    //manual doc update request
    var doc_number: String? = "",
    var ifsc_code: String? = "",
    var user_name: String? = "",
    var dob: String? = "", // 31 Jan 1992
    var gender: String? = "",
    var doc_type: String? = "", // aadhaar/pan/bank/dl
    var is_pan: Boolean? = true,
    var is_dl: Boolean? = true,
    var expiry_date: String? = "", // 10/10/2022
    var permanent_address: String? = "",
    var current_address: String? = "",
    var img_type: String? = "",//for aadhaar 1 and aadhaar 2

    var address: String? = "",
    var img: String? = "",
    var state: String? = "",
    var is_address: Boolean? = false,

    //Referral request
    var referral_code: String? = "",
    var current_state: String? = "",
    var current_pincode: String? = "",
    var permanent_pincode: String? = "",
    var permanent_state: String? = "",
    var temp_empl_code: String? = "",
    var bank_name: String? = "",
    var fcm_token : String = "",

    //terms conditions checkbox status
    var terms_check: Int = 0,

    var referral:List<ReferenceModel> = listOf()
)