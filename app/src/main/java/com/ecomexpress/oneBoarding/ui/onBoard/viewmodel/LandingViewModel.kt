package com.ecomexpress.oneBoarding.ui.onBoard.viewmodel

import androidx.lifecycle.viewModelScope
import com.ecomexpress.oneBoarding.data.datasource.local.preference.PreferenceKey
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
class LandingViewModel @Inject constructor(private val repository: OneBoardingRepository) :
    BaseViewModel()  {
    private var mutableBannerResponse = MutableStateFlow<Results<Any>>(Results.Empty)
    val commonBannerResponse: StateFlow<Results<Any>> get() = mutableBannerResponse


    fun callToBannerImageService() {
        viewModelScope.launch(Dispatchers.IO) {
            mutableBannerResponse.emit(Results.Loading)
            val result = repository.getBanner()
            result.collect {
                mutableBannerResponse.emit(it)
            }
        }
    }

    fun setTermsRead(){
        viewModelScope.launch {
            repository.getDataStoreContext().putBoolean(PreferenceKey.TERMS_READ.name,true)
        }
    }

    fun getTermsRead():Boolean?{
        var res:Boolean? = null
        viewModelScope.launch {
            res = repository.getDataStoreContext().getBoolean(PreferenceKey.TERMS_READ.name)
        }
        return res
    }
}