package com.ecomexpress.oneBoarding.ui.onBoard.activity

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.constraintlayout.helper.widget.MotionEffect.TAG
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.ecomexpress.oneBoarding.BuildConfig
import com.ecomexpress.oneBoarding.R
import com.ecomexpress.oneBoarding.data.model.CommonResponse
import com.ecomexpress.oneBoarding.data.model.onBoarding.OtpRequest
import com.ecomexpress.oneBoarding.databinding.ActivitySignUpBinding
import com.ecomexpress.oneBoarding.ui.base.BaseActivity
import com.ecomexpress.oneBoarding.ui.onBoard.viewmodel.SignUpViewModel
import com.ecomexpress.oneBoarding.utils.comman.*
import com.ecomexpress.oneBoarding.utils.comman.AppConstants.DELAY_TIME
import com.ecomexpress.oneBoarding.utils.comman.AppConstants.MOBILE_NO
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@AndroidEntryPoint
class SignUpActivity : BaseActivity<SignUpViewModel, ActivitySignUpBinding>(),
    View.OnClickListener {
    override fun getLayout(): Int = R.layout.activity_sign_up
    override fun getViewModelClass(): Class<SignUpViewModel> = SignUpViewModel::class.java
     var fcmToken : String = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.signup = mViewModel
        mBinding.lifecycleOwner = this
        getFcmToken()
        buttonStatus()
        collectData()
        mBinding.otpBtn.setOnClickListener(this)
        mBinding.icBackArrow.setOnClickListener(this)
        mBinding.tvVersion.text = "Version ${BuildConfig.VERSION_NAME}"
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            mBinding.otpBtn.id -> {
                val customData = mapOf("Screen" to "SignUpActivity", "key1" to 4)
                logButtonClick("otp_btn", customData)
                getOTP()
            }

            mBinding.icBackArrow.id -> {
                val customData = mapOf("Screen" to "SignUpActivity", "key1" to 5)
                logButtonClick("ic_back_arrow", customData)
                finish()
            }
        }
    }

    private fun buttonStatus() {
        mBinding.enterMoblieNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(arg0: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                enableSubmitIfReady(s)
            }
        })
    }

    private fun getOTP() {
        if (mBinding.enterMoblieNumber.text?.length == 10) {
            if (!CommonUtils.checkMobileNumberValidation(mBinding.enterMoblieNumber.text.toString()))
                snackBar(
                    getString(R.string.enter_valid_number), false
                )
            else {
                mBinding.otpBtn.isEnabled = false
                mViewModel?.setMobileNumber(mBinding.enterMoblieNumber.text.toString())
                val otpRequest = OtpRequest(
                    mobile = mBinding.enterMoblieNumber.text.toString(),
                    device_id = CommonUtils.getDeviceID(contentResolver),
                    device_version = CommonUtils.getDeviceVersion(),
                    device_name = CommonUtils.getDeviceName(),
                    device_model = CommonUtils.getDeviceModel(),
                    fcm_token = fcmToken
                )
                mViewModel!!.generateOTPService(otpRequest)
            }
        } else {
            snackBar(getString(R.string.enter_mobile_number), false)
        }
    }

    fun enableSubmitIfReady(mobileNumber: CharSequence) {
        if (mobileNumber.length == 10 && mobileNumber.isNotEmpty()) {
            CommonUtils.hideKeyboard(this)
            mBinding.otpBtn.isEnabled = true
            mBinding.otpBtn.setBackgroundResource((R.drawable.button))
            mBinding.otpBtn.setTextColor(
                ContextCompat.getColor(
                    applicationContext, R.color.white
                )
            )
        } else {
            mBinding.otpBtn.isEnabled = false
            mBinding.otpBtn.setBackgroundResource((R.drawable.disable_button))
            mBinding.otpBtn.setTextColor(
                ContextCompat.getColor(
                    applicationContext, R.color.grey_A3
                )
            )
        }
    }

    private fun collectData() {
        lifecycleScope.launch(Dispatchers.Main) {
            mViewModel!!.commonResponseOTP.collect { it ->
                it.onLoading { progressDialog(this@SignUpActivity).show() }.onSuccess {
                    progressDialog(this@SignUpActivity).dismiss()
                    mBinding.otpBtn.isEnabled = true
                    val result = it as CommonResponse
                    if (result.success) {
                        launchNewActivity<OTPVerifyActivity> {
                            putExtra(MOBILE_NO, mBinding.enterMoblieNumber.text.toString())
                            putExtra(DELAY_TIME, result.data!!.delay_time)
                        }
                    } else {
                        snackBar(result.description, false)
                    }
                }.onError {
                    mBinding.otpBtn.isEnabled = true
                    progressDialog(this@SignUpActivity).dismiss()
                    snackBar(it.message, false)
                }
            }
        }
    }

    private fun getFcmToken()  {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                else{
                    fcmToken = task.result
                    mViewModel?.setFcmToken(fcmToken)
                }
            })
    }

}

