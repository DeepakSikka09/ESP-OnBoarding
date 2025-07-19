package com.ecomexpress.oneBoarding.ui.onBoard.viewmodel

import androidx.lifecycle.viewModelScope
import com.ecomexpress.oneBoarding.data.datasource.local.preference.PreferenceKey
import com.ecomexpress.oneBoarding.data.model.CommonRequest
import com.ecomexpress.oneBoarding.data.model.onBoarding.Data
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
class FinalStatusViewModel @Inject constructor(private val repository: OneBoardingRepository) :
    BaseViewModel() {
    private val mutableFinalStatusFlow = MutableStateFlow<Results<Any>>(Results.Empty)
    val finalStatusFlow: StateFlow<Results<Any>> get() = mutableFinalStatusFlow

    private val mutableFcmStateFlow = MutableStateFlow<Results<Any>>(Results.Empty)
    val fcmStatusFlow: StateFlow<Results<Any>> get() = mutableFcmStateFlow

    fun getTempId(): String {
        var value = ""
        viewModelScope.launch {
            value = repository.getDataStoreContext().getString(PreferenceKey.TEMP_EMP_CODE.name)
                .toString()
        }
        return "ID: $value"
    }

    fun getFinalStatus() {
        viewModelScope.launch {
            mutableFinalStatusFlow.emit(Results.Loading)
            val result = repository.approvalStatus(
                repository.getDataStoreContext().getString(PreferenceKey.ACCESS_TOKEN.name)!!,
                CommonRequest(
                    temp_empl_code = repository.getDataStoreContext()
                        .getString(PreferenceKey.TEMP_EMP_CODE.name)!!
                )
            )
            result.collect {
                mutableFinalStatusFlow.emit(
                    it
                )
            }
        }
    }

    fun updateDocumentStatus(data: Data) {
        viewModelScope.launch(Dispatchers.IO) {
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
                PreferenceKey.DC_NUMBER.name, data.dc_number!!
            )
            repository.getDataStoreContext().putInt(
                PreferenceKey.DC_STATUS.name, data.dc_status!!
            )
            repository.getDataStoreContext().putBoolean(
                PreferenceKey.REAPPLY.name, data.reapply!!
            )
            repository.getDataStoreContext().putString(
                PreferenceKey.TEMP_EMP_CODE.name, data.temp_empl_code!!
            )
            updateFeName(data.username!!)
        }
    }

    fun setDcLatLong(dcLong: Double, dcLat: Double) {
        viewModelScope.launch {
            repository.getDataStoreContext().putDouble(PreferenceKey.DC_LONGITUDE.name, dcLong)
            repository.getDataStoreContext().putDouble(PreferenceKey.DC_LATITUDE.name, dcLat)
        }
    }

    fun setReferral() {
        viewModelScope.launch {
            repository.getDataStoreContext().putBoolean(PreferenceKey.IS_REFER.name, true)
        }
    }

    fun setBtnClicked(value: Boolean) {
        viewModelScope.launch {
            repository.getDataStoreContext().putBoolean(
                PreferenceKey.REAPPLYBTN.name, value
            )
        }
    }

    fun getbooleanValue(key: String): Boolean? {
        var result: Boolean? = null
        viewModelScope.launch {
            result = repository.getDataStoreContext().getBoolean(
                key
            )
        }
        return result
    }

    fun getStringValue(key: String): String? {
        var result: String? = null
        viewModelScope.launch {
            result = repository.getDataStoreContext().getString(key)
        }
        return result
    }

    fun updateFeName(feName: String) {
        viewModelScope.launch {
            repository.getDataStoreContext().putString(PreferenceKey.FE_NAME.name, feName)
        }
    }

    fun setRejectionReason(reason: String) {
        viewModelScope.launch {
            repository.getDataStoreContext().putString(
                PreferenceKey.REJECTION_REASON.name, reason
            )
        }
    }

    fun setFcmToken(value: String) {
        viewModelScope.launch {
            repository.getDataStoreContext().putString(
                PreferenceKey.FCM_TOKEN.name, value
            )
        }
    }

    fun updateApiFcmToken(fcmToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.updateFcmToken(
                repository.getDataStoreContext().getString(PreferenceKey.ACCESS_TOKEN.name).toString(),
                CommonRequest(
                    fcm_token = fcmToken
                )
            )
            result.collect {
                mutableFcmStateFlow.emit(it)
            }
        }
    }
}