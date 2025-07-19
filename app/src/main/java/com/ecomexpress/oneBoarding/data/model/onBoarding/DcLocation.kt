package com.ecomexpress.oneBoarding.data.model.onBoarding

import androidx.annotation.Keep

@Keep

data class DcLocation(
    var address: String,
    val center_name: String,
    val center_shortcode: String,
    val city: String,
    val city_id: Int,
    val dc_lat: Double,
    val dc_long: Double,
    val distance_from_user: String,
    val state: String,
    val success: Boolean,
    var isSelected: Boolean,
    var address_list: String,
    var pin_code: String,
    var dc_number: String
)