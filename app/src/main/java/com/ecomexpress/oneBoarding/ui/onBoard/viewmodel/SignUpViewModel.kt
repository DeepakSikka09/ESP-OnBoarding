package com.ecomexpress.oneBoarding.ui.onBoard.viewmodel

import androidx.lifecycle.viewModelScope
import com.ecomexpress.oneBoarding.data.datasource.local.preference.PreferenceKey
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
class SignUpViewModel @Inject constructor(private val repository: OneBoardingRepository) :
    BaseViewModel() {
    private var mutableResponseOTP = MutableStateFlow<Results<Any>>(Results.Empty)
    val commonResponseOTP: StateFlow<Results<Any>> get() = mutableResponseOTP
    private var getNo: String = ""

    fun setMobileNumber(mobileNumber: String) {
        getNo = mobileNumber
    }

    fun generateOTPService(otpRequest: OtpRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getDataStoreContext()
                .putString(PreferenceKey.MOBILE_NUMBER.name, otpRequest.mobile!!)
            mutableResponseOTP.emit(Results.Loading)
            val result = repository.getOTP(otpRequest)
            result.collect {
                mutableResponseOTP.emit(it)
            }
        }
    }

    fun setFcmToken(value: String) {
        viewModelScope.launch {
            repository.getDataStoreContext().putString(
                PreferenceKey.FCM_TOKEN.name, value
            )
        }
    }
}