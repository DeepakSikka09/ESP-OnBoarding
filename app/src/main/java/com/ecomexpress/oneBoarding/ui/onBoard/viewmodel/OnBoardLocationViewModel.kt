package com.ecomexpress.oneBoarding.ui.onBoard.viewmodel

import androidx.lifecycle.viewModelScope
import com.ecomexpress.oneBoarding.data.datasource.local.preference.PreferenceKey
import com.ecomexpress.oneBoarding.data.model.CommonRequest
import com.ecomexpress.oneBoarding.data.model.onBoarding.DcLocation
import com.ecomexpress.oneBoarding.data.repository.OneBoardingRepository
import com.ecomexpress.oneBoarding.ui.base.BaseViewModel
import com.ecomexpress.oneBoarding.utils.comman.Results
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnBoardLocationViewModel @Inject constructor(private val repository: OneBoardingRepository) :
    BaseViewModel() {

    private var mutableDCLocationResponse = MutableStateFlow<Results<Any>>(Results.Empty)
    val commonDCLocationResponse: StateFlow<Results<Any>> get() = mutableDCLocationResponse
    private var mutableCityUpdateResponse = MutableStateFlow<Results<Any>>(Results.Empty)
    val commonCityUpdateResponse: StateFlow<Results<Any>> get() = mutableCityUpdateResponse
    private var latitudeMutable = MutableStateFlow(0.0)
    val latitude = latitudeMutable.asStateFlow()
    private var longitudeMutable = MutableStateFlow(0.0)
    val longitude = longitudeMutable.asStateFlow()
    private var pinCodeMutable = MutableStateFlow("")
    val pinCode = pinCodeMutable.asStateFlow()

    // get near by Dc locations
    fun callToNearByDCLocations(commonRequest: CommonRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            mutableDCLocationResponse.emit(Results.Loading)
            val result = repository.getDCLocations(
                repository.getDataStoreContext().getString(PreferenceKey.ACCESS_TOKEN.name)!!,
                commonRequest
            )
            result.collect {
                mutableDCLocationResponse.emit(it)
            }
        }
    }

    fun getLocationForPinCode() {
        viewModelScope.launch(Dispatchers.IO) {
            latitudeMutable.emit(
                repository.getDataStoreContext().getDouble(PreferenceKey.USER_LATITUDE.name) ?: 0.0
            )
            longitudeMutable.emit(
                repository.getDataStoreContext().getDouble(PreferenceKey.USER_LONGITUDE.name) ?: 0.0
            )
            pinCodeMutable.emit(
                repository.getDataStoreContext().getString(PreferenceKey.USER_PINCODE.name) ?: ""
            )
        }
    }

    fun setDcAddressLocation(address: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getDataStoreContext().putString(
                PreferenceKey.USER_DC_ADDRESS.name,
                address
            )
        }
    }

    fun setCityUpdate(dcLocation: DcLocation) {
        setDcLatLong(dcLocation.dc_lat, dcLocation.dc_long, dcLocation.dc_number)
        viewModelScope.launch(Dispatchers.IO) {
            mutableCityUpdateResponse.emit(Results.Loading)
            dcLocation.address = dcLocation.address_list
            val result = repository.cityUpdate(
                repository.getDataStoreContext().getString(PreferenceKey.ACCESS_TOKEN.name)!!,
                dcLocation
            )
            result.collect {
                mutableCityUpdateResponse.emit(it)
            }
        }
    }

    fun getTempId(): String {
        var value = ""
        viewModelScope.launch {
            value = repository.getDataStoreContext().getString(PreferenceKey.TEMP_EMP_CODE.name)
                .toString()
        }
        return "ID: $value"
    }

    fun setIsDcLocation() {
        viewModelScope.launch {
            repository.getDataStoreContext().putBoolean(
                PreferenceKey.IS_DC_LOCATION.name,
                true
            )
        }
    }

    fun setDcLatLong(dclat: Double, dcLong: Double, dcNumber: String) {
        viewModelScope.launch {
            repository.getDataStoreContext().putDouble(PreferenceKey.DC_LATITUDE.name, dclat)
            repository.getDataStoreContext().putDouble(PreferenceKey.DC_LONGITUDE.name, dcLong)
            repository.getDataStoreContext().putString(PreferenceKey.DC_NUMBER.name, dcNumber)
        }
    }
}