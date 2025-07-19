package com.ecomexpress.oneBoarding.ui.onBoard.activity

import android.Manifest
import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.helper.widget.MotionEffect
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.ecomexpress.oneBoarding.R
import com.ecomexpress.oneBoarding.data.datasource.local.preference.PreferenceKey
import com.ecomexpress.oneBoarding.data.model.CommonResponse
import com.ecomexpress.oneBoarding.databinding.ActivityFinalStatusBinding
import com.ecomexpress.oneBoarding.ui.base.BaseActivity
import com.ecomexpress.oneBoarding.ui.onBoard.viewmodel.FinalStatusViewModel
import com.ecomexpress.oneBoarding.utils.comman.AppConstants
import com.ecomexpress.oneBoarding.utils.comman.CommonUtils
import com.ecomexpress.oneBoarding.utils.comman.CommonUtils.downloadAPK
import com.ecomexpress.oneBoarding.utils.comman.CommonUtils.navigateToMap
import com.ecomexpress.oneBoarding.utils.comman.launchNewActivity
import com.ecomexpress.oneBoarding.utils.comman.onError
import com.ecomexpress.oneBoarding.utils.comman.onLoading
import com.ecomexpress.oneBoarding.utils.comman.onSuccess
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class FinalStatusActivity : BaseActivity<FinalStatusViewModel, ActivityFinalStatusBinding>(),
    View.OnClickListener {

    override fun getLayout(): Int = R.layout.activity_final_status
    override fun getViewModelClass(): Class<FinalStatusViewModel> = FinalStatusViewModel::class.java
    private var dcAddress = ""
    var sathi_url: String? = null
    var sruti_url: String? = null
    var employee_terms_conditions_status: Int? = 0
    private val PERMISSION_REQUEST_CODE = 123
    lateinit var downloadManager: DownloadManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        mBinding.lifecycleOwner = this

        mBinding.addressSubLayout.directionLogo.setOnClickListener(this)
        mBinding.dcOnhold.directionLogo.setOnClickListener(this)
        mBinding.helpImage.setOnClickListener(this)
        mBinding.finalRejected.reApplyBtn.setOnClickListener(this)
        mBinding.finalVerification.btnSathi.setOnClickListener(this)
        mBinding.finalVerification.btnSruti.setOnClickListener(this)
        mBinding.termsCondition.acceptTermsTextView.setOnClickListener(this)
        mViewModel!!.setReferral()
        collectData()
        mBinding.feId.text = mViewModel?.getTempId()
        mViewModel!!.getStringValue(PreferenceKey.FE_NAME.name)?.let { setUserNameUI(it) }
        analyticsToAllScreen("Final_Status_Activity", this@FinalStatusActivity.localClassName)
    }

    override fun onResume() {
        super.onResume()
        initializeData()
    }

    private fun initializeData() {
        mViewModel!!.getbooleanValue(PreferenceKey.REAPPLYBTN.name)?.let {
            if (it) {
                mViewModel!!.getFinalStatus()
            } else {
                val reasons = mViewModel!!.getStringValue(PreferenceKey.REJECTION_REASON.name)
                mBinding.commonBg.imageView4.setImageResource(R.drawable.cancel_status)
                mBinding.commonBg.dcStatus.setTextColor(
                    ContextCompat.getColor(
                        this@FinalStatusActivity, R.color.red_4B
                    )
                )
                mBinding.commonBg.dcStatus.text = getString(R.string.onboarding_center_rejected)
                mBinding.commonBg.dcReason.setTextColor(
                    ContextCompat.getColor(
                        this@FinalStatusActivity, R.color.red_4B
                    )
                )
                mBinding.commonBg.dcReason.visibility = View.VISIBLE
                mBinding.commonBg.dcReason.text = reasons
                mBinding.addressSubLayout.addressSubLayout.visibility = View.GONE
                mBinding.finalRejected.finalRejected.visibility = View.VISIBLE
                mViewModel!!.getbooleanValue(PreferenceKey.REAPPLY.name)?.let {
                    if (it) {
                        mViewModel!!.setBtnClicked(false)
                    }
                }
            }
        } ?: kotlin.run {
            mViewModel!!.getFinalStatus()
        }
    }

    private fun setUserNameUI(name: String) {
        mBinding.feName.text = "Hi ${name},"
    }

    private fun collectData() {
        lifecycleScope.launch {
            mViewModel!!.finalStatusFlow.collect {
                it.onLoading {
                    progressDialog().show()
                }
                it.onError { res ->
                    progressDialog().dismiss()
                    showToast(res.message.toString(), false)
                }
                it.onSuccess { res ->
                    progressDialog().dismiss()
                    val result = res as CommonResponse
                    if (result.success) {
                        result.data!!.username?.let { it1 ->
                            setUserNameUI(it1)
                            mViewModel!!.updateFeName(it1)
                        }
                        mViewModel!!.updateDocumentStatus(result.data)
                        val data = res.data!!
                        dcAddress = data.dc_location_details
                        mBinding.feId.text = data.temp_empl_code
                        mBinding.addressSubLayout.officeAdress.text = dcAddress
                        sathi_url = data.app_url_sathi
                        sruti_url = data.app_url_sruti
                        dcLat = data.dc_lat!!
                        dcLong = data.dc_long!!
                        employee_terms_conditions_status = data.employee_terms_conditions_status
                        mViewModel!!.setDcLatLong(data.dc_long!!, data.dc_lat!!)
                        showToast(result.description, true)
                        data.reapply?.let {
                            if (it) {
                                mBinding.finalRejected.reApplyBtn.visibility = View.VISIBLE
                            }else{
                                mBinding.finalRejected.reApplyBtn.visibility = View.GONE
                            }
                        }
                        if (result.data.dc_status == 1) {
                            mBinding.commonBg.dcReason.visibility = View.GONE
                            mBinding.commonBg.dcStatus.setTextColor(
                                ContextCompat.getColor(
                                    this@FinalStatusActivity, R.color.green_92
                                )
                            )
                            mBinding.commonBg.dcStatus.text =
                                resources.getString(R.string.onboarding_center_approved)
                            mBinding.commonBg.imageView4.setImageResource(R.drawable.success_status)
                            if (result.data.hr_status == 0) {
                                mBinding.commonBg.imageView5.setImageResource(R.drawable.pending_icon)
                                mBinding.commonBg.hrStatus.setTextColor(
                                    ContextCompat.getColor(
                                        this@FinalStatusActivity, R.color.blue
                                    )
                                )
                                mBinding.commonBg.hrStatusReason.text =
                                    getString(R.string.hr_approval_time)
                                mBinding.commonBg.hrStatusReason.setTextColor(
                                    ContextCompat.getColor(
                                        this@FinalStatusActivity, R.color.light_blue
                                    )
                                )
                                mBinding.commonBg.hrStatusReason.visibility = View.VISIBLE
                                mBinding.addressSubLayout.addressSubLayout.visibility = View.GONE
                                mBinding.hrPending.constrntTick.visibility = View.VISIBLE
                            }
                            if (result.data.hr_status == 1) {
                                mBinding.commonBg.imageView5.setImageResource(R.drawable.success_status)
                                mBinding.commonBg.hrStatus.setTextColor(
                                    ContextCompat.getColor(
                                        this@FinalStatusActivity, R.color.green
                                    )
                                )
                                mBinding.commonBg.hrStatus.text =
                                    resources.getString(R.string.hr_approved)
                                mBinding.commonBg.hrStatusReason.visibility = View.GONE
                                mBinding.addressSubLayout.addressSubLayout.visibility = View.GONE
                                mBinding.hrPending.constrntTick.visibility = View.GONE
                                mBinding.termsCondition.applicationUnderHr.text =
                                    result.data.employment_type
                                mBinding.commonBg.imageView6.setImageResource(R.drawable.pending_icon)
                                mBinding.termsCondition.termsCondition.visibility = View.VISIBLE

                                if (result.data.employee_terms_conditions_status == 1) {
                                    mBinding.finalVerification.finalVerification.visibility =
                                        View.VISIBLE
                                    mBinding.termsCondition.termsCondition.visibility = View.GONE
                                    mBinding.commonBg.imageView6.setImageResource(R.drawable.success_status)
                                    mBinding.commonBg.termsStatus.setTextColor(
                                        ContextCompat.getColor(
                                            this@FinalStatusActivity, R.color.green
                                        )
                                    )
                                }
                            }

                            if (result.data.hr_status == 2) {
                                var reasons = ""
                                result.data.reasons!!.forEach { reason ->
                                    if (reasons.isNullOrEmpty()) {
                                        reasons = reason
                                    } else {
                                        reasons = "$reason \n$reasons"
                                    }
                                }
                                mBinding.commonBg.imageView5.setImageResource(R.drawable.cancel_status)
                                mBinding.commonBg.hrStatus.setTextColor(
                                    ContextCompat.getColor(
                                        this@FinalStatusActivity, R.color.red_4B
                                    )
                                )
                                mBinding.commonBg.hrStatus.text = getString(R.string.hr_rejected)
                                mBinding.commonBg.hrStatusReason.visibility = View.VISIBLE
                                mBinding.commonBg.hrStatusReason.text = reasons
                                mBinding.addressSubLayout.addressSubLayout.visibility = View.GONE
                                mBinding.finalRejected.finalRejected.visibility = View.VISIBLE
                            }
                        }
                        if (result.data.dc_status == 2) {
                            var reasons = ""
                            result.data.reasons!!.forEach { reason ->
                                if (reasons.isNullOrEmpty()) {
                                    reasons = reason
                                } else {
                                    reasons = "$reason \n$reasons"
                                }
                            }
                            mViewModel!!.setRejectionReason(reasons)
                            mBinding.commonBg.imageView4.setImageResource(R.drawable.cancel_status)
                            mBinding.commonBg.dcStatus.setTextColor(
                                ContextCompat.getColor(
                                    this@FinalStatusActivity, R.color.red_4B
                                )
                            )
                            mBinding.commonBg.dcStatus.text =
                                getString(R.string.onboarding_center_rejected)
                            mBinding.commonBg.dcReason.setTextColor(
                                ContextCompat.getColor(
                                    this@FinalStatusActivity, R.color.red_4B
                                )
                            )
                            mBinding.commonBg.dcReason.visibility = View.VISIBLE
                            mBinding.commonBg.dcReason.text = reasons
                            mBinding.addressSubLayout.addressSubLayout.visibility = View.GONE
                            mBinding.finalRejected.finalRejected.visibility = View.VISIBLE
                            if (result.data.reapply!!) {
                                mViewModel!!.setBtnClicked(false)
                            }
                        }
                        if (result.data.dc_status == 3) {
                            mBinding.commonBg.imageView4.setImageResource(R.drawable.verification)
                            mBinding.commonBg.dcStatus.setTextColor(
                                ContextCompat.getColor(
                                    this@FinalStatusActivity, R.color.dark_orange
                                )
                            )
                            mBinding.commonBg.dcStatus.text = getString(R.string.hiring_on_hold)
                            mBinding.commonBg.dcReason.visibility = View.GONE
                            mBinding.dcOnhold.addressSubLayout.visibility = View.VISIBLE
                            mBinding.addressSubLayout.addressSubLayout.visibility = View.GONE
                            mBinding.dcOnhold.officeAdress.text = dcAddress
                        }
                        val storedFcmToken =
                            mViewModel?.getStringValue(PreferenceKey.FCM_TOKEN.name)
                        updateFcmToken(storedFcmToken)
                    } else {
                        showToast(result.description, false)
                    }
                }
            }
        }
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            mBinding.addressSubLayout.directionLogo.id -> {
                val customData = mapOf("Screen" to "FinalStatusActivity", "key1" to 32)
                logButtonClick("DirectionLogo_button", customData)
                getLatLong()
                if (CommonUtils.isInternetAvailable(this)) {
                    navigateToMap(this, dcLat, dcLong)
                } else {
                    snackBar("no internet connection", false)
                }
            }

            mBinding.helpImage.id -> {
                val customData = mapOf("Screen" to "FinalStatusActivity", "key1" to 33)
                logButtonClick("help_image", customData)
                openHelpBottomSheet()
            }

            mBinding.finalRejected.reApplyBtn.id -> {
                mViewModel!!.setBtnClicked(true)
                launchNewActivity<UploadDocumentsActivity> { }
                finishAffinity()
            }

            mBinding.finalVerification.btnSathi.id -> {
                sathi_url?.let {
                    if (it.isNotBlank()) {
                        val downloadId =
                            downloadAPK(it, downloadManager, getString(R.string.sathi), this)
                        lifecycleScope.launch(Dispatchers.IO) {
                            checkDownloadStatus(downloadId, downloadManager)
                        }
                    }
                }
            }

            mBinding.finalVerification.btnSruti.id -> {
                sruti_url?.let {
                    if (it.isNotBlank()) {
                        val downloadId =
                            downloadAPK(it, downloadManager, getString(R.string.sruti), this)
                        lifecycleScope.launch(Dispatchers.IO) {
                            checkDownloadStatus(downloadId, downloadManager)
                        }
                    }
                }
            }

            mBinding.dcOnhold.directionLogo.id -> {
                val customData = mapOf("Screen" to "FinalStatusActivity", "key1" to 32)
                logButtonClick("DirectionLogo_button", customData)
                getLatLong()
                if (CommonUtils.isInternetAvailable(this)) {
                    navigateToMap(this, dcLat, dcLong)
                } else {
                    snackBar("no internet connection", false)
                }
            }

            mBinding.termsCondition.acceptTermsTextView.id -> {
                val customData = mapOf("Screen" to "TermsAndConditionsActivity", "key1" to 33)
                logButtonClick("DirectionLogo_button", customData)
                launchNewActivity<TermsAndConditionsActivity> {
                    putExtra(AppConstants.TERMS_CONDITIONS_STATUS, employee_terms_conditions_status)
                }
            }

        }
    }

    private suspend fun checkDownloadStatus(downloadId: Long, downloadManager: DownloadManager) {
        withContext(Dispatchers.Main) {
            progressDialog().show()
        }
        while (true) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor: Cursor = downloadManager.query(query) ?: return

            if (cursor.moveToFirst()) {
                val columnIndexStatus = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                if (columnIndexStatus != -1) {
                    val status = cursor.getInt(columnIndexStatus)

                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            withContext(Dispatchers.Main) {
                                progressDialog(this@FinalStatusActivity).dismiss()
                                showToast("Download Successful", true)
                            }
                            return
                        }

                        DownloadManager.STATUS_FAILED -> {
                            withContext(Dispatchers.Main) {
                                progressDialog(this@FinalStatusActivity).dismiss()
                                showToast("Download Failed", false)
                            }
                            return
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        progressDialog(this@FinalStatusActivity).dismiss()
                        showToast("Download Successful", true)
                    }
                    return
                }
            } else {
                withContext(Dispatchers.Main) {
                    progressDialog(this@FinalStatusActivity).dismiss()
                    showToast("Download Cancelled", false)
                }
                return
            }
            cursor.close()
            delay(1000)
        }
    }

    override fun onBackPressed() {
        super.onBackPressedDispatcher.onBackPressed()
        finishAffinity()
    }

    private fun checkStoragePermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun updateFcmToken(storedFcmToken: String?) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    storedFcmToken?.let { it1 -> mViewModel?.updateApiFcmToken(it1) }

                } else {
                    val fcmToken = task.result
                    if (storedFcmToken != fcmToken) {
                        mViewModel?.setFcmToken(fcmToken)
                        mViewModel?.updateApiFcmToken(fcmToken)
                    }

                }
            })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(
                    this, "Permission denied. Cannot download the APK.", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

