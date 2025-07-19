package com.ecomexpress.oneBoarding.data.datasource.local.preference

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferenceKey {
    val IS_USER_LOGIN = booleanPreferencesKey("is_user_login")
    val ACCESS_TOKEN = stringPreferencesKey("key_access_token")
    val MOBILE_NUMBER = stringPreferencesKey("mobile_number")
    val USER_LATITUDE = doublePreferencesKey("user_latitude")
    val USER_LONGITUDE = doublePreferencesKey("user_longitude")
    val DC_LATITUDE = doublePreferencesKey("dc_latitude")
    val DC_LONGITUDE = doublePreferencesKey("dc_longitude")
    val USER_PINCODE = stringPreferencesKey("user_pincode")
    val USER_DC_ADDRESS = stringPreferencesKey("user_dc_address")
    val ALL_DOCUMENTS_STATUS = stringPreferencesKey("all_documents_status")
    val AADHAAR_STATUS = stringPreferencesKey("aadhaar_status")
    val FRONT_AADHAAR_STATUS = booleanPreferencesKey("front_aadhaar_status")
    val BACK_AADHAAR_STATUS = booleanPreferencesKey("back_aadhaar_status")
    val PAN_STATUS = stringPreferencesKey("pan_status")
    val BANK_STATUS = stringPreferencesKey("bank_status")
    val DRIVING_STATUS = stringPreferencesKey("driving_status")
    val TEMP_EMP_CODE = stringPreferencesKey("temp_empl_code")
    val IS_DC_LOCATION = booleanPreferencesKey("is_dc_location")
    val IS_PROFILE_PIC = stringPreferencesKey("is_profile_pic")
    val FE_NAME = stringPreferencesKey("fe_name")
    val DC_NUMBER = stringPreferencesKey("dc_number")
    val IS_REFER = booleanPreferencesKey("is_refer")
    val DC_STATUS = intPreferencesKey("dc_status")
    val REAPPLY = booleanPreferencesKey("reapply")
    val REAPPLYBTN = booleanPreferencesKey("reapply_btn")
    val REJECTION_REASON = stringPreferencesKey("rejection_reason")
    val TERMS_READ = booleanPreferencesKey("terms_read")
    val FCM_TOKEN = stringPreferencesKey("fcm_token")
    val IS_REFERENCE = booleanPreferencesKey("is_reference")
}
