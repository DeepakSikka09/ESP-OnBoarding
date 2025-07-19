package com.ecomexpress.oneBoarding.data.model.onBoarding

import androidx.annotation.Keep

@Keep
data class LanguageListing(
    var title: String? = "",
    var description: String? = "",
    var imageId: Int? = 0,
    var isSelected: Boolean = false

)