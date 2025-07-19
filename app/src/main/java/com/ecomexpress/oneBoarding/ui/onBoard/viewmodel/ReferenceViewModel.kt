package com.ecomexpress.oneBoarding.ui.onBoard.viewmodel

import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewModelScope
import com.ecomexpress.oneBoarding.data.datasource.local.preference.PreferenceKey
import com.ecomexpress.oneBoarding.data.model.CommonRequest
import com.ecomexpress.oneBoarding.data.model.CommonResponse
import com.ecomexpress.oneBoarding.data.model.onBoarding.Data
import com.ecomexpress.oneBoarding.data.model.onBoarding.ReferenceModel
import com.ecomexpress.oneBoarding.data.repository.OneBoardingRepository
import com.ecomexpress.oneBoarding.ui.base.BaseViewModel
import com.ecomexpress.oneBoarding.utils.comman.Results
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ReferenceViewModel @Inject constructor(private val repository: OneBoardingRepository) :
    BaseViewModel() {
    private val mutableReferenceApiResponse =
        MutableStateFlow<Results<CommonResponse>>(Results.Empty)
    val referenceApiResponse: StateFlow<Results<CommonResponse>> get() = mutableReferenceApiResponse

    private val mutableGetReferenceList = MutableStateFlow<Results<CommonResponse>>(Results.Empty)
    val getReferenceList: StateFlow<Results<CommonResponse>> get() = mutableGetReferenceList

    fun submitReference(list: List<ReferenceModel>) {
        viewModelScope.launch {
            mutableReferenceApiResponse.emit(Results.Loading)
            val result = repository.sendReferenceList(
                repository.getDataStoreContext().getString(PreferenceKey.ACCESS_TOKEN.name)!!,
                CommonRequest(
                    referral = list
                )
            )
            result.collect {
                mutableReferenceApiResponse.emit(it)
            }
        }
    }

    fun setReferenceStatus(key: Preferences.Key<Boolean>) {
        viewModelScope.launch {
            repository.getDataStoreContext().putBoolean(key.name, true)
        }
    }

    fun callReferenceList() {
        viewModelScope.launch(Dispatchers.IO) {
            mutableGetReferenceList.emit(Results.Loading)
            val result = repository.getReferenceList(
                repository.getDataStoreContext().getString(PreferenceKey.ACCESS_TOKEN.name)!!,
            )
            result.collect {
                mutableGetReferenceList.emit(it)
            }
        }
    }

    fun getMobileNumber():String{
        var mobile = ""
        viewModelScope.launch {

                mobile =
                    withContext(Dispatchers.Main) {
                        repository.getDataStoreContext().getString(PreferenceKey.MOBILE_NUMBER.name)
                    }?:""
        }
        return mobile
    }
}