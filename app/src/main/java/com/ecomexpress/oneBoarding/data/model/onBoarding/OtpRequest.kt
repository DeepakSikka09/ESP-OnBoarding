package com.ecomexpress.oneBoarding.data.model.onBoarding

import androidx.annotation.Keep

@Keep

/*we should use keep annotation as we want this class to be retained in the final APK as sometimes
in Android development where the build process often includes these tools to reduce the size of
the APK by removing unused code and resources.*/

data class OtpRequest(
    var device_id: String? = "",
    var device_version: String? = "",
    var device_name: String? = "",
    var mobile: String? = "",
    var otp: String? = "",
    var device_model: String? = "",
    var fcm_token: String? = ""

)