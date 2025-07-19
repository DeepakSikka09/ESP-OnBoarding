package com.ecomexpress.oneBoarding.ui.onBoard.activity

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.ecomexpress.oneBoarding.BuildConfig
import com.ecomexpress.oneBoarding.R
import com.ecomexpress.oneBoarding.data.model.CommonResponse
import com.ecomexpress.oneBoarding.databinding.ActivityTermsAndConditionsBinding
import com.ecomexpress.oneBoarding.databinding.DialougeNotLinkingBinding
import com.ecomexpress.oneBoarding.databinding.TncPanAadhaarDialogBinding
import com.ecomexpress.oneBoarding.ui.base.BaseActivity
import com.ecomexpress.oneBoarding.ui.onBoard.viewmodel.TermsAndConditionsViewModel
import com.ecomexpress.oneBoarding.utils.comman.AppConstants
import com.ecomexpress.oneBoarding.utils.comman.onError
import com.ecomexpress.oneBoarding.utils.comman.onLoading
import com.ecomexpress.oneBoarding.utils.comman.onSuccess
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TermsAndConditionsActivity :
    BaseActivity<TermsAndConditionsViewModel, ActivityTermsAndConditionsBinding>(),
    OnClickListener {
    override fun getLayout(): Int = R.layout.activity_terms_and_conditions
    override fun getViewModelClass(): Class<TermsAndConditionsViewModel> =
        TermsAndConditionsViewModel::class.java
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding.lifecycleOwner = this
        setTermsConditions()

        changesOnUI(intent.getIntExtra(AppConstants.TERMS_CONDITIONS_STATUS, 0))
        collectData()

        mBinding.checkBox.setOnClickListener(this)
        mBinding.submitAppCompatButton.setOnClickListener(this)
    }

    private fun setTermsConditions() {
        mBinding.termsConditionsWebView.settings.javaScriptEnabled = true
        mBinding.termsConditionsWebView.loadUrl(BuildConfig.BASE_URL + getString(R.string.termsurl))
        mBinding.termsConditionsWebView.webViewClient = WebViewClient()
    }

    private fun collectData() {
        lifecycleScope.launch(Dispatchers.Main) {
            mViewModel!!.commonTermsResponse.collect { it ->
                it.onLoading { progressDialog().show() }.onSuccess {
                    progressDialog().dismiss()
                    val result = it as CommonResponse
                    snackBar(result.description, true)
                    changesOnUI(1)
                    if(result.data?.docLink == false){
                        dialogueForNotLinking()
                    }
                }.onError {
                    progressDialog(this@TermsAndConditionsActivity).dismiss()
                    snackBar(it.message.toString(), false)
                }
            }
        }
    }

    private fun changesOnUI(status: Int) {
        if (status == 0) {
            mBinding.checkBox.isEnabled = true
            mBinding.submitAppCompatButton.visibility = View.VISIBLE
        } else {
            mBinding.checkBox.isChecked = true
            mBinding.checkBox.isEnabled = false
            mBinding.submitAppCompatButton.visibility = View.GONE
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {

            mBinding.checkBox.id -> {
                if (mBinding.checkBox.isChecked) {
                    mBinding.submitAppCompatButton.isEnabled = true
                    mBinding.submitAppCompatButton.setBackgroundResource((R.drawable.button))
                    mBinding.submitAppCompatButton.setTextColor(
                        ContextCompat.getColor(
                            this@TermsAndConditionsActivity, R.color.white
                        )
                    )
                } else {
                    mBinding.submitAppCompatButton.isEnabled = false
                    mBinding.submitAppCompatButton.setBackgroundResource((R.drawable.disable_button))
                    mBinding.submitAppCompatButton.setTextColor(
                        ContextCompat.getColor(
                            this@TermsAndConditionsActivity, R.color.grey_A3
                        )
                    )
                }
            }

            mBinding.submitAppCompatButton.id -> {
                mViewModel?.callToTermsService()
            }
        }
    }
    private fun dialogueForNotLinking() {
        val alertDialog = Dialog(this)
        val dialogNotLinkingBinding = TncPanAadhaarDialogBinding.inflate(layoutInflater)
        alertDialog.setContentView(dialogNotLinkingBinding.root)
        alertDialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        alertDialog.window!!.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    this, R.color.transparent
                )
            )
        )
        alertDialog.setCancelable(false)
        alertDialog.show()
        dialogNotLinkingBinding.ivCancel.setOnClickListener {
            alertDialog.dismiss()
        }
        dialogNotLinkingBinding.btnConfirm.setOnClickListener {
            val customData = mapOf("NotLinkedAddharPan" to "UploadDocumentActivity", "key1" to 28)
            logButtonClick("btn_confirm", customData)
            alertDialog.dismiss()
        }
    }
}