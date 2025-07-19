package com.ecomexpress.oneBoarding.data.model

import androidx.annotation.Keep
import com.ecomexpress.oneBoarding.data.model.onBoarding.Data

@Keep
data class CommonResponse(
    val code: Int,
    val data: Data?,
    val description: String,
    val success: Boolean,
)