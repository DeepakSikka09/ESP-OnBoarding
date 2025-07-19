package com.ecomexpress.oneBoarding.ui.onBoard.viewmodel

import androidx.lifecycle.viewModelScope
import com.ecomexpress.oneBoarding.data.datasource.local.preference.PreferenceKey
import com.ecomexpress.oneBoarding.data.model.onBoarding.Data
import com.ecomexpress.oneBoarding.data.model.onBoarding.OtpRequest
import com.ecomexpress.oneBoarding.data.repository.OneBoardingRepository
import com.ecomexpress.oneBoarding.ui.base.BaseViewModel
import com.ecomexpress.oneBoarding.utils.comman.Results
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class OTPVerifyViewModel @Inject constructor(private val repository: OneBoardingRepository) :
    BaseViewModel() {
    private var mutableResponseOTP = MutableStateFlow<Results<Any>>(Results.Empty)
    var responseOTP: StateFlow<Results<Any>> = mutableResponseOTP

    private var mutableResponseVerifyOTP = MutableStateFlow<Results<Any>>(Results.Empty)
    val commonResponseVerifyOTP: StateFlow<Results<Any>> get() = mutableResponseVerifyOTP

    fun setLoginStatus(data: Data) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getDataStoreContext().putBoolean(PreferenceKey.IS_USER_LOGIN.name, true)
            repository.getDataStoreContext().putString(
                PreferenceKey.ACCESS_TOKEN.name, "token" + " " + data.auth_key.toString()
            )
            data.all_documents_status?.let {
                repository.getDataStoreContext().putInt(
                    PreferenceKey.ALL_DOCUMENTS_STATUS.name, it
                )
            }
            repository.getDataStoreContext().putInt(
                PreferenceKey.BANK_STATUS.name, data.bank_status!!
            )
            repository.getDataStoreContext().putInt(
                PreferenceKey.DRIVING_STATUS.name, data.driving_status!!
            )
            repository.getDataStoreContext().putInt(
                PreferenceKey.AADHAAR_STATUS.name, data.aadhaar_status!!
            )
            repository.getDataStoreContext().putInt(
                PreferenceKey.PAN_STATUS.name, data.pan_status!!
            )
            repository.getDataStoreContext().putString(
                PreferenceKey.TEMP_EMP_CODE.name, data.temp_empl_code!!
            )
            repository.getDataStoreContext().putBoolean(
                PreferenceKey.IS_DC_LOCATION.name, data.is_dc_location!!
            )
            repository.getDataStoreContext()
                .putString(PreferenceKey.USER_DC_ADDRESS.name, data.dc_location_details!!)
            repository.getDataStoreContext()
                .putDouble(PreferenceKey.DC_LATITUDE.name, data.dc_lat!!)
            repository.getDataStoreContext()
                .putDouble(PreferenceKey.DC_LONGITUDE.name, data.dc_long!!)
            repository.getDataStoreContext().putInt(
                PreferenceKey.IS_PROFILE_PIC.name, data.is_profile!!
            )
            repository.getDataStoreContext().putString(
                PreferenceKey.DC_NUMBER.name,
                data.dc_number!!
            )
            repository.getDataStoreContext().putInt(
                PreferenceKey.DC_STATUS.name,
                data.dc_status!!
            )
            repository.getDataStoreContext().putBoolean(
                PreferenceKey.REAPPLY.name,
                data.reapply!!
            )
            repository.getDataStoreContext().putBoolean(
                PreferenceKey.REAPPLYBTN.name,
                true
            )
            if (data.dc_status != 0) {
                repository.getDataStoreContext().putBoolean(
                    PreferenceKey.IS_REFER.name,
                    true
                )
            } else {
                repository.getDataStoreContext().putBoolean(
                    PreferenceKey.IS_REFER.name,
                    false
                )
            }
        }
    }


    fun verifyOTPService(otpRequest: OtpRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            mutableResponseVerifyOTP.emit(Results.Loading)
            val result = repository.verifyOTP(otpRequest)
            result.collect {
                mutableResponseVerifyOTP.emit(it)
            }
        }
    }

    fun generateOTPService(otpRequest: OtpRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            mutableResponseOTP.emit(Results.Loading)
            val result = repository.getOTP(otpRequest)
            result.collect {
                mutableResponseOTP.emit(it)
            }
        }
    }
}