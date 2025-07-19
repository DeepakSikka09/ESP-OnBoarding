package com.ecomexpress.oneBoarding.ui.onBoard.activity

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecomexpress.oneBoarding.R
import com.ecomexpress.oneBoarding.data.datasource.local.preference.PreferenceKey
import com.ecomexpress.oneBoarding.data.model.onBoarding.ReferenceModel
import com.ecomexpress.oneBoarding.databinding.ActivityReferenceBinding
import com.ecomexpress.oneBoarding.ui.adapter.ReferenceAdapter
import com.ecomexpress.oneBoarding.ui.base.BaseActivity
import com.ecomexpress.oneBoarding.ui.onBoard.viewmodel.ReferenceViewModel
import com.ecomexpress.oneBoarding.utils.comman.CommonUtils
import com.ecomexpress.oneBoarding.utils.comman.CommonUtils.checkMobileNumberValidation
import com.ecomexpress.oneBoarding.utils.comman.onError
import com.ecomexpress.oneBoarding.utils.comman.onLoading
import com.ecomexpress.oneBoarding.utils.comman.onSuccess
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReferenceActivity : BaseActivity<ReferenceViewModel, ActivityReferenceBinding>(),
    View.OnClickListener {
    private lateinit var referenceAdapter: ReferenceAdapter
    var lastSaveIndex = -1
    override fun getLayout(): Int = R.layout.activity_reference
    private var referenceList = arrayListOf(
        ReferenceModel("", ""),
        ReferenceModel("", "")
    )

    private fun toggleSaveBtn(isBtnEnable: Boolean ){
        if (isBtnEnable) {
            mBinding.saveBtn.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.blue)
            mBinding.saveBtn.setTextColor(
                ContextCompat.getColor(
                    applicationContext, R.color.white
                )
            )
            mBinding.saveBtn.isEnabled = true
        } else {
            mBinding.saveBtn.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.grey)
            mBinding.saveBtn.setTextColor(
                ContextCompat.getColor(
                    applicationContext, R.color.grey_A3
                )
            )
            mBinding.saveBtn.isEnabled = false
        }
    }
    private val setReferenceList = { referencesList: ArrayList<ReferenceModel> ->
        if(lastSaveIndex == -1){
            lastSaveIndex = 0
        }
        referenceList = referencesList
        if(referenceList.subList(lastSaveIndex, referenceList.size).none { it.mobile_number.length == 10 && it.name.isNotBlank() }){
            toggleSaveBtn(false)
        }else{
            toggleSaveBtn(true)
        }
    }
    private val keyBoardHide = {
        CommonUtils.hideKeyboard(this)
    }

    override fun getViewModelClass(): Class<ReferenceViewModel> = ReferenceViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel?.callReferenceList()
        mBinding.referenceRecycler.layoutManager = LinearLayoutManager(this)
        referenceAdapter = ReferenceAdapter(
            referenceList,
            setReferenceList,
            -1,
            keyBoardHide
        )
        mBinding.referenceRecycler.adapter = referenceAdapter
        mBinding.addItem.setOnClickListener(this)
        mBinding.saveBtn.setOnClickListener(this)
        mBinding.backBtn.setOnClickListener(this)
        lifecycleScope.launch(Dispatchers.Main) {
            mViewModel?.referenceApiResponse?.collect { result ->
                result.onLoading {
                    progressDialog(this@ReferenceActivity).show()
                }
                result.onSuccess { response ->
                    progressDialog(this@ReferenceActivity).dismiss()
                    if (response.success) {
                        showToast(response.description, true)
                        mViewModel?.setReferenceStatus(PreferenceKey.IS_REFERENCE)
                        this@ReferenceActivity.finish()
                    } else {
                        showToast(response.description, false)
                        response.data?.invalid_users?.let { invalidUsers ->
                            for (i in 0 until referenceList.size) {
                                if (invalidUsers.contains(referenceList[i].mobile_number)) {
                                    referenceList[i].isMobileError = true
                                    referenceList[i].mobileError = response.description
                                    referenceAdapter.notifyItemChanged(i)
                                }
                            }
                        }
                    }
                }
                result.onError {
                    showToast(it.message ?: "", false)
                    progressDialog(this@ReferenceActivity).dismiss()
                }
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            mViewModel?.getReferenceList?.collect { result ->
                result.onLoading {
                    progressDialog(this@ReferenceActivity).show()
                }
                result.onSuccess { response ->
                    progressDialog(this@ReferenceActivity).dismiss()
                    if (response.success) {
                        response.data?.let { data ->
                            if (data.referral.isNotEmpty()) {
                                referenceList.removeAll(referenceList.toSet())
                                referenceList.addAll(data.referral)
                                lastSaveIndex = referenceList.size
                                referenceAdapter = ReferenceAdapter(
                                    referenceList,
                                    setReferenceList,
                                    lastSaveIndex,
                                    keyBoardHide
                                )
                                mBinding.referenceRecycler.adapter = referenceAdapter
                            }
                        }
                    }
                }
                result.onError {
                    showToast(it.message ?: "", false)
                    progressDialog(this@ReferenceActivity).dismiss()
                }
            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0) {
            mBinding.addItem -> {
                if (referenceList.size < 10) {
                    referenceList.add(ReferenceModel("", ""))
                    referenceAdapter.notifyItemInserted(referenceList.size)
                    referenceAdapter.notifyItemRangeChanged(
                        referenceList.size - 2,
                        referenceList.size
                    )
                }
                if (referenceList.size == 10) {
                    mBinding.addItem.visibility = View.GONE
                }
                mBinding.referenceRecycler.scrollToPosition(referenceList.size - 1)
            }

            mBinding.saveBtn -> {
                var isError = false
                val numbers = referenceList.map { it.mobile_number }
                for (i in 0 until referenceList.size) {
                    if (
                        referenceList[i].name.isBlank() && referenceList[i].mobile_number.isEmpty()
                    ) {
                        continue
                    }else{
                        if (referenceList[i].name.isBlank()) {
                            isError = true
                            referenceList[i].isNameError = true
                            referenceList[i].nameError = getString(R.string.please_enter_name)
                        } else {
                            referenceList[i].isNameError = false
                            referenceList[i].nameError = ""
                        }
                        if (numbers.count { it == referenceList[i].mobile_number } > 1 && referenceList[i].mobile_number.length == 10) {
                            isError = true
                            referenceList[i].isMobileError = true
                            referenceList[i].mobileError =
                                getString(R.string.mobile_number_can_t_be_same)
                        } else if (mViewModel?.getMobileNumber().equals(referenceList[i].mobile_number)){
                            isError = true
                            referenceList[i].isMobileError = true
                            referenceList[i].mobileError =
                                getString(R.string.current_user_mobile_number)
                        } else if (!checkMobileNumberValidation(referenceList[i].mobile_number)) {
                            isError = true
                            referenceList[i].isMobileError = true
                            referenceList[i].mobileError =
                                getString(R.string.please_enter_mobile_number)
                        } else {
                            referenceList[i].isMobileError = false
                            referenceList[i].mobileError = ""
                        }
                        referenceAdapter.notifyItemChanged(i)
                    }
                }
                if (!isError) {
                    if(lastSaveIndex == -1){
                        lastSaveIndex = 0
                    }
                    mViewModel?.submitReference(
                        referenceList
                            .subList(lastSaveIndex, referenceList.size)
                        .filter { it.mobile_number.isNotEmpty() && it.name.isNotEmpty() })
                }
            }

            mBinding.backBtn -> {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }
}