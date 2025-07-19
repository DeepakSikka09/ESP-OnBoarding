package com.ecomexpress.oneBoarding.ui.onBoard.viewmodel

import androidx.lifecycle.viewModelScope
import com.ecomexpress.oneBoarding.data.datasource.local.preference.PreferenceKey
import com.ecomexpress.oneBoarding.data.model.CommonRequest
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
class TermsAndConditionsViewModel @Inject constructor(private val repository: OneBoardingRepository) :
    BaseViewModel() {

    private var mutableTermsResponse = MutableStateFlow<Results<Any>>(Results.Empty)
    val commonTermsResponse: StateFlow<Results<Any>> get() = mutableTermsResponse
    fun callToTermsService() {
        viewModelScope.launch(Dispatchers.IO) {
            mutableTermsResponse.emit(Results.Loading)
            val result = repository.updateTermsConditions(
                repository.getDataStoreContext().getString(
                    PreferenceKey.ACCESS_TOKEN.name
                )!!, CommonRequest(
                    terms_check = 1
                )
            )
            result.collect {
                mutableTermsResponse.emit(it)
            }
        }
    }

}