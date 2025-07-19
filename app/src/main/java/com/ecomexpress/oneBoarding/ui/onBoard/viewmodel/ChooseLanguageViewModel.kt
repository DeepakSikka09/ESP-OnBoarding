package com.ecomexpress.oneBoarding.ui.onBoard.viewmodel

import androidx.lifecycle.viewModelScope
import com.ecomexpress.oneBoarding.data.datasource.local.preference.PreferenceKey
import com.ecomexpress.oneBoarding.data.repository.OneBoardingRepository
import com.ecomexpress.oneBoarding.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChooseLanguageViewModel @Inject constructor(private val repository: OneBoardingRepository) :
    BaseViewModel() {

    private val isUserLoggedFlow = MutableStateFlow(false)
    val isUserLoggedIn = isUserLoggedFlow.asStateFlow()
    private val mutableActivityLauncher = MutableStateFlow(0)
    var childJob: Job? = null
    var parentJob: Job? = null

    /* mutableActivityLauncher Status
    1 -> FinalStatus Activity
    2 -> OnBoardLocation Activity
    3 -> UploadDocument Activity
    */
    val activityLauncher: StateFlow<Int> get() = mutableActivityLauncher

    fun checkAccessState() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getDataStoreContext().getInt(PreferenceKey.ALL_DOCUMENTS_STATUS.name)?.let {allDoc->
                repository.getDataStoreContext().getInt(PreferenceKey.DC_STATUS.name)?.let{dcStatus->
                    repository.getDataStoreContext().getBoolean(PreferenceKey.REAPPLY.name)?.let {reapply->
                        if(dcStatus == 0 || dcStatus == 3){
                            if(allDoc == 1){
                                //all documents are uploaded and dc is pending or on hold
                                mutableActivityLauncher.emit(1)
                            }else{
                                //all documents are not uploaded and dc is pending or on hold
                                mutableActivityLauncher.emit(2)
                            }
                        }

                        else if(dcStatus == 1){
                            //dc_status is accepted or hiring is in hold
                            mutableActivityLauncher.emit(1)
                        }
                        else if (dcStatus == 2){
                            if (reapply){
                                repository.getDataStoreContext().getBoolean(
                                    PreferenceKey.REAPPLYBTN.name
                                )?.let {
                                    if(it){
                                        //dc has rejected and user have clicked reapply button
                                        mutableActivityLauncher.emit(3)
                                    }else{
                                        //dc has rejected and user have not clicked reapply button
                                        mutableActivityLauncher.emit(1)
                                    }
                                }
                            }else{
                                //dc have hard rejected
                                mutableActivityLauncher.emit(1)
                            }
                        }
                        else {
                            repository.getDataStoreContext().getBoolean(PreferenceKey.IS_USER_LOGIN.name)
                                .let {
                                    if (it!!) {
                                        isUserLoggedFlow.emit(true)
                                    } else {
                                        isUserLoggedFlow.emit(false)
                                    }
                                }
                        }
                    }
                }
            }
        }
    }


    init {
            println("ViewModelTag: ViewModel created")

            parentJob = viewModelScope.launch {
                println("ViewModelTag: Parent coroutine started")

                childJob = launch(Job()) {
                    println("ViewModelTag: Child coroutine started")

                    while (isActive) {
                        println("ViewModelTag: Child coroutine is working")
                        delay(1000)
                    }
                }

                while (isActive) {
                    println("ViewModelTag: Parent coroutine is working")
                    delay(1000)
                }
            }
        }

        override fun onCleared() {
            super.onCleared()
            println("ViewModelTag: Parent isActive = ${parentJob?.isActive}")
            println("ViewModelTag: Child isActive = ${childJob?.isActive}")
        }

}
