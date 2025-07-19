package com.ecomexpress.oneBoarding.ui.onBoard.activity

import android.annotation.SuppressLint
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.ecomexpress.oneBoarding.BuildConfig
import com.ecomexpress.oneBoarding.R
import com.ecomexpress.oneBoarding.data.model.CommonResponse
import com.ecomexpress.oneBoarding.data.model.onBoarding.OtpRequest
import com.ecomexpress.oneBoarding.databinding.ActivityOtpverifyBinding
import com.ecomexpress.oneBoarding.ui.base.BaseActivity
import com.ecomexpress.oneBoarding.ui.onBoard.viewmodel.OTPVerifyViewModel
import com.ecomexpress.oneBoarding.utils.broadcast.OTPPickerBroadcastReceiver
import com.ecomexpress.oneBoarding.utils.broadcast.OTPPickerBroadcastReceiver.OTPReceiveListener
import com.ecomexpress.oneBoarding.utils.comman.*
import com.ecomexpress.oneBoarding.utils.comman.AppConstants.DELAY_TIME
import com.ecomexpress.oneBoarding.utils.comman.AppConstants.MOBILE_NO
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.tasks.Task
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.DecimalFormat


@AndroidEntryPoint
class OTPVerifyActivity : BaseActivity<OTPVerifyViewModel, ActivityOtpverifyBinding>(), TextWatcher,
    View.OnFocusChangeListener, View.OnKeyListener, View.OnClickListener {

    private var isRegistered = false
    var number: String = ""
    var delay: Int = 0
    var countDownTimer: CountDownTimer? = null
    val otpPickerBroadcastReceiver = OTPPickerBroadcastReceiver()
    var isTimerRuning = false
    override fun getLayout(): Int = R.layout.activity_otpverify
    override fun getViewModelClass(): Class<OTPVerifyViewModel> = OTPVerifyViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.otpVerify = mViewModel
        mBinding.lifecycleOwner = this

        number = intent.getStringExtra(MOBILE_NO)!!
        delay = intent.getIntExtra(DELAY_TIME, 0)
        val start_num = number.substring(0, 3)
        val end_num = number.substring(7)
        val num = "$start_num****$end_num"
        mBinding.tvResend.setOnClickListener(this)
        mBinding.otpBtn.setOnClickListener(this)
        mBinding.icBackArrow.setOnClickListener(this)

        counter(delay, true)
        setPinListeners()
        collectData()


        (getString(R.string.resend_otp_statement) + "\n" + "+91 " + num + " " + getString(R.string.Did_receive)).also {
            mBinding.tvResendOtpStatement.text = it
        }

        mBinding.tvVersion.text = "Version ${BuildConfig.VERSION_NAME}"

    }

    override fun onResume() {
        super.onResume()
        otpCollectRelatedData()
    }

    private fun otpCollectRelatedData() {
//  check hashkey for system
//        val appSignatureHelper = AppSignatureHelper(this@OTPVerifyActivity)
//        Log.e("hashKey: ", " / " + appSignatureHelper.appSignatures)
//        mBinding.tvResendOtpStatement.text = appSignatureHelper.appSignatures.toString()
        startSMSRetrieverClient()
    }

    private fun counter(delay: Int, check: Boolean) {
        if (countDownTimer != null) {
            countDownTimer!!.cancel()
        }
        countDownTimer = object : CountDownTimer(delay.toLong(), 1000) {
            var minutes: Long = 0L
            var seconds: Long = 0L

            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                mBinding.tvResend.visibility = View.GONE
                mBinding.tvResendOtpViaSms.visibility = View.VISIBLE
                DecimalFormat("00")
                minutes = millisUntilFinished / 1000 / 60
                seconds = millisUntilFinished / 1000 % 60
                if (check) {
                    //to make particular string in color
                    val spannable = SpannableString("Resend OTP via SMS in $seconds seconds. ")
                    spannable.setSpan(
                        ForegroundColorSpan(
                            ContextCompat.getColor(
                                this@OTPVerifyActivity, R.color.red_66
                            )
                        ), 15, // start
                        33, // end
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                    )
                    mBinding.tvResendOtpViaSms.text = spannable
                } else {
                    isTimerRuning = true
                    if (seconds < 10) {
                        mBinding.otpBtn.text = "$minutes:0$seconds"
                    } else {
                        mBinding.otpBtn.text = "$minutes:$seconds"
                    }
                    viewChange()
                    mBinding.tvResend.visibility = View.GONE
                    mBinding.tvResendOtpViaSms.text = ""
                    mBinding.otpMessageAppCompatTextView.visibility = View.GONE
                    mBinding.otpBtn.setTextColor(
                        ContextCompat.getColor(this@OTPVerifyActivity, R.color.white)
                    )
                }
            }

            override fun onFinish() {
                if (check) {
                    mBinding.tvResend.visibility = View.VISIBLE
                    mBinding.tvResendOtpViaSms.visibility = View.GONE
                    mBinding.tvResendOtpViaSms.text = ""
                    mBinding.otpMessageAppCompatTextView.visibility = View.GONE
                } else {
                    isTimerRuning = false
                    if (mBinding.mHiddenEditText.text.toString().length == 4) {
                        mBinding.otpBtn.setText(
                            getText(R.string.verify_otp)
                        )
                        mBinding.tvResend.visibility = View.VISIBLE
                        mBinding.otpBtn.setBackgroundResource(R.drawable.button)
                        mBinding.otpBtn.isEnabled = true
                        mBinding.otpBtn.setTextColor(
                            ContextCompat.getColor(this@OTPVerifyActivity, R.color.white)
                        )
                    }
                }
            }
        }.start()
    }

    private fun collectData() {
        lifecycleScope.launch {
            launch {
                mViewModel!!.commonResponseVerifyOTP.collect { it ->
                    it.onLoading {
                        progressDialog(this@OTPVerifyActivity).show()
                    }.onError {
                        progressDialog(this@OTPVerifyActivity).dismiss()
                        snackBar(it.message, false)
                    }.onSuccess {
                        progressDialog(this@OTPVerifyActivity).dismiss()
                        val res = it as CommonResponse
                        if (res.success) {
                            snackBar(res.description, true)
                            mViewModel!!.setLoginStatus(res.data!!)
                            res.data.let { data ->
                                if (data.dc_status == 0 || data.dc_status == 3) {
                                    if (data.all_documents_status == 1) {
                                        //all documents are uploaded and dc is pending or on hold
                                        launchNewActivity<FinalStatusActivity> {}
                                    } else {
                                        //all documents are not uploaded and dc is pending or on hold
                                        launchNewActivity<OnBoardLocationActivity> { }
                                    }
                                } else if (data.dc_status == 1) {
                                    //dc_status is accepted or hiring is in hold
                                    launchNewActivity<FinalStatusActivity> {}
                                } else {
                                    if (data.reapply!!) {
                                        //dc has rejected and user have updated some rejected docs
                                        launchNewActivity<UploadDocumentsActivity> { }
                                    } else {
                                        //dc have hard rejected
                                        launchNewActivity<FinalStatusActivity>()
                                    }
                                }
                            }
                        } else {
                            snackBar(it.description, false)
                            it.data!!.time_difference?.let { time ->
                                if (time != 0) {
                                    counter(time, false)
                                }
                            }
                        }
                    }
                }
            }
            launch {
                mViewModel!!.responseOTP.collect {
                    it.onLoading {
                        progressDialog().show()
                    }
                    it.onSuccess {
                        progressDialog(this@OTPVerifyActivity).dismiss()
                        val res = it as CommonResponse
                        if (res.success) {
                            snackBar(res.description, true)
                        } else {
                            snackBar(res.description, false)
                        }
                    }
                    it.onError {
                        progressDialog(this@OTPVerifyActivity).dismiss()
                        snackBar(it.message, false)
                    }
                }
            }
        }
    }


    private fun otpVerifyService(mobileNumber: String, otp: String) {
        mViewModel?.verifyOTPService(OtpRequest(mobile = mobileNumber, otp = otp))
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        when (s?.length) {
            0 -> {
                mBinding.ed1.setText("")
            }

            1 -> {
                mBinding.ed1.nextFocusForwardId = mBinding.ed2.id
                mBinding.ed1.setText(s[0] + "")
                mBinding.ed2.setText("")
                mBinding.ed3.setText("")
                mBinding.ed4.setText("")
                if (!isTimerRuning) {
                    viewChange()
                }
            }

            2 -> {
                mBinding.ed2.setText(s[1] + "")
                mBinding.ed3.setText("")
                mBinding.ed4.setText("")
                if (!isTimerRuning) {
                    viewChange()
                }
            }

            3 -> {
                mBinding.ed3.setText(s[2] + "")
                mBinding.ed4.setText("")
                if (!isTimerRuning) {
                    viewChange()
                }
            }

            4 -> {
                mBinding.ed4.setText(s[3] + "")
                if (!isTimerRuning) {
                    mBinding.otpBtn.setBackgroundResource(R.drawable.button)
                    mBinding.otpBtn.isEnabled = true
                    mBinding.otpBtn.setTextColor(
                        ContextCompat.getColor(this, R.color.white)
                    )
                }
            }
        }
    }

    private fun viewChange() {
        mBinding.otpBtn.isEnabled = false
        mBinding.otpBtn.setBackgroundResource(R.drawable.disable_button)
        mBinding.otpMessageAppCompatTextView.visibility = View.GONE
        mBinding.otpBtn.setTextColor(
            ContextCompat.getColor(this, R.color.grey_A3)
        )
    }


    private fun setPinListeners() {
        mBinding.mHiddenEditText.addTextChangedListener(this)
        mBinding.ed1.onFocusChangeListener = this
        mBinding.ed2.onFocusChangeListener = this
        mBinding.ed3.onFocusChangeListener = this
        mBinding.ed4.onFocusChangeListener = this
        mBinding.ed1.setOnKeyListener(this)
        mBinding.ed2.setOnKeyListener(this)
        mBinding.ed3.setOnKeyListener(this)
        mBinding.ed4.setOnKeyListener(this)
        mBinding.mHiddenEditText.setOnKeyListener(this)
    }


    override fun afterTextChanged(s: Editable?) {}

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_DOWN) {
            when (v!!.id) {
                R.id.m_hidden_Edit_text -> {
                    if (keyCode == KeyEvent.KEYCODE_DEL) {
                        when (mBinding.mHiddenEditText.text?.length) {
                            4 -> mBinding.ed4.setText("")
                            3 -> mBinding.ed3.setText("")
                            2 -> mBinding.ed2.setText("")
                            1 -> mBinding.ed1.setText("")
                        }
                    }
                }
            }
        }
        return false
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        when (v!!.id) {
            R.id.ed_1, R.id.ed_2, R.id.ed_3, R.id.ed_4 -> {
                if (hasFocus) {
                    setFocus(mBinding.mHiddenEditText)
                    CommonUtils.showSoftKeyboard(this, mBinding.mHiddenEditText)
                }
            }
        }
    }

    private fun setFocus(editText: EditText?) {
        if (editText == null) return
        editText.isFocusable = true
        editText.isFocusableInTouchMode = true
        editText.requestFocus()
    }

    override fun onClick(p0: View?) {
        if (p0 != null) {
            CommonUtils.hideKeyBoard(this@OTPVerifyActivity, p0)
        }
        when (p0?.id) {
            mBinding.otpBtn.id -> {
                val customData = mapOf("Screen" to "OTPVerifyActivity", "key1" to 6)
                logButtonClick("search_button", customData)
                otpVerifyService(
                    number, mBinding.mHiddenEditText.text.toString()
                )
            }

            mBinding.icBackArrow.id -> {
                onBackPressed()
            }

            mBinding.tvResend.id -> {
                val customData = mapOf("Screen" to "OTPVerifyActivity", "key1" to 7)
                logButtonClick("tv_resend", customData)
                counter(delay, true)
                resendOtp(number)
            }
        }
    }

    private fun resendOtp(number: String) {
        mBinding.otpMessageAppCompatTextView.visibility = View.VISIBLE
        mBinding.otpMessageAppCompatTextView.text = resources.getString(R.string.auto_fetching_otp)
        mBinding.otpMessageAppCompatTextView.setTextColor(
            ContextCompat.getColor(
                this@OTPVerifyActivity, R.color.grey_7F
            )
        )
        val otpRequest = OtpRequest(
            mobile = number,
            device_id = CommonUtils.getDeviceID(this.contentResolver),
            device_version = CommonUtils.getDeviceVersion(),
            device_name = CommonUtils.getDeviceName(),
            device_model = CommonUtils.getDeviceModel()
        )
        mViewModel!!.generateOTPService(otpRequest)
    }

    override fun onBackPressed() {
        if (mBinding.tvResend.visibility == View.VISIBLE) {
            super.onBackPressed()
            overridePendingTransition(
                R.anim.slide_in_left, R.anim.slide_out_right
            )
        }
    }

    private fun startSMSRetrieverClient() {
        val client = SmsRetriever.getClient(this@OTPVerifyActivity)
        val task: Task<Void> = client.startSmsRetriever()
        task.addOnSuccessListener {
            isRegistered = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(
                    otpPickerBroadcastReceiver, IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION),
                    RECEIVER_EXPORTED
                )
            }else{
                registerReceiver(
                    otpPickerBroadcastReceiver, IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
                )
            }
            otpPickerBroadcastReceiver.init(object : OTPReceiveListener {
                override fun onOTPReceived(otp: String?) {
                    // OTP Received
                    mBinding.otpMessageAppCompatTextView.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_baseline_check_circle, 0, 0, 0
                    )
                    mBinding.otpMessageAppCompatTextView.setTextColor(
                        ContextCompat.getColor(
                            this@OTPVerifyActivity, R.color.green
                        )
                    )
                    mBinding.otpMessageAppCompatTextView.text =
                        resources.getString(R.string.otp_verified_successfully)
                    mBinding.otpMessageAppCompatTextView.visibility = View.VISIBLE
                    mBinding.mHiddenEditText.setText(otp)
                    otp.let {
                        if (otp?.length == 4) {
                            mBinding.ed1.setText(otp[0].toString())
                            mBinding.ed2.setText(otp[1].toString())
                            mBinding.ed3.setText(otp[2].toString())
                            mBinding.ed4.setText(otp[3].toString())
                        }
                    }
                    Handler(Looper.getMainLooper()).postDelayed({
                        otpVerifyService(
                            number, mBinding.mHiddenEditText.text.toString()
                        )
                    }, 2000)
                }

                override fun onOTPTimeOut() {
                    mBinding.otpMessageAppCompatTextView.visibility = View.GONE
                }
            })
        }
        task.addOnFailureListener {}
    }

    override fun onPause() {
        super.onPause()
        if (isRegistered) {
            unregisterReceiver(otpPickerBroadcastReceiver)
            isRegistered = false
        }
    }

}