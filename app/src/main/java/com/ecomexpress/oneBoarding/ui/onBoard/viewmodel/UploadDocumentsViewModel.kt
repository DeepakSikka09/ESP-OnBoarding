package com.ecomexpress.oneBoarding.ui.onBoard.viewmodel

import android.graphics.Bitmap
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewModelScope
import com.ecomexpress.oneBoarding.data.datasource.local.preference.PreferenceKey
import com.ecomexpress.oneBoarding.data.model.CommonRequest
import com.ecomexpress.oneBoarding.data.model.CommonResponse
import com.ecomexpress.oneBoarding.data.repository.OneBoardingRepository
import com.ecomexpress.oneBoarding.ui.base.BaseViewModel
import com.ecomexpress.oneBoarding.utils.comman.AppConstants.AADHAR
import com.ecomexpress.oneBoarding.utils.comman.AppConstants.BACKAADHAAR
import com.ecomexpress.oneBoarding.utils.comman.AppConstants.BANK
import com.ecomexpress.oneBoarding.utils.comman.AppConstants.DL
import com.ecomexpress.oneBoarding.utils.comman.AppConstants.FRONTAADHAAR
import com.ecomexpress.oneBoarding.utils.comman.AppConstants.PAN
import com.ecomexpress.oneBoarding.utils.comman.AppConstants.RazorpayUrl
import com.ecomexpress.oneBoarding.utils.comman.CommonUtils
import com.ecomexpress.oneBoarding.utils.comman.Results
import com.ecomexpress.oneBoarding.utils.enums.DocType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UploadDocumentsViewModel @Inject constructor(private val repository: OneBoardingRepository) :
    BaseViewModel() {

    private var mutableManualDocResponse = MutableStateFlow<Results<Any>>(Results.Empty)
    val commonManualDocResponse: StateFlow<Results<Any>> get() = mutableManualDocResponse
    private var mutableRefferalCodeResponse = MutableStateFlow<Results<Any>>(Results.Empty)
    val commonReferralCodeResponse: StateFlow<Results<Any>> get() = mutableRefferalCodeResponse
    private var mutableDocUploadFlow = MutableStateFlow<Results<CommonResponse>>(Results.Empty)
    val docUploadFlow = mutableDocUploadFlow.asStateFlow()

    private val _ifscResponse = MutableStateFlow<Results<Any>>(Results.Empty)
    val ifscResponse: StateFlow<Results<Any>> get() = _ifscResponse
    lateinit var result: Flow<Results<CommonResponse>>
    lateinit var uploadResult: Flow<Results<CommonResponse>>
    private var documentDetailsMutableFlow = MutableStateFlow<Results<Any>>(Results.Empty)
    val documentDetailsFlow: StateFlow<Results<Any>> get() = documentDetailsMutableFlow
    fun updateManualDocService(
        docType: String? = "",
        docNum: String? = "",
        pan: Boolean? = false,
        ifsc: String? = "",
        expiry: String? = "",
        userName: String? = "",
        dob: String? = "",
        gender: String? = "",
        currentAddress: String? = "",
        permanentAddress: String? = "",
        currentState: String? = "",
        currentPincode: String? = "",
        permanentPincode: String? = "",
        permanentState: String? = "",
        isAddress:Boolean?=false,
        bankName:String?=""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            mutableManualDocResponse.emit(Results.Loading)

            when (docType) {
                BANK -> {
                    result = repository.setManualDocUpdate(
                        repository.getDataStoreContext()
                            .getString(PreferenceKey.ACCESS_TOKEN.name)!!, CommonRequest(
                            ifsc_code = ifsc, doc_number = docNum, doc_type = BANK , bank_name = bankName
                        )
                    )
                }

                PAN -> {
                    result = repository.setManualDocUpdate(
                        repository.getDataStoreContext()
                            .getString(PreferenceKey.ACCESS_TOKEN.name)!!, CommonRequest(
                            is_pan = pan, doc_number = docNum, doc_type = PAN
                        )
                    )
                }

                DL -> {
                    result = repository.setManualDocUpdate(
                        repository.getDataStoreContext()
                            .getString(PreferenceKey.ACCESS_TOKEN.name)!!, CommonRequest(
                            is_dl = pan, doc_number = docNum, doc_type = DL, expiry_date = expiry
                        )
                    )
                }

                FRONTAADHAAR -> {
                    result = repository.setManualDocUpdate(
                        repository.getDataStoreContext()
                            .getString(PreferenceKey.ACCESS_TOKEN.name)!!, CommonRequest(
                            doc_type = AADHAR,
                            img_type = "1",
                            doc_number = docNum,
                            user_name = userName,
                            dob = dob,
                            gender = gender
                        )
                    )
                }

                BACKAADHAAR -> {
                    result = repository.setManualDocUpdate(
                        repository.getDataStoreContext()
                            .getString(PreferenceKey.ACCESS_TOKEN.name)!!, CommonRequest(
                            doc_type = AADHAR,
                            img_type = "2",
                            current_address = currentAddress,
                            permanent_address = permanentAddress,
                            current_state = currentState,
                            current_pincode = currentPincode,
                            permanent_pincode = permanentPincode,
                            permanent_state = permanentState,
                            is_address = isAddress
                        )
                    )
                }
            }

            result.collect {
                mutableManualDocResponse.emit(it)
            }
        }
    }

    fun uploadDoc(bitmap: Bitmap, docType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            mutableDocUploadFlow.emit(Results.Loading)
            val baseString = CommonUtils.getBase64String(bitmap)
            when (docType) {
                BACKAADHAAR -> {
                    uploadResult = repository.uploadImages(
                        repository.getDataStoreContext()
                            .getString(PreferenceKey.ACCESS_TOKEN.name)!!, CommonRequest(
                            img = baseString, doc_type = DocType.Aadhaar.name, img_type = "2"
                        )
                    )
                }

                FRONTAADHAAR -> {
                    uploadResult = repository.uploadImages(
                        repository.getDataStoreContext()
                            .getString(PreferenceKey.ACCESS_TOKEN.name)!!, CommonRequest(
                            img = baseString, doc_type = DocType.Aadhaar.name, img_type = "1"
                        )
                    )
                }

                BANK -> {
                    uploadResult = repository.uploadImages(
                        repository.getDataStoreContext()
                            .getString(PreferenceKey.ACCESS_TOKEN.name)!!, CommonRequest(
                            img = baseString, doc_type = DocType.Bank.name
                        )
                    )
                }
            }

            uploadResult.collect {
                mutableDocUploadFlow.emit(it)
            }
        }
    }

    fun refferalCode(request: CommonRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            mutableRefferalCodeResponse.emit(Results.Loading)
            val result = repository.referral(
                repository.getDataStoreContext().getString(PreferenceKey.ACCESS_TOKEN.name)!!,
                request
            )
            result.collect {
                mutableRefferalCodeResponse.emit(it)
            }
        }
    }

    fun checkIfscCode(ifscCode:String){
        viewModelScope.launch {
            _ifscResponse.emit(Results.Loading)
            val result = repository.checkIfscCode("$RazorpayUrl$ifscCode")
            result.collect{
                _ifscResponse.emit(it)
            }
        }
    }

    fun documentDetails() {
        viewModelScope.launch(Dispatchers.IO) {
            if (repository.getDataStoreContext().getInt(
                    PreferenceKey.ALL_DOCUMENTS_STATUS.name
                ) != 0 || repository.getDataStoreContext().getInt(
                    PreferenceKey.BANK_STATUS.name
                ) != 0 || repository.getDataStoreContext().getInt(
                    PreferenceKey.DRIVING_STATUS.name
                ) != 0 || repository.getDataStoreContext().getInt(
                    PreferenceKey.AADHAAR_STATUS.name
                ) != 0 || repository.getDataStoreContext().getInt(
                    PreferenceKey.PAN_STATUS.name
                ) != 0 || repository.getDataStoreContext()
                    .getInt(PreferenceKey.IS_PROFILE_PIC.name) != 0
            ) {
                documentDetailsMutableFlow.emit(Results.Loading)
                val result = repository.documentdetails(
                    repository.getDataStoreContext()
                        .getString(PreferenceKey.ACCESS_TOKEN.name)!!
                )
                result.collect {
                    documentDetailsMutableFlow.emit(it)
                }
            }
        }
    }

    fun getDocumentStatus(key: String): Int? {
        var value: Int? = 0
        viewModelScope.launch {
            value = repository.getDataStoreContext().getInt(
                key
            )
        }
        return value
    }

    fun setReapplybtn() {
        viewModelScope.launch {
            repository.getDataStoreContext().putBoolean(
                PreferenceKey.REAPPLYBTN.name,
                true
            )
        }
    }

    fun updateDocumentStatus(key: String, value: Int) {
        viewModelScope.launch {
            repository.getDataStoreContext().putInt(
                key, value
            )
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

    fun setProfilePic() {
        viewModelScope.launch {
            repository.getDataStoreContext().putInt(PreferenceKey.IS_PROFILE_PIC.name, 1)
        }
    }


    fun getBooleanStatus(key: Preferences.Key<Boolean>): Boolean {
        var value: Boolean = false
        viewModelScope.launch {
            value = repository.getDataStoreContext().getBoolean(key.name)?:false
        }
        return value
    }

    fun setBooleanStatus(key: Preferences.Key<Boolean>){
        viewModelScope.launch {
            repository.getDataStoreContext().putBoolean(key.name,true)
        }
    }
}