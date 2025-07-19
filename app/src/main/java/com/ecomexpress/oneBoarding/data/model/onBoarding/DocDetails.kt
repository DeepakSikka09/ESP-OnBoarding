package com.ecomexpress.oneBoarding.data.model.onBoarding

data class DocDetails(

    var pancardno: String? = "",
    var accountno: String? = "",
    var ifsccode: String? = "",
    var dlno: String? = "",
    var dlexpiry: String? = "",
    var account_holder_name:String?="",
    var aadharno: String? = "",
    var aadharname: String? = "",
    var dob: String? = "",
    var gender:String?="",

    var permanent_address: String? = "",
    var permanent_pincode: String? = "",
    var permanent_state: String? = "",
    var current_address: String? = "",
    var current_state: String? = "",
    var current_pincode: String? = "",

    var is_pan: Boolean? = true,
    var is_dl: Boolean? = true,
    var img_aadhar_first:String? = "",
    var img_aadhar_second:String? = "",
    var img_profile:String? = "",
    var img_cheque:String? = "",
    var is_profile:Int?=0
)