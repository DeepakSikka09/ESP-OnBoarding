package com.ecomexpress.oneBoarding.ui.onBoard.viewmodel

import androidx.lifecycle.viewModelScope
import com.ecomexpress.oneBoarding.data.datasource.local.preference.PreferenceKey
import com.ecomexpress.oneBoarding.data.model.CommonRequest
import com.ecomexpress.oneBoarding.data.model.CommonResponse
import com.ecomexpress.oneBoarding.data.repository.OneBoardingRepository
import com.ecomexpress.oneBoarding.ui.base.BaseViewModel
import com.ecomexpress.oneBoarding.utils.comman.Results
import com.ecomexpress.oneBoarding.utils.enums.DocType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelfieViewModel @Inject constructor(private val repository: OneBoardingRepository) : BaseViewModel() {

    private val mutableUploadProfileImgFlow = MutableStateFlow<Results<CommonResponse>>(Results.Empty)
    val uploadProfileImgFlow: StateFlow<Results<CommonResponse>> get() = mutableUploadProfileImgFlow

    fun uploadImage(baseString: String){
        viewModelScope.launch(Dispatchers.IO) {
            mutableUploadProfileImgFlow.emit(Results.Loading)
            val result = repository.uploadImages(
                repository.getDataStoreContext().getString(PreferenceKey.ACCESS_TOKEN.name)!!
                ,
                CommonRequest(
                    img = baseString,
                    doc_type = DocType.Profile.name
                )
            )
            result.collect {
                mutableUploadProfileImgFlow.emit(it)
            }
        }
    }
}