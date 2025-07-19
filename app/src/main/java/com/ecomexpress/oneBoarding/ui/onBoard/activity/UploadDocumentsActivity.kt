package com.ecomexpress.oneBoarding.ui.onBoard.activity


import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ecomexpress.oneBoarding.BuildConfig.BASE_URL
import com.ecomexpress.oneBoarding.R
import com.ecomexpress.oneBoarding.data.datasource.local.preference.PreferenceKey
import com.ecomexpress.oneBoarding.data.model.CommonRequest
import com.ecomexpress.oneBoarding.data.model.CommonResponse
import com.ecomexpress.oneBoarding.data.model.onBoarding.DocDetails
import com.ecomexpress.oneBoarding.data.model.onBoarding.IfscResponse
import com.ecomexpress.oneBoarding.databinding.ActivityUploadDocumentsBinding
import com.ecomexpress.oneBoarding.databinding.BottomsheetAadharBackphotoNewBinding
import com.ecomexpress.oneBoarding.databinding.BottomsheetBankDetailsLayoutBinding
import com.ecomexpress.oneBoarding.databinding.BottomsheetDialoguePancardBinding
import com.ecomexpress.oneBoarding.databinding.BottomsheetDrivingLicenseBinding
import com.ecomexpress.oneBoarding.databinding.BottomsheetFrontPhotoAadhaarBinding
import com.ecomexpress.oneBoarding.databinding.BottomsheetGalleryOptionLayoutBinding
import com.ecomexpress.oneBoarding.databinding.BottomsheetReferalcodeBinding
import com.ecomexpress.oneBoarding.databinding.DialogueImageNotValidBinding
import com.ecomexpress.oneBoarding.databinding.DialougeNotLinkingBinding
import com.ecomexpress.oneBoarding.databinding.ReferenceDialogBinding
import com.ecomexpress.oneBoarding.ui.base.BaseActivity
import com.ecomexpress.oneBoarding.ui.onBoard.viewmodel.UploadDocumentsViewModel
import com.ecomexpress.oneBoarding.utils.cameraX.CameraxActivity
import com.ecomexpress.oneBoarding.utils.comman.AppConstants.BACKAADHAAR
import com.ecomexpress.oneBoarding.utils.comman.AppConstants.BACK_DOC_UPLOAD
import com.ecomexpress.oneBoarding.utils.comman.AppConstants.BANK
import com.ecomexpress.oneBoarding.utils.comman.AppConstants.CAME_FROM
import com.ecomexpress.oneBoarding.utils.comman.AppConstants.CAME_FROM_RETAKE
import com.ecomexpress.oneBoarding.utils.comman.AppConstants.CHEQUE_DOC_UPLOAD
import com.ecomexpress.oneBoarding.utils.comman.AppConstants.CONFIRM_UPLOAD_PROFILE
import com.ecomexpress.oneBoarding.utils.comman.AppConstants.DL
import com.ecomexpress.oneBoarding.utils.comman.AppConstants.FRONTAADHAAR
import com.ecomexpress.oneBoarding.utils.comman.AppConstants.FRONT_DOC_UPLOAD
import com.ecomexpress.oneBoarding.utils.comman.AppConstants.GET_FROM_CAMERA
import com.ecomexpress.oneBoarding.utils.comman.AppConstants.ONLY_FRONT_ENABLE
import com.ecomexpress.oneBoarding.utils.comman.AppConstants.PAN
import com.ecomexpress.oneBoarding.utils.comman.AppConstants.UPLOAD_BACK_ADHAR
import com.ecomexpress.oneBoarding.utils.comman.AppConstants.UPLOAD_BANK
import com.ecomexpress.oneBoarding.utils.comman.AppConstants.UPLOAD_FRONT_ADHAR_CLICK
import com.ecomexpress.oneBoarding.utils.comman.AppConstants.UPLOAD_PROFILE
import com.ecomexpress.oneBoarding.utils.comman.CommonUtils
import com.ecomexpress.oneBoarding.utils.comman.CommonUtils.getBase64String
import com.ecomexpress.oneBoarding.utils.comman.CommonUtils.isValidIFSCCode
import com.ecomexpress.oneBoarding.utils.comman.CommonUtils.isValidPanCardNo
import com.ecomexpress.oneBoarding.utils.comman.CommonUtils.isValid_Bank_Acc_Number
import com.ecomexpress.oneBoarding.utils.comman.CommonUtils.setError
import com.ecomexpress.oneBoarding.utils.comman.launchNewActivity
import com.ecomexpress.oneBoarding.utils.comman.onError
import com.ecomexpress.oneBoarding.utils.comman.onLoading
import com.ecomexpress.oneBoarding.utils.comman.onSuccess
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.theartofdev.edmodo.cropper.CropImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class UploadDocumentsActivity :
    BaseActivity<UploadDocumentsViewModel, ActivityUploadDocumentsBinding>(), View.OnClickListener,
    TextWatcher {
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var dlBottomSheetDialog: BottomSheetDialog
    private lateinit var bankBottomSheetDialog: BottomSheetDialog
    private lateinit var aadhaarFrontBottomSheetDialog: BottomSheetDialog
    private lateinit var aadhaarBackBottomSheetDialog: BottomSheetDialog
    private lateinit var bottomSheetBankDetailsLayoutBinding: BottomsheetBankDetailsLayoutBinding
    private lateinit var bottomSheetFrontPhotoAadhaarBinding: BottomsheetFrontPhotoAadhaarBinding
    private lateinit var bottomSheetBackPhotoAadharBackPhotoBinding: BottomsheetAadharBackphotoNewBinding
    private lateinit var bottomSheetDrivingLicenseBinding: BottomsheetDrivingLicenseBinding
    private lateinit var bottomSheetPanCardBinding: BottomsheetDialoguePancardBinding
    private lateinit var panCardDialog: BottomSheetDialog
    var cameFrom = ""
    private var isFrontImageUplaod = false
    private var isBackImageUpload = false
    private var isBankImageUpload = false
    private var isChequeUploadSuceessful = false
    private var doc_file = DocDetails()
    private var is_adhar_verified = false
    private var is_bank_verified = false
    private var is_pan_verified = false
    private var is_dl_verified = false
    var isProfileUploaded = false
    private var frontAdharBitmap: Bitmap? = null
    private var backAdharBitmap: Bitmap? = null
    var bankChequeBitmap: Bitmap? = null
    var docUploadFrom = ""
    var authToken = ""
    var isReferral = false
    var currentBottomSheet: ViewDataBinding? = null
    var selectedGender = ""
    var isIfscValid: Boolean = false
    var bank_name = ""
    override fun getLayout(): Int = R.layout.activity_upload_documents
    override fun getViewModelClass(): Class<UploadDocumentsViewModel> =
        UploadDocumentsViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.lifecycleOwner = this
        initialize()
        mViewModel!!.documentDetails()
        setNavigator(
            mBinding.commonBg.imageView,
            mBinding.commonBg.imageView2,
            mBinding.commonBg.view1,
            mBinding.commonBg.view3,
            mBinding.commonBg.view4,
            mBinding.commonBg.tvLocation,
            mBinding.commonBg.tvRegister,
            null
        )

        mBinding.submitAppCompatButton.setBackgroundResource(R.drawable.disable_button)
        mBinding.submitAppCompatButton.setTextColor(
            ContextCompat.getColor(
                applicationContext, R.color.grey_A3
            )
        )
        mBinding.commonBg.tempId.text = mViewModel!!.getTempId()
        collectData()
        analyticsToAllScreen(
            "Upload_Document_Activity", this@UploadDocumentsActivity.localClassName
        )
        isReferral = mViewModel!!.getBooleanStatus(PreferenceKey.IS_REFER)

        manageDocumentDetailStatus()
    }

    override fun onPause() {
        super.onPause()
        if (progressDialog(this@UploadDocumentsActivity).isShowing) {
            progressDialog(this@UploadDocumentsActivity).dismiss()
        }

    }

    private fun initialize() {
        mBinding.ivClickFront.setOnClickListener(this)
        mBinding.aadharAppCompatImageView.setOnClickListener(this)
        mBinding.ivPanCardDetails.setOnClickListener(this)
        mBinding.ivBankDetails.setOnClickListener(this)
        mBinding.ivDrivingDetails.setOnClickListener(this)
        mBinding.tvFrontClick.setOnClickListener(this)
        mBinding.submitAppCompatButton.setOnClickListener(this)
        mBinding.commonBg.helpIv.setOnClickListener(this)
        mBinding.cameraButton.setOnClickListener(this)
        mBinding.submitAppCompatButton.setOnClickListener(this)
        mBinding.ivReferenceDetails.setOnClickListener(this)
    }

    private fun collectData() {
        lifecycleScope.launch(Dispatchers.Main) {
            launch {
                mViewModel!!.docUploadFlow.collect {
                    it.onLoading {
                        if (this@UploadDocumentsActivity::aadhaarBackBottomSheetDialog.isInitialized && aadhaarBackBottomSheetDialog.isShowing) {
                            aadhaarBackBottomSheetDialog.dismiss()
                        }
                        if (this@UploadDocumentsActivity::aadhaarFrontBottomSheetDialog.isInitialized && aadhaarFrontBottomSheetDialog.isShowing) {
                            aadhaarFrontBottomSheetDialog.dismiss()
                        }
                        when (docUploadFrom) {
                            CHEQUE_DOC_UPLOAD -> {
                                progressDialog(
                                    this@UploadDocumentsActivity,
                                    bottomSheetBankDetailsLayoutBinding
                                ).show()
                            }

                            else -> {
                                progressDialog(this@UploadDocumentsActivity).show()
                            }
                        }
                    }.onError {
                        when (docUploadFrom) {
                            CHEQUE_DOC_UPLOAD -> {
                                progressDialog(
                                    this@UploadDocumentsActivity,
                                    bottomSheetBankDetailsLayoutBinding
                                ).dismiss()
                            }

                            else -> {
                                progressDialog(this@UploadDocumentsActivity).dismiss()
                            }
                        }
                        it.message?.let { it1 -> showToast(it1, false) }
                    }.onSuccess {
                        val res = it
                        when (docUploadFrom) {
                            FRONT_DOC_UPLOAD -> {
                                docUploadFrom = ""
                                progressDialog(this@UploadDocumentsActivity).dismiss()
                                if (res.success) {
                                    showToast(res.description, true)
                                    frontAdharBitmap?.let { it1 ->
                                        openFrontAdhaarBottomSheet(
                                            it1, res
                                        )
                                    }

                                    // check linking process
                                    if (res.data?.docLink == true && res.data?.isLinking == false) {
                                        //show popup with message "Your PAN & Aadhaar are not linked so 20% TDS will be deducted."
                                        dialogueForNotLinking()
                                    }

                                } else {
                                    if (res.description.contains("manually")) {
                                        dialogueForImgNotValid(
                                            res.description, true, frontAdharBitmap, "front", res
                                        )
                                    } else {
                                        dialogueForImgNotValid(
                                            res.description, false, frontAdharBitmap, "", res
                                        )
                                    }
                                }
                            }

                            BACK_DOC_UPLOAD -> {
                                docUploadFrom = ""
                                progressDialog(this@UploadDocumentsActivity).dismiss()
                                if (res.success) {
                                    backAdharBitmap?.let { it1 ->
                                        openBackAadharBottomsheet(
                                            it1, res
                                        )
                                    }

                                    // check linking process
                                    if (res.data?.docLink == true && res.data?.isLinking == false) {
                                        //show popup with message "Your PAN & Aadhaar are not linked so 20% TDS will be deducted."
                                        dialogueForNotLinking()
                                    }

                                } else {
                                    if (res.description.contains("manually")) {
                                        dialogueForImgNotValid(
                                            res.description, true, backAdharBitmap, "back", res
                                        )
                                    } else {
                                        dialogueForImgNotValid(
                                            res.description, false, backAdharBitmap, "", res
                                        )
                                    }
                                }
                            }

                            CHEQUE_DOC_UPLOAD -> {
                                docUploadFrom = ""
                                progressDialog(
                                    this@UploadDocumentsActivity,
                                    bottomSheetBankDetailsLayoutBinding
                                ).dismiss()
                                if (res.success) {
                                    isBankImageUpload = true
                                    showToast(res.description, true)
                                    isChequeUploadSuceessful = true
                                    bottomSheetBankDetailsLayoutBinding.continueAppCompatButton1.isEnabled =
                                        true
                                    bottomSheetBankDetailsLayoutBinding.continueAppCompatButton1.setBackgroundResource(
                                        (R.drawable.button)
                                    )
                                    bottomSheetBankDetailsLayoutBinding.continueAppCompatButton1.setTextColor(
                                        ContextCompat.getColor(
                                            applicationContext, R.color.white
                                        )
                                    )
                                    bottomSheetBankDetailsLayoutBinding.verifiedName.visibility =
                                        View.GONE
                                    bottomSheetBankDetailsLayoutBinding.ivCancelCheck.setImageBitmap(
                                        bankChequeBitmap
                                    )
                                } else {
                                    dialogueForImgNotValid(
                                        res.description, false, bankChequeBitmap, "", res
                                    )
                                }
                            }
                        }
                    }
                }
            }

            lifecycleScope.launch(Dispatchers.Main) {
                mViewModel!!.commonReferralCodeResponse.collect { it ->
                    it.onError {
                        progressDialog(this@UploadDocumentsActivity).dismiss()
                        showToast(it.message!!, false)
                    }
                    it.onLoading {
                        progressDialog(this@UploadDocumentsActivity).show()
                    }
                    it.onSuccess { response ->
                        val result = response as CommonResponse
                        if (result.success) {
                            showToast(result.description, true)
                            finishAffinity()
                            launchNewActivity<FinalStatusActivity>()
                        } else {
                            showToast(result.description, false)
                        }
                        progressDialog(this@UploadDocumentsActivity).dismiss()
                    }
                }
            }

            launch {
                mViewModel!!.commonManualDocResponse.collect {
                    it.onLoading {
                        progressDialog(this@UploadDocumentsActivity, currentBottomSheet!!).show()
                    }.onError {
                        progressDialog(this@UploadDocumentsActivity, currentBottomSheet!!).dismiss()

                        it.message?.let { it1 -> showToast(it1, false) }
                    }.onSuccess {
                        progressDialog(this@UploadDocumentsActivity, currentBottomSheet!!).dismiss()
                        val res = it as CommonResponse
                        if (res.success) {
                            when (cameFrom) {
                                PAN -> {
                                    is_pan_verified = true
                                    doc_file.pancardno = res.data!!.doc_number.toString()
                                    panCardDialog.dismiss()
                                    cameFrom = ""
                                    mViewModel!!.updateDocumentStatus(
                                        PreferenceKey.PAN_STATUS.name, 1
                                    )
                                    doc_file.is_pan = true
                                    doc_file.pancardno =
                                        bottomSheetPanCardBinding.edPanNo.text.toString()
                                    mBinding.pancardStatus.text = getString(R.string.verified)


                                    mBinding.pancardStatus.setTextColor(
                                        ContextCompat.getColor(
                                            this@UploadDocumentsActivity, R.color.green_92
                                        )
                                    )
                                    mBinding.pancardStatus.setCompoundDrawablesWithIntrinsicBounds(
                                        R.drawable.ic_verified, 0, 0, 0
                                    )
                                    snackBar(res.description, true)
                                    manageSubmitButton()

                                    // check linking process
                                    if (res.data.docLink == true && res.data.isLinking == false) {
                                        //show popup with message "Your PAN & Aadhaar are not linked so 20% TDS will be deducted."
                                        dialogueForNotLinking()
                                    }
                                }

                                DL -> {
                                    is_dl_verified = true
                                    dlBottomSheetDialog.dismiss()
                                    cameFrom = ""
                                    mViewModel!!.updateDocumentStatus(
                                        PreferenceKey.DRIVING_STATUS.name, 1
                                    )
                                    doc_file.dlno = res.data!!.doc_number.toString()
                                    doc_file.dlexpiry = res.data.expiry_date.toString()
                                    doc_file.dlno =
                                        bottomSheetDrivingLicenseBinding.drivingLicenceEd.text.toString()
                                    doc_file.dlexpiry =
                                        bottomSheetDrivingLicenseBinding.drivingLicenceDateEd.text.toString()
                                    mBinding.dlStatus.text = getString(R.string.available)


                                    mBinding.dlStatus.setTextColor(
                                        ContextCompat.getColor(
                                            this@UploadDocumentsActivity, R.color.green_92
                                        )
                                    )
                                    mBinding.dlStatus.setCompoundDrawablesWithIntrinsicBounds(
                                        R.drawable.ic_verified, 0, 0, 0
                                    )
                                    bottomSheetDrivingLicenseBinding.drivingLicenceDateEd.text = ""
                                    snackBar(res.description, true)
                                    manageSubmitButton()
                                }

                                FRONTAADHAAR -> {
                                    cameFrom = ""
                                    aadhaarFrontBottomSheetDialog.dismiss()
                                    isFrontImageUplaod = true
                                    mBinding.tvFrontClick.visibility = View.GONE
                                    mBinding.ivClickFront.setImageBitmap(frontAdharBitmap)
                                    mBinding.ivClickFront.layoutParams.height = 200
                                    mBinding.ivClickFront.requestLayout()
                                    mBinding.ivClickFront.scaleType = ImageView.ScaleType.FIT_XY
                                    mViewModel!!.setBooleanStatus(PreferenceKey.FRONT_AADHAAR_STATUS)
                                    if (isBackImageUpload) {
                                        is_adhar_verified = true
//                                        isBackImageUpload = false
//                                        isFrontImageUplaod = false
                                        mBinding.tvPending.text = getString(R.string.verified)
                                        mBinding.tvPending.setTextColor(
                                            ContextCompat.getColor(
                                                this@UploadDocumentsActivity, R.color.green_92
                                            )
                                        )
                                        mBinding.tvPending.setCompoundDrawablesWithIntrinsicBounds(
                                            R.drawable.ic_verified, 0, 0, 0
                                        )
                                    } else {
                                        mViewModel!!.updateDocumentStatus(
                                            PreferenceKey.AADHAAR_STATUS.name, 2
                                        )
                                    }
                                    if (is_adhar_verified) {
                                        mViewModel!!.updateDocumentStatus(
                                            PreferenceKey.AADHAAR_STATUS.name, 1
                                        )
                                    }
                                    snackBar(res.description, true)
                                    manageSubmitButton()

                                    // check linking process
                                    if (res.data?.docLink == true && res.data.isLinking == false) {
                                        //show popup with message "Your PAN & Aadhaar are not linked so 20% TDS will be deducted."
                                        dialogueForNotLinking()
                                    }
                                }

                                BACKAADHAAR -> {
                                    cameFrom = ""
                                    aadhaarBackBottomSheetDialog.dismiss()
                                    isBackImageUpload = true
                                    mViewModel!!.setBooleanStatus(PreferenceKey.BACK_AADHAAR_STATUS)
                                    mBinding.aadharAppCompatImageView.setImageBitmap(backAdharBitmap)
                                    mBinding.tvBackText.visibility = View.GONE
                                    mBinding.aadharAppCompatImageView.layoutParams.height = 200
                                    mBinding.aadharAppCompatImageView.requestLayout()
                                    mBinding.aadharAppCompatImageView.scaleType =
                                        ImageView.ScaleType.FIT_XY
                                    if (isFrontImageUplaod) {
                                        is_adhar_verified = true
//                                        isBackImageUpload = false
//                                        isFrontImageUplaod = false
                                        mBinding.tvPending.text = getString(R.string.verified)
                                        mBinding.tvPending.setTextColor(
                                            ContextCompat.getColor(
                                                this@UploadDocumentsActivity, R.color.green_92
                                            )
                                        )
                                        mBinding.tvPending.setCompoundDrawablesWithIntrinsicBounds(
                                            R.drawable.ic_verified, 0, 0, 0
                                        )
                                    } else {
                                        mViewModel!!.updateDocumentStatus(
                                            PreferenceKey.AADHAAR_STATUS.name, 2
                                        )
                                    }
                                    if (is_adhar_verified) {
                                        mViewModel!!.updateDocumentStatus(
                                            PreferenceKey.AADHAAR_STATUS.name, 1
                                        )
                                    }
                                    snackBar(res.description, true)
                                    manageSubmitButton()

                                    // check linking process
                                    if (res.data?.docLink == true && res.data.isLinking == false) {
                                        //show popup with message "Your PAN & Aadhaar are not linked so 20% TDS will be deducted."
                                        dialogueForNotLinking()
                                    }
                                }

                                BANK -> {
                                    if (res.data!!.retry) {
                                        cameFrom = ""
                                        doc_file.account_holder_name = res.data.account_holder_name
                                        bottomSheetBankDetailsLayoutBinding.verifiedName.visibility =
                                            View.VISIBLE
                                        bottomSheetBankDetailsLayoutBinding.tvHolderName.text =
                                            res.data.account_holder_name
                                        bottomSheetBankDetailsLayoutBinding.continueAppCompatButton1.text =
                                            getString(R.string.confirm)
                                        showToast(res.description, true)
                                    }
                                }
                            }
                        } else {
                            when (cameFrom) {
                                BANK -> {
                                    if (!res.data!!.retry) {/*bottomSheetBankDetailsLayoutBinding.tilEnterName.visibility =
                                            View.VISIBLE*/
                                        bottomSheetBankDetailsLayoutBinding.enterNameTv.visibility =
                                            View.GONE
                                        bottomSheetBankDetailsLayoutBinding.cvCancelCheck.visibility =
                                            View.VISIBLE
                                        bottomSheetBankDetailsLayoutBinding.verifiedName.visibility =
                                            View.GONE
                                        bottomSheetBankDetailsLayoutBinding.continueAppCompatButton1.isEnabled =
                                            false
                                        bottomSheetBankDetailsLayoutBinding.continueAppCompatButton1.setBackgroundResource(
                                            (R.drawable.disable_button)
                                        )
                                        bottomSheetBankDetailsLayoutBinding.continueAppCompatButton1.setTextColor(
                                            ContextCompat.getColor(
                                                applicationContext, R.color.grey_A3
                                            )
                                        )
                                    }
                                }

                                PAN -> {
                                    is_pan_verified = false
                                    mViewModel!!.updateDocumentStatus(
                                        PreferenceKey.PAN_STATUS.name, 0
                                    )
                                    doc_file.is_pan = true
                                    doc_file.pancardno =
                                        bottomSheetPanCardBinding.edPanNo.text.toString()
                                    mBinding.pancardStatus.text = getString(R.string.pending)


                                    mBinding.pancardStatus.setTextColor(
                                        ContextCompat.getColor(
                                            this@UploadDocumentsActivity, R.color.orange
                                        )
                                    )
                                    mBinding.pancardStatus.setCompoundDrawablesWithIntrinsicBounds(
                                        R.drawable.ic_pending, 0, 0, 0
                                    )
                                    manageSubmitButton()
                                }
                            }
                            showToast(res.description, false)
                        }
                    }
                }
            }
            launch {
                mViewModel!!.documentDetailsFlow.collect {
                    it.onError {
                        progressDialog(this@UploadDocumentsActivity).dismiss()
                        showToast(it.message!!, false)
                    }
                    it.onLoading {
                        progressDialog(this@UploadDocumentsActivity).show()
                    }
                    it.onSuccess { res ->
                        progressDialog(this@UploadDocumentsActivity).dismiss()
                        val result = res as CommonResponse
                        if (res.success) {

                            //hide click front photo and click back photo
                            if (!res.data?.img_aadhar_first.isNullOrEmpty() && isFrontImageUplaod) mBinding.tvFrontClick.visibility =
                                View.GONE
                            if (!res.data?.img_aadhar_second.isNullOrEmpty() && isBackImageUpload) mBinding.tvBackText.visibility =
                                View.GONE

                            doc_file.aadharno = res.data!!.aadhar_number
                            doc_file.aadharname = res.data.aadhar_name
                            doc_file.accountno = res.data.bank_number
                            doc_file.account_holder_name = res.data.account_holder_name
                            doc_file.current_address = res.data.current_address
                            doc_file.current_pincode = res.data.current_pincode
                            doc_file.current_state = res.data.current_state
                            doc_file.dlno = res.data.dl_number
                            doc_file.dlexpiry = res.data.expiry_date
                            doc_file.dob = res.data.dob
                            doc_file.gender = res.data.gender
                            doc_file.ifsccode = res.data.ifsc_code
                            doc_file.is_pan = res.data.is_pan
                            doc_file.pancardno = res.data.pan_number
                            doc_file.permanent_address = res.data.permanent_address
                            doc_file.permanent_pincode = res.data.permanent_pincode
                            doc_file.permanent_state = res.data.permanent_state
                            doc_file.img_aadhar_first = res.data.img_aadhar_first
                            doc_file.img_aadhar_second = res.data.img_aadhar_second
                            doc_file.img_profile = res.data.img_profile
                            doc_file.img_cheque = res.data.img_cheque
//                            doc_file.is_profile = res.data.is_profile
                            if (doc_file.img_profile?.isEmpty() != true) {
                                isProfileUploaded = true
                            }
                            authToken = repository.getDataStoreContext()
                                .getString(PreferenceKey.ACCESS_TOKEN.name)!!.split(" ")[1]
                            uploadDocImages(authToken)
                            manageSubmitButton()
                        } else {
                            showToast(result.description, false)
                        }
                    }
                }
            }
            launch {
                mViewModel!!.ifscResponse.collect {
                    it.onError {
                        isIfscValid = false
                        progressDialog(this@UploadDocumentsActivity).dismiss()
                        bottomSheetBankDetailsLayoutBinding.ifscErrorTxt.visibility = View.VISIBLE
                        bottomSheetBankDetailsLayoutBinding.ifscSuccess.visibility = View.GONE
                        manageBankButton()
                    }
                    it.onLoading {
                        progressDialog(this@UploadDocumentsActivity).show()
                    }
                    it.onSuccess {
                        progressDialog(this@UploadDocumentsActivity).dismiss()
                        isIfscValid = true
                        bottomSheetBankDetailsLayoutBinding.ifscErrorTxt.visibility = View.GONE
                        bottomSheetBankDetailsLayoutBinding.ifscSuccess.visibility = View.VISIBLE
                        val res = it as IfscResponse
                        bank_name = res.BANK
                        bottomSheetBankDetailsLayoutBinding.ifscBankTxt.text = res.BANK
                        bottomSheetBankDetailsLayoutBinding.ifscBranchTxt.text = res.BRANCH
                        manageBankButton()
                    }
                }
            }
        }

    }

    private fun openBottomSheetViaPanCard() {
        panCardDialog = BottomSheetDialog(this, R.style.otpsheetDialogTheme)
        bottomSheetPanCardBinding = BottomsheetDialoguePancardBinding.inflate(layoutInflater)
        panCardDialog.setCancelable(true)
        val behavior = panCardDialog.behavior
        behavior.saveFlags = BottomSheetBehavior.SAVE_SKIP_COLLAPSED
        panCardDialog.setContentView(bottomSheetPanCardBinding.root)
        panCardDialog.show()
        currentBottomSheet = bottomSheetPanCardBinding
        if (!doc_file.pancardno.isNullOrBlank()) {
            bottomSheetPanCardBinding.edPanNo.setText(doc_file.pancardno)
        }
        bottomSheetPanCardBinding.edPanNo.addTextChangedListener(this)
        bottomSheetPanCardBinding.btnContinue.setOnClickListener {
            if (bottomSheetPanCardBinding.edPanNo.text?.length == 10) {
                cameFrom = PAN
                mViewModel?.updateManualDocService(
                    docType = PAN,
                    docNum = bottomSheetPanCardBinding.edPanNo.text.toString(),
                    pan = true
                )
            } else {
                setError(
                    bottomSheetPanCardBinding.txtInputPan,
                    getString(R.string.enter_valid_pan_number)
                )
            }
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            mBinding.aadharAppCompatImageView.id -> {
                logButtonClick(
                    "aadharButton", mapOf("Screen" to "UploadDocumentsActivity", "key1" to 11)
                )
                CAME_FROM = UPLOAD_BACK_ADHAR
                if (!this::bottomSheetDialog.isInitialized || !bottomSheetDialog.isShowing) {
                    openBottomCameraOptions()
                }
            }

            mBinding.ivPanCardDetails.id -> {
                logButtonClick(
                    "PanCardButton", mapOf("Screen" to "UploadDocumentsActivity", "key1" to 12)
                )
                if (!this::panCardDialog.isInitialized || !panCardDialog.isShowing) {
                    openBottomSheetViaPanCard()
                }
            }

            mBinding.ivBankDetails.id -> {
                logButtonClick(
                    "BankButton", mapOf("Screen" to "UploadDocumentsActivity", "key1" to 13)
                )
                if (!this::bankBottomSheetDialog.isInitialized || !bankBottomSheetDialog.isShowing) {
                    openBottomSheetbank()
                }
            }

            mBinding.commonBg.helpIv.id -> {
                logButtonClick(
                    "HelpButton", mapOf("Screen" to "UploadDocumentsActivity", "key1" to 14)
                )
                if (!this::panCardDialog.isInitialized || !panCardDialog.isShowing) openHelpBottomSheet()
            }

            mBinding.ivDrivingDetails.id -> {
                logButtonClick(
                    "DrivingButton", mapOf("Screen" to "UploadDocumentsActivity", "key1" to 15)
                )
                if (!this::dlBottomSheetDialog.isInitialized || !dlBottomSheetDialog.isShowing) {
                    drivingLicense()
                }
            }

            mBinding.ivReferenceDetails.id -> {
                launchNewActivity<ReferenceActivity>()
            }

            mBinding.ivClickFront.id -> {
                logButtonClick(
                    "FrontImageButton", mapOf("Screen" to "UploadDocumentsActivity", "key1" to 16)
                )
                CAME_FROM = UPLOAD_FRONT_ADHAR_CLICK
                if (!this::bottomSheetDialog.isInitialized || !bottomSheetDialog.isShowing) {
                    openBottomCameraOptions()
                }
            }

            R.id.cameraButton -> {
                logButtonClick(
                    "CameraButton", mapOf("Screen" to "UploadDocumentsActivity", "key1" to 17)
                )
                openCamera(true)
                CAME_FROM = UPLOAD_PROFILE
            }

            R.id.submit_AppCompatButton -> {
                logButtonClick(
                    "SubmitButton", mapOf("Screen" to "UploadDocumentsActivity", "key1" to 18)
                )
                if (!isReferral) {
                    if (!mViewModel!!.getBooleanStatus(PreferenceKey.IS_REFERENCE)) {
                        openReferenceDialog()
                    } else {
                        openReferralCodeBottomSheet()
                    }
                } else {
                    logButtonClick(
                        "skipButton", mapOf("Screen" to "UploadDocumentActivity", "key1" to 20)
                    )
                    finishAffinity()
                    launchNewActivity<FinalStatusActivity>()
                }
            }
        }
    }

    private fun openReferralCodeBottomSheet() {
        bottomSheetDialog = BottomSheetDialog(this, R.style.otpsheetDialogTheme)
        val bottomSheetReferralCodeBinding = BottomsheetReferalcodeBinding.inflate(layoutInflater)
        bottomSheetDialog.setCancelable(true)
        bottomSheetDialog.setContentView(bottomSheetReferralCodeBinding.root)
        bottomSheetDialog.show()
        currentBottomSheet = bottomSheetReferralCodeBinding
        bottomSheetReferralCodeBinding.verifyButton.setOnClickListener {
            val customData = mapOf("Screen" to "UploadDocumentActivity", "key1" to 19)
            logButtonClick("verifyButton", customData)

            mViewModel!!.refferalCode(CommonRequest(referral_code = bottomSheetReferralCodeBinding.enterReferalNo.text.toString()))
        }
        bottomSheetReferralCodeBinding.skipButton.setOnClickListener {
            val customData = mapOf("Screen" to "UploadDocumentActivity", "key1" to 20)
            logButtonClick("skipButton", customData)
            finishAffinity()
            launchNewActivity<FinalStatusActivity>()
        }
    }


    private fun openBottomCameraOptions() {
        bottomSheetDialog = BottomSheetDialog(this, R.style.otpsheetDialogTheme)
        val bottomSheetGalleryOptionLayoutBinding =
            BottomsheetGalleryOptionLayoutBinding.inflate(layoutInflater)
        bottomSheetDialog.setCancelable(true)
        bottomSheetDialog.setContentView(bottomSheetGalleryOptionLayoutBinding.root)
        bottomSheetDialog.show()
        bottomSheetGalleryOptionLayoutBinding.cameraClick.setOnClickListener {
            val customData = mapOf("Screen" to "UploadDocumentActivity", "key1" to 21)
            logButtonClick("cameraClick", customData)
            openCamera(false)
            bottomSheetDialog.dismiss()
        }
        bottomSheetGalleryOptionLayoutBinding.galleryAppCompatImageView.setOnClickListener {
            val customData = mapOf("Screen" to "UploadDocumentActivity", "key1" to 22)
            logButtonClick("galleryAppCompatImageView", customData)
            imageChooser()
            bottomSheetDialog.dismiss()
        }
    }

    private fun drivingLicense() {
        dlBottomSheetDialog = BottomSheetDialog(this, R.style.otpsheetDialogTheme)
        bottomSheetDrivingLicenseBinding = BottomsheetDrivingLicenseBinding.inflate(layoutInflater)
        dlBottomSheetDialog.setCancelable(true)
        dlBottomSheetDialog.setContentView(bottomSheetDrivingLicenseBinding.root)
        dlBottomSheetDialog.show()
        currentBottomSheet = bottomSheetDrivingLicenseBinding
        if (!doc_file.dlno.isNullOrBlank()) {
            bottomSheetDrivingLicenseBinding.drivingLicenceEd.setText(doc_file.dlno)
        }
        if (!doc_file.dlexpiry.isNullOrBlank()) {
            bottomSheetDrivingLicenseBinding.drivingLicenceDateEd.text = doc_file.dlexpiry
        }
        bottomSheetDrivingLicenseBinding.drivingLicenceEd.addTextChangedListener(this)

        bottomSheetDrivingLicenseBinding.drivingLicenceDateEd.addTextChangedListener(this)
        bottomSheetDrivingLicenseBinding.drivingLicenceDateEd.setOnClickListener {
            openDatePicker(false)
        }


        bottomSheetDrivingLicenseBinding.continueAppCompatButton.setOnClickListener {
            val customData = mapOf("Screen" to "UploadDocumentActivity", "key1" to 24)
            logButtonClick("continueAppCompatButton", customData)
            if (bottomSheetDrivingLicenseBinding.drivingLicenceEd.text.toString().isEmpty()) {
                snackBar(getString(R.string.enter_driving_licence), false)
            } else if (bottomSheetDrivingLicenseBinding.drivingLicenceDateEd.text.toString()
                    .isEmpty()
            ) {
                snackBar(getString(R.string.provide_valid_expiry_date), false)
            } else {
                cameFrom = DL
                mViewModel!!.updateManualDocService(
                    docType = DL,
                    docNum = bottomSheetDrivingLicenseBinding.drivingLicenceEd.text.toString(),
                    pan = true,
                    expiry = bottomSheetDrivingLicenseBinding.drivingLicenceDateEd.text.toString()
                )
            }
        }
    }

    private fun openDatePicker(type: Boolean) {
        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(
            this@UploadDocumentsActivity, { arg0, year, month, day_of_month ->
                calendar[Calendar.YEAR] = year
                calendar[Calendar.MONTH] = month
                calendar[Calendar.DAY_OF_MONTH] = day_of_month
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)

                if (type) {
                    bottomSheetFrontPhotoAadhaarBinding.edDob.text = sdf.format(calendar.time)
                    manageAadhaarFrontButtonState()
                } else {
                    bottomSheetDrivingLicenseBinding.drivingLicenceDateEd.text = sdf.format(
                        calendar.time
                    )
                }
            }, calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DAY_OF_MONTH]
        )
        // dialog.datePicker.maxDate = calendar.timeInMillis
        calendar.timeInMillis

        if (!type) {
            dialog.datePicker.minDate = (System.currentTimeMillis() - 1000)
        } else {
            dialog.datePicker.maxDate = System.currentTimeMillis() - 568025136000L

        }
        dialog.show()
    }

    fun openBackAadharBottomsheet(bitmap: Bitmap, res: CommonResponse) {
        aadhaarBackBottomSheetDialog = BottomSheetDialog(this, R.style.otpsheetDialogTheme)
        bottomSheetBackPhotoAadharBackPhotoBinding =
            BottomsheetAadharBackphotoNewBinding.inflate(layoutInflater)
        bottomSheetBackPhotoAadharBackPhotoBinding.enterAdressEd.addTextChangedListener(this)
        aadhaarBackBottomSheetDialog.setCancelable(true)
        aadhaarBackBottomSheetDialog.setContentView(bottomSheetBackPhotoAadharBackPhotoBinding.root)
        currentBottomSheet = bottomSheetBackPhotoAadharBackPhotoBinding

        bottomSheetBackPhotoAadharBackPhotoBinding.properAddressTv.addTextChangedListener(this)
        bottomSheetBackPhotoAadharBackPhotoBinding.prmntStateEd.addTextChangedListener(this)
        bottomSheetBackPhotoAadharBackPhotoBinding.prmntPincodeEd.addTextChangedListener(this)
        bottomSheetBackPhotoAadharBackPhotoBinding.stateEd.addTextChangedListener(this)
        bottomSheetBackPhotoAadharBackPhotoBinding.pincodeEd.addTextChangedListener(this)
        if (!res.data!!.permanent_address.isNullOrBlank()) {
            bottomSheetBackPhotoAadharBackPhotoBinding.properAddressTv.setText(res.data.permanent_address)
        }
        if (!res.data.permanent_address.isNullOrBlank()) {
            bottomSheetBackPhotoAadharBackPhotoBinding.properAddressTv.setText(res.data.permanent_address)
        }
        if (!res.data.permanent_pincode.isNullOrBlank()) {
            bottomSheetBackPhotoAadharBackPhotoBinding.prmntPincodeEd.setText(res.data.permanent_pincode)
        }
        if (!res.data.permanent_state.isNullOrBlank()) {
            bottomSheetBackPhotoAadharBackPhotoBinding.prmntStateEd.setText(res.data.permanent_state)
        }

        bottomSheetBackPhotoAadharBackPhotoBinding.checkBox.setOnCheckedChangeListener { compoundButton, b ->
            manageAadhaarCheckbox(b)
        }
        bottomSheetBackPhotoAadharBackPhotoBinding.ivAadharcard.setImageBitmap(bitmap)
        bottomSheetBackPhotoAadharBackPhotoBinding.imageClick.setOnClickListener {
            val customData = mapOf("Screen" to "UploadDocumentActivity", "key1" to 25)
            logButtonClick("imageClick", customData)
            CAME_FROM = UPLOAD_BACK_ADHAR
            openCamera(false)
        }
        bottomSheetBackPhotoAadharBackPhotoBinding.continueAppCompatButton1.setOnClickListener {
            val customData = mapOf("Screen" to "UploadDocumentActivity", "key1" to 26)
            logButtonClick("continueAppCompatButton1", customData)
            cameFrom = BACKAADHAAR
            mViewModel!!.updateManualDocService(
                docType = BACKAADHAAR,
                permanentAddress = bottomSheetBackPhotoAadharBackPhotoBinding.properAddressTv.text.toString(),
                permanentPincode = bottomSheetBackPhotoAadharBackPhotoBinding.prmntPincodeEd.text.toString(),
                permanentState = bottomSheetBackPhotoAadharBackPhotoBinding.prmntStateEd.text.toString(),
                currentAddress = bottomSheetBackPhotoAadharBackPhotoBinding.enterAdressEd.text.toString(),
                currentState = bottomSheetBackPhotoAadharBackPhotoBinding.stateEd.text.toString(),
                currentPincode = bottomSheetBackPhotoAadharBackPhotoBinding.pincodeEd.text.toString(),
                isAddress = bottomSheetBackPhotoAadharBackPhotoBinding.checkBox.isChecked
            )
        }
        if (bottomSheetBackPhotoAadharBackPhotoBinding.properAddressTv.text.toString()
                .isNotBlank() && bottomSheetBackPhotoAadharBackPhotoBinding.prmntStateEd.text.toString()
                .isNotBlank() && bottomSheetBackPhotoAadharBackPhotoBinding.prmntPincodeEd.text.toString().length == 6 && bottomSheetBackPhotoAadharBackPhotoBinding.enterAdressEd.text.toString()
                .isNotBlank() && bottomSheetBackPhotoAadharBackPhotoBinding.stateEd.text.toString()
                .isNotBlank() && bottomSheetBackPhotoAadharBackPhotoBinding.pincodeEd.text.toString().length == 6
        ) {
            bottomSheetBackPhotoAadharBackPhotoBinding.continueAppCompatButton1.isEnabled = true
            bottomSheetBackPhotoAadharBackPhotoBinding.continueAppCompatButton1.setBackgroundResource(
                (R.drawable.button)
            )
            bottomSheetBackPhotoAadharBackPhotoBinding.continueAppCompatButton1.setTextColor(
                ContextCompat.getColor(
                    applicationContext, R.color.white
                )
            )
        }

        bottomSheetBackPhotoAadharBackPhotoBinding.properAddressTv.addTextChangedListener {
            if (bottomSheetBackPhotoAadharBackPhotoBinding.checkBox.isChecked) {
                bottomSheetBackPhotoAadharBackPhotoBinding.checkBox.isChecked = false
                manageAadhaarCheckbox(false)
            }
        }
        bottomSheetBackPhotoAadharBackPhotoBinding.prmntStateEd.addTextChangedListener {
            if (bottomSheetBackPhotoAadharBackPhotoBinding.checkBox.isChecked) {
                bottomSheetBackPhotoAadharBackPhotoBinding.checkBox.isChecked = false
                manageAadhaarCheckbox(false)
            }
        }
        bottomSheetBackPhotoAadharBackPhotoBinding.prmntPincodeEd.addTextChangedListener {
            if (bottomSheetBackPhotoAadharBackPhotoBinding.checkBox.isChecked) {
                bottomSheetBackPhotoAadharBackPhotoBinding.checkBox.isChecked = false
                manageAadhaarCheckbox(false)
            }
        }
        aadhaarBackBottomSheetDialog.show()

    }

    fun manageAadhaarCheckbox(checked: Boolean) {
        if (checked) {
            bottomSheetBackPhotoAadharBackPhotoBinding.enterAdressEd.setText(
                bottomSheetBackPhotoAadharBackPhotoBinding.properAddressTv.text.toString()
            )

            bottomSheetBackPhotoAadharBackPhotoBinding.stateEd.setText(
                bottomSheetBackPhotoAadharBackPhotoBinding.prmntStateEd.text.toString()
            )

            bottomSheetBackPhotoAadharBackPhotoBinding.pincodeEd.setText(
                bottomSheetBackPhotoAadharBackPhotoBinding.prmntPincodeEd.text.toString()
            )
            bottomSheetBackPhotoAadharBackPhotoBinding.enterAdressEd.isFocusable = false
            bottomSheetBackPhotoAadharBackPhotoBinding.stateEd.isFocusable = false
            bottomSheetBackPhotoAadharBackPhotoBinding.pincodeEd.isFocusable = false
        } else {
            bottomSheetBackPhotoAadharBackPhotoBinding.enterAdressEd.isFocusableInTouchMode = true
            bottomSheetBackPhotoAadharBackPhotoBinding.stateEd.isFocusableInTouchMode = true
            bottomSheetBackPhotoAadharBackPhotoBinding.pincodeEd.isFocusableInTouchMode = true
            bottomSheetBackPhotoAadharBackPhotoBinding.enterAdressEd.setText("")
            bottomSheetBackPhotoAadharBackPhotoBinding.stateEd.setText("")
            bottomSheetBackPhotoAadharBackPhotoBinding.pincodeEd.setText("")
        }
    }

    private fun openFrontAdhaarBottomSheet(bitmap: Bitmap, res: CommonResponse) {
        aadhaarFrontBottomSheetDialog = BottomSheetDialog(this, R.style.otpsheetDialogTheme)
        bottomSheetFrontPhotoAadhaarBinding =
            BottomsheetFrontPhotoAadhaarBinding.inflate(layoutInflater)
        bottomSheetFrontPhotoAadhaarBinding.edFullname.addTextChangedListener(this)
        bottomSheetFrontPhotoAadhaarBinding.number.addTextChangedListener(this)
        aadhaarFrontBottomSheetDialog.setCancelable(true)
        aadhaarFrontBottomSheetDialog.setContentView(bottomSheetFrontPhotoAadhaarBinding.root)
        aadhaarFrontBottomSheetDialog.show()
        currentBottomSheet = bottomSheetFrontPhotoAadhaarBinding
        bottomSheetFrontPhotoAadhaarBinding.clAadhaarForm.visibility = View.VISIBLE
        bottomSheetFrontPhotoAadhaarBinding.btnMale.setOnClickListener {
            selectedGender = "male"
            selectGender(bottomSheetFrontPhotoAadhaarBinding.btnMale)
            unselectGender(bottomSheetFrontPhotoAadhaarBinding.btnFemale)
            unselectGender(bottomSheetFrontPhotoAadhaarBinding.btnTransgender)
            manageAadhaarFrontButtonState()
        }
        bottomSheetFrontPhotoAadhaarBinding.btnFemale.setOnClickListener {
            selectedGender = "female"
            selectGender(bottomSheetFrontPhotoAadhaarBinding.btnFemale)
            unselectGender(bottomSheetFrontPhotoAadhaarBinding.btnMale)
            unselectGender(bottomSheetFrontPhotoAadhaarBinding.btnTransgender)
            manageAadhaarFrontButtonState()
        }
        bottomSheetFrontPhotoAadhaarBinding.btnTransgender.setOnClickListener {
            selectedGender = "transgender"
            selectGender(bottomSheetFrontPhotoAadhaarBinding.btnTransgender)
            unselectGender(bottomSheetFrontPhotoAadhaarBinding.btnFemale)
            unselectGender(bottomSheetFrontPhotoAadhaarBinding.btnMale)
            manageAadhaarFrontButtonState()
        }
        val aadhaarNumber = res.data!!.doc_number?.replace("\\s".toRegex(), "") ?: ""
        if (!aadhaarNumber.isNullOrBlank()) {
            bottomSheetFrontPhotoAadhaarBinding.number.setText(aadhaarNumber)
        }
        if (!res.data.doc_name.isNullOrBlank()) {
            bottomSheetFrontPhotoAadhaarBinding.edFullname.setText(res.data.doc_name)
        }
        bottomSheetFrontPhotoAadhaarBinding.edDob.text = res.data.DOB

        if (!res.data.gender.isNullOrEmpty()) {
            if (res.data.gender.equals("MALE", ignoreCase = true)) {
                selectedGender = "male"
                bottomSheetFrontPhotoAadhaarBinding.btnMale.callOnClick()
            } else if (res.data.gender.equals("FEMALE", ignoreCase = true)) {
                selectedGender = "female"
                bottomSheetFrontPhotoAadhaarBinding.btnFemale.callOnClick()
            } else if (res.data.gender.equals("TRANSGENDER", ignoreCase = true)) {
                selectedGender = "female"
                bottomSheetFrontPhotoAadhaarBinding.btnTransgender.callOnClick()
            }
        }

        bottomSheetFrontPhotoAadhaarBinding.edDob.setOnClickListener {
            openDatePicker(true)
        }

        bottomSheetFrontPhotoAadhaarBinding.ivAadharcard.setImageBitmap(bitmap)
        bottomSheetFrontPhotoAadhaarBinding.imageClick.setOnClickListener {
            CAME_FROM = UPLOAD_FRONT_ADHAR_CLICK
            aadhaarFrontBottomSheetDialog.dismiss()
            openCamera(false)
        }



        bottomSheetFrontPhotoAadhaarBinding.continueAppCompatButton.setOnClickListener {
            val customData = mapOf("Screen" to "UploadDocumentActivity", "key1" to 27)
            logButtonClick("continueAppCompatButton", customData)
            if (bottomSheetFrontPhotoAadhaarBinding.number.text.toString().isEmpty()) setError(
                bottomSheetFrontPhotoAadhaarBinding.textInputNumer,
                getString(R.string.enter_aadhaar_number)
            )
            else if (bottomSheetFrontPhotoAadhaarBinding.edFullname.text.toString().isEmpty()) {
                setError(
                    bottomSheetFrontPhotoAadhaarBinding.textInputFullname,
                    getString(R.string.enter_full_name_as_per_aadhaar)
                )
            } else if (bottomSheetFrontPhotoAadhaarBinding.edDob.text.toString().isEmpty()) {
                showToast(getString(R.string.enter_date_of_birth), true)
            } else {
                cameFrom = FRONTAADHAAR
                mViewModel!!.updateManualDocService(
                    docType = FRONTAADHAAR,
                    gender = selectedGender,
                    userName = bottomSheetFrontPhotoAadhaarBinding.edFullname.text.toString(),
                    dob = bottomSheetFrontPhotoAadhaarBinding.edDob.text.toString(),
                    docNum = bottomSheetFrontPhotoAadhaarBinding.number.text.toString(),
                )
            }
        }
        manageAadhaarFrontButtonState()
    }

    private fun manageAadhaarFrontButtonState() {
        if (bottomSheetFrontPhotoAadhaarBinding.number.text.toString().length == 12 && bottomSheetFrontPhotoAadhaarBinding.edFullname.text.toString()
                .isNotEmpty() && bottomSheetFrontPhotoAadhaarBinding.edDob.text.toString()
                .isNotBlank() && selectedGender != ""
        ) {
            bottomSheetFrontPhotoAadhaarBinding.continueAppCompatButton.isEnabled = true
            bottomSheetFrontPhotoAadhaarBinding.continueAppCompatButton.setBackgroundResource((R.drawable.button))
            bottomSheetFrontPhotoAadhaarBinding.continueAppCompatButton.setTextColor(
                ContextCompat.getColor(
                    applicationContext, R.color.white
                )
            )
        } else {
            bottomSheetFrontPhotoAadhaarBinding.continueAppCompatButton.isEnabled = false
            bottomSheetFrontPhotoAadhaarBinding.continueAppCompatButton.setBackgroundResource((R.drawable.disable_button))
            bottomSheetFrontPhotoAadhaarBinding.continueAppCompatButton.setTextColor(
                ContextCompat.getColor(
                    applicationContext, R.color.grey_A3
                )
            )
        }
    }

    fun selectGender(selectBtn: AppCompatButton) {
        selectBtn.setBackgroundResource(R.drawable.blue_bg_button)
        selectBtn.setTextColor(ContextCompat.getColor(applicationContext, R.color.white))
    }

    fun unselectGender(unSelectBtn: AppCompatButton) {
        unSelectBtn.setBackgroundResource(R.drawable.bg_white)
        unSelectBtn.setTextColor(ContextCompat.getColor(applicationContext, R.color.blue))
    }

    private fun openBottomSheetbank() {
        bankBottomSheetDialog = BottomSheetDialog(this, R.style.otpsheetDialogTheme)
        bottomSheetBankDetailsLayoutBinding =
            BottomsheetBankDetailsLayoutBinding.inflate(layoutInflater)
        bottomSheetBankDetailsLayoutBinding.edAccountNo.addTextChangedListener(this)
        bottomSheetBankDetailsLayoutBinding.edIfsc.addTextChangedListener(this)
        bottomSheetBankDetailsLayoutBinding.edEnterName.addTextChangedListener(this)
        bankBottomSheetDialog.setCancelable(true)
        bankBottomSheetDialog.setContentView(bottomSheetBankDetailsLayoutBinding.root)
        bankBottomSheetDialog.show()
        currentBottomSheet = bottomSheetBankDetailsLayoutBinding
        if (isChequeUploadSuceessful) {
            bottomSheetBankDetailsLayoutBinding.verifiedName.visibility = View.GONE
            if (!doc_file.img_cheque.isNullOrEmpty()) {
                uploadImages(
                    "${BASE_URL + doc_file.img_cheque}&auth=$authToken",
                    bottomSheetBankDetailsLayoutBinding.ivCancelCheck,
                    R.drawable.cheque_img
                )
            }
            //bottomSheetBankDetailsLayoutBinding.tilEnterName.visibility = View.VISIBLE
            bottomSheetBankDetailsLayoutBinding.enterNameTv.visibility = View.GONE
            bottomSheetBankDetailsLayoutBinding.cvCancelCheck.visibility = View.VISIBLE
        }
        bankChequeBitmap?.let {
            bottomSheetBankDetailsLayoutBinding.ivCancelCheck.setImageBitmap(it)
        }
        if (is_bank_verified) {
            if (!isChequeUploadSuceessful) {
                bottomSheetBankDetailsLayoutBinding.verifiedName.visibility = View.VISIBLE
                bottomSheetBankDetailsLayoutBinding.tvHolderName.text = doc_file.account_holder_name
            }
        } else {
            bottomSheetBankDetailsLayoutBinding.verifiedName.visibility = View.INVISIBLE
        }
        if (!doc_file.accountno.isNullOrEmpty()) {
            bottomSheetBankDetailsLayoutBinding.edAccountNo.setText(doc_file.accountno)

        }
        if (!doc_file.ifsccode.isNullOrBlank()) {
            bottomSheetBankDetailsLayoutBinding.edIfsc.setText(doc_file.ifsccode)
            mViewModel!!.checkIfscCode(doc_file.ifsccode!!)
        }
        if (!doc_file.account_holder_name.isNullOrEmpty()) {
            bottomSheetBankDetailsLayoutBinding.edEnterName.setText(doc_file.account_holder_name)
        }
        bottomSheetBankDetailsLayoutBinding.ivCancelCheck.setOnClickListener {
            CAME_FROM = UPLOAD_BANK
            openBottomCameraOptions()
        }
        bottomSheetBankDetailsLayoutBinding.tvFindYourIfsc.setOnClickListener {}
        bottomSheetBankDetailsLayoutBinding.edIfsc.addTextChangedListener {
            if (isValidIFSCCode(bottomSheetBankDetailsLayoutBinding.edIfsc.text.toString())) {
                mViewModel!!.checkIfscCode(bottomSheetBankDetailsLayoutBinding.edIfsc.text.toString())
            }
        }
        bottomSheetBankDetailsLayoutBinding.continueAppCompatButton1.setOnClickListener {
            doc_file.accountno = bottomSheetBankDetailsLayoutBinding.edAccountNo.text.toString()
            doc_file.ifsccode = bottomSheetBankDetailsLayoutBinding.edIfsc.text.toString()
            if (bottomSheetBankDetailsLayoutBinding.edAccountNo.text.toString().isEmpty()) {
                setError(
                    bottomSheetBankDetailsLayoutBinding.txtInputAccount,
                    getString(R.string.enter_account_number)
                )
            } else if (bottomSheetBankDetailsLayoutBinding.edIfsc.text.toString().isEmpty()) {
                setError(
                    bottomSheetBankDetailsLayoutBinding.txtInputIfsc,
                    getString(R.string.enter_ifsc_code)
                )
            } else {
                if (bottomSheetBankDetailsLayoutBinding.continueAppCompatButton1.text.toString() == getString(
                        R.string.confirm
                    )
                ) {
                    mViewModel!!.updateDocumentStatus(
                        PreferenceKey.BANK_STATUS.name, 1
                    )
                    is_bank_verified = true
                    bankBottomSheetDialog.dismiss()
                    mBinding.bankStatus.text = getString(R.string.verified)
                    mBinding.bankStatus.setTextColor(
                        ContextCompat.getColor(
                            this@UploadDocumentsActivity, R.color.green_92
                        )
                    )
                    mBinding.bankStatus.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_verified, 0, 0, 0
                    )

                    manageSubmitButton()
                } else {
                    if (isChequeUploadSuceessful) {
                        mViewModel!!.updateDocumentStatus(
                            PreferenceKey.BANK_STATUS.name, 2
                        )
                        is_bank_verified = true
                        bankBottomSheetDialog.dismiss()
                        mBinding.bankStatus.text = getString(R.string.cheque_uploaded)
                        mBinding.bankStatus.setTextColor(
                            ContextCompat.getColor(
                                this@UploadDocumentsActivity, R.color.green_92
                            )
                        )
                        mBinding.bankStatus.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_verified, 0, 0, 0
                        )
                        manageSubmitButton()
                    } else {
                        bottomSheetBankDetailsLayoutBinding.edIfsc.clearFocus()
                        cameFrom = BANK
                        mViewModel!!.updateManualDocService(
                            docType = BANK,
                            docNum = bottomSheetBankDetailsLayoutBinding.edAccountNo.text.toString(),
                            ifsc = bottomSheetBankDetailsLayoutBinding.edIfsc.text.toString(),
                            bankName = bank_name
                        )
                    }
                }
            }
        }
    }

    private fun openCamera(openfrontcamera: Boolean) {
        GET_FROM_CAMERA = true
        val intent = Intent(this, CameraxActivity::class.java)
        intent.putExtra(ONLY_FRONT_ENABLE, openfrontcamera)
        activityResultLauncher.launch(intent)
    }

    private fun imageChooser() {
        GET_FROM_CAMERA = false
        val i = Intent()
        i.type = "image/*"
        i.action = Intent.ACTION_GET_CONTENT
        activityResultLauncher.launch(i)
    }

    var activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            when (result.resultCode) {
                RESULT_OK -> {
                    val bitmap: Bitmap?
                    val files: File?

                    if (GET_FROM_CAMERA) {
                        files = result.data?.extras?.getSerializable("data") as File?
                        if (files != null) {
                            val imageString: String? = CommonUtils.compressImages(files, this)
                            bitmap = BitmapFactory.decodeFile(imageString)
                            uploadSelectedImage(bitmap, files, result)
                        }
                    } else {
                        val data = result.data
                        if (data != null && data.data != null) {
                            val selectedImageUri: Uri? = data.data
                            selectedImageUri?.let { uri ->
                                imageCropperLauncher.launch(CropImage.activity(uri).getIntent(this))
                            }
                        }
                    }
                }

                Activity.RESULT_CANCELED -> {}
            }
        }

    private fun uploadSelectedImage(bitmap: Bitmap?, files: File?, result: ActivityResult?) {
        when (CAME_FROM) {
            UPLOAD_PROFILE, CAME_FROM_RETAKE -> {
                if (files != null) {
                    checkImageAfterClick(files)
                }
            }

            CONFIRM_UPLOAD_PROFILE -> {
                CAME_FROM = if (result!!.data?.extras?.getBoolean("retake") != true) {
                    Log.e("image-> ", " " + getBase64String(bitmap!!))
                    mViewModel!!.setProfilePic()
                    mBinding.profilePicture.setImageBitmap(bitmap)
                    isProfileUploaded = true
                    manageSubmitButton()
                    mBinding.profilePicture.scaleX = -1f
                    ""
                } else {
                    openCamera(true)
                    CAME_FROM_RETAKE
                }
            }

            UPLOAD_FRONT_ADHAR_CLICK -> {
                frontAdharBitmap = null
                if (bitmap != null) {
                    frontAdharBitmap = bitmap
                    docUploadFrom = FRONT_DOC_UPLOAD

                    mViewModel!!.uploadDoc(bitmap, FRONTAADHAAR)
                }
                CAME_FROM = ""
            }

            UPLOAD_BACK_ADHAR -> {
                backAdharBitmap = null
                if (bitmap != null) {
                    backAdharBitmap = bitmap
                    docUploadFrom = BACK_DOC_UPLOAD
                    mViewModel!!.uploadDoc(bitmap, BACKAADHAAR)
                }
                CAME_FROM = ""
            }

            UPLOAD_BANK -> {
                Log.e("image-> ", " " + getBase64String(bitmap!!))
                bankChequeBitmap = null
                if (bitmap != null) {
                    bankChequeBitmap = bitmap
                    docUploadFrom = CHEQUE_DOC_UPLOAD
                    mViewModel!!.uploadDoc(bitmap, BANK)
                }
                CAME_FROM = ""
            }
        }
    }

    var imageCropperLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { cropResult ->
            var bitmap: Bitmap? = null
            if (cropResult.resultCode == RESULT_OK) {
                val resultUri = CropImage.getActivityResult(cropResult.data!!).uri!!
                try {
                    bitmap = if (Build.VERSION.SDK_INT < 28) {
                        MediaStore.Images.Media.getBitmap(
                            contentResolver, resultUri
                        )
                    } else {
                        val source: ImageDecoder.Source = ImageDecoder.createSource(
                            contentResolver, resultUri
                        )
                        ImageDecoder.decodeBitmap(source)
                    }
                    uploadSelectedImage(bitmap, null, null)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }

    fun checkImageAfterClick(files: File) {
        CAME_FROM = CONFIRM_UPLOAD_PROFILE
        val intent = Intent(this, SelfieActivity::class.java)
        intent.putExtra("file", files)
        activityResultLauncher.launch(intent)
    }

    private fun dialogueForNotLinking(
    ) {
        val alertDialog = Dialog(this)
        val dialogNotLinkingBinding = DialougeNotLinkingBinding.inflate(layoutInflater)
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
        dialogNotLinkingBinding.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                dialogNotLinkingBinding.btnConfirm.isEnabled = true
                dialogNotLinkingBinding.btnConfirm.setBackgroundResource(R.drawable.button)
                dialogNotLinkingBinding.btnConfirm.setTextColor(
                    ContextCompat.getColor(
                        this@UploadDocumentsActivity, R.color.white
                    )
                )
            } else {
                dialogNotLinkingBinding.btnConfirm.isEnabled = false
                dialogNotLinkingBinding.btnConfirm.setBackgroundResource(R.drawable.disable_button)
                dialogNotLinkingBinding.btnConfirm.setTextColor(
                    ContextCompat.getColor(this@UploadDocumentsActivity, R.color.grey_A3)
                )
            }
        }
        dialogNotLinkingBinding.ivCancel.setOnClickListener {
            alertDialog.dismiss()
        }
        dialogNotLinkingBinding.btnConfirm.setOnClickListener {
            val customData = mapOf("NotLinkedAddharPan" to "UploadDocumentActivity", "key1" to 28)
            logButtonClick("btn_confirm", customData)
            alertDialog.dismiss()
        }
    }

    private fun dialogueForImgNotValid(
        description: String,
        openbottomSheet: Boolean,
        aadharBitmap: Bitmap?,
        came_from: String,
        res: CommonResponse
    ) {
        val alertDialog = Dialog(this)
        val dialogueImageNotValid = DialogueImageNotValidBinding.inflate(layoutInflater)
        alertDialog.setContentView(dialogueImageNotValid.root)
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
        if (this::aadhaarBackBottomSheetDialog.isInitialized && aadhaarBackBottomSheetDialog.isShowing) {
            aadhaarBackBottomSheetDialog.dismiss()
        }
        if (this::aadhaarFrontBottomSheetDialog.isInitialized && aadhaarFrontBottomSheetDialog.isShowing) {
            aadhaarFrontBottomSheetDialog.dismiss()
        }
        dialogueImageNotValid.tvUplaodvalid.text = description
        if (openbottomSheet) {
            dialogueImageNotValid.btnExit.text = getString(R.string.ok)
        }

        dialogueImageNotValid.btnExit.setOnClickListener {
            val customData = mapOf("Screen" to "UploadDocumentActivity", "key1" to 28)
            logButtonClick("btnExit", customData)
            if (openbottomSheet) {
                alertDialog.dismiss()
                if (came_from == "front") {
                    aadharBitmap?.let { it1 ->
                        openFrontAdhaarBottomSheet(it1, res)
                        frontAdharBitmap = aadharBitmap
                    }
                } else {
                    aadharBitmap?.let { it1 ->
                        openBackAadharBottomsheet(it1, res)
                        backAdharBitmap = aadharBitmap
                    }
                }
            } else {
                alertDialog.dismiss()
            }
        }
        dialogueImageNotValid.imvClose.setOnClickListener {
            val customData = mapOf("Screen" to "UploadDocumentActivity", "key1" to 29)
            logButtonClick("imvClose", customData)
            alertDialog.dismiss()
        }
    }

    fun openReferenceDialog() {
        val alertDialog = Dialog(this)
        val referenceDialog = ReferenceDialogBinding.inflate(layoutInflater)
        alertDialog.setContentView(referenceDialog.root)
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
        referenceDialog.noBtn.setOnClickListener {
            alertDialog.dismiss()
            openReferralCodeBottomSheet()
            mViewModel?.setBooleanStatus(PreferenceKey.IS_REFERENCE)
        }
        referenceDialog.yesBtn.setOnClickListener {
            alertDialog.dismiss()
            launchNewActivity<ReferenceActivity>()
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (this::bottomSheetPanCardBinding.isInitialized) {
            if (bottomSheetPanCardBinding.edPanNo.text?.length == 10 && isValidPanCardNo(
                    bottomSheetPanCardBinding.edPanNo.text.toString()
                )
            ) {
                bottomSheetPanCardBinding.txtInputPan.isErrorEnabled = false
                bottomSheetPanCardBinding.btnContinue.isEnabled = true
                bottomSheetPanCardBinding.btnContinue.setBackgroundResource((R.drawable.button))
                bottomSheetPanCardBinding.btnContinue.setTextColor(
                    ContextCompat.getColor(
                        this@UploadDocumentsActivity, R.color.white
                    )
                )
                CommonUtils.hideKeyboardForDialog(this, bottomSheetPanCardBinding.edPanNo)
            } else {
                bottomSheetPanCardBinding.btnContinue.isEnabled = false
                bottomSheetPanCardBinding.btnContinue.setBackgroundResource((R.drawable.disable_button))
                bottomSheetPanCardBinding.btnContinue.setTextColor(
                    ContextCompat.getColor(this@UploadDocumentsActivity, R.color.grey_A3)
                )
                bottomSheetPanCardBinding.txtInputPan.error =
                    getString(R.string.enter_valid_pan_card)
            }
        }
        if (this::bottomSheetFrontPhotoAadhaarBinding.isInitialized) {
            if (bottomSheetFrontPhotoAadhaarBinding.number.text.toString().length == 12) {
                bottomSheetFrontPhotoAadhaarBinding.textInputNumer.isErrorEnabled = false


            } else {
                bottomSheetFrontPhotoAadhaarBinding.textInputNumer.isErrorEnabled = true
                setError(
                    bottomSheetFrontPhotoAadhaarBinding.textInputNumer,
                    getString(R.string.enter_valid_adhar_number)
                )
            }

            if (bottomSheetFrontPhotoAadhaarBinding.edFullname.text.toString().isNotEmpty()) {
                bottomSheetFrontPhotoAadhaarBinding.textInputFullname.isErrorEnabled = false
            } else {
                bottomSheetFrontPhotoAadhaarBinding.textInputFullname.isErrorEnabled = true
                setError(
                    bottomSheetFrontPhotoAadhaarBinding.textInputFullname,
                    getString(R.string.enter_name)
                )
            }
            manageAadhaarFrontButtonState()
        }
        if (this::bottomSheetBankDetailsLayoutBinding.isInitialized) {
            if (isValidIFSCCode(bottomSheetBankDetailsLayoutBinding.edIfsc.text.toString())

            ) {

                bottomSheetBankDetailsLayoutBinding.txtInputIfsc.isErrorEnabled = false
            } else {
                bottomSheetBankDetailsLayoutBinding.txtInputIfsc.isErrorEnabled = true
                setError(
                    bottomSheetBankDetailsLayoutBinding.txtInputIfsc,
                    getString(R.string.enter_valid_ifsc_code)
                )
                bottomSheetBankDetailsLayoutBinding.ifscErrorTxt.visibility = View.GONE
                bottomSheetBankDetailsLayoutBinding.ifscSuccess.visibility = View.GONE

            }
            //bottomSheetBankDetailsLayoutBinding.tilEnterName.error = ""
            if (isValid_Bank_Acc_Number(bottomSheetBankDetailsLayoutBinding.edAccountNo.text.toString())) {
                bottomSheetBankDetailsLayoutBinding.txtInputAccount.isErrorEnabled = false
            } else {
                bottomSheetBankDetailsLayoutBinding.txtInputAccount.isErrorEnabled = true
                setError(
                    bottomSheetBankDetailsLayoutBinding.txtInputAccount,
                    getString(R.string.enter_valid_account_no)
                )
            }
            manageBankButton()
        }
        //Driving License
        if (this::bottomSheetDrivingLicenseBinding.isInitialized) {
            val text = bottomSheetDrivingLicenseBinding.drivingLicenceEd.text
            if (text!!.isNotEmpty() && text.length >= 15) {
                CommonUtils.hideKeyboardForDialog(
                    this, bottomSheetDrivingLicenseBinding.drivingLicenceEd
                )
                if (bottomSheetDrivingLicenseBinding.drivingLicenceDateEd.text!!.isNotEmpty()) {
                    bottomSheetDrivingLicenseBinding.continueAppCompatButton.isEnabled = true
                    bottomSheetDrivingLicenseBinding.continueAppCompatButton.setBackgroundResource((R.drawable.button))
                    bottomSheetDrivingLicenseBinding.continueAppCompatButton.setTextColor(
                        ContextCompat.getColor(
                            applicationContext, R.color.white
                        )
                    )
                }
            } else {
                bottomSheetDrivingLicenseBinding.continueAppCompatButton.isEnabled = false
                bottomSheetDrivingLicenseBinding.continueAppCompatButton.setBackgroundResource((R.drawable.disable_button))
                bottomSheetDrivingLicenseBinding.continueAppCompatButton.setTextColor(
                    ContextCompat.getColor(
                        applicationContext, R.color.grey_A3
                    )
                )
            }
        }
        if (this::bottomSheetBackPhotoAadharBackPhotoBinding.isInitialized) {
            if (bottomSheetBackPhotoAadharBackPhotoBinding.properAddressTv.text.toString()
                    .isNotBlank() && bottomSheetBackPhotoAadharBackPhotoBinding.prmntStateEd.text.toString()
                    .isNotBlank() && bottomSheetBackPhotoAadharBackPhotoBinding.prmntPincodeEd.text.toString().length == 6 && bottomSheetBackPhotoAadharBackPhotoBinding.enterAdressEd.text.toString()
                    .isNotBlank() && bottomSheetBackPhotoAadharBackPhotoBinding.stateEd.text.toString()
                    .isNotBlank() && bottomSheetBackPhotoAadharBackPhotoBinding.pincodeEd.text.toString().length == 6
            ) {
                bottomSheetBackPhotoAadharBackPhotoBinding.continueAppCompatButton1.isEnabled = true
                bottomSheetBackPhotoAadharBackPhotoBinding.continueAppCompatButton1.setBackgroundResource(
                    (R.drawable.button)
                )
                bottomSheetBackPhotoAadharBackPhotoBinding.continueAppCompatButton1.setTextColor(
                    ContextCompat.getColor(
                        applicationContext, R.color.white
                    )
                )
            } else {
                bottomSheetBackPhotoAadharBackPhotoBinding.continueAppCompatButton1.isEnabled =
                    false
                bottomSheetBackPhotoAadharBackPhotoBinding.continueAppCompatButton1.setBackgroundResource(
                    (R.drawable.disable_button)
                )
                bottomSheetBackPhotoAadharBackPhotoBinding.continueAppCompatButton1.setTextColor(
                    ContextCompat.getColor(
                        applicationContext, R.color.grey_A3
                    )
                )
            }
        }
    }

    override fun afterTextChanged(s: Editable?) {}

    fun manageSubmitButton() {
        if (isProfileUploaded && is_adhar_verified && is_pan_verified && is_bank_verified && is_dl_verified) {
            mBinding.submitAppCompatButton.isEnabled = true
            mViewModel!!.setReapplybtn()
            mBinding.submitAppCompatButton.setBackgroundResource(R.drawable.button)
            mBinding.submitAppCompatButton.setTextColor(
                ContextCompat.getColor(
                    applicationContext, R.color.white
                )
            )
            setNavigator(
                mBinding.commonBg.imageView4,
                mBinding.commonBg.imageView2,
                mBinding.commonBg.view1,
                mBinding.commonBg.view3,
                mBinding.commonBg.view2,
                mBinding.commonBg.tvLocation,
                mBinding.commonBg.tvRegister,
                mBinding.commonBg.tvVerification
            )
        } else {
            mBinding.submitAppCompatButton.isEnabled = false
            mBinding.submitAppCompatButton.setBackgroundResource(R.drawable.disable_button)
            mBinding.submitAppCompatButton.setTextColor(
                ContextCompat.getColor(
                    applicationContext, R.color.grey_A3
                )
            )
        }
    }

    fun manageDocumentDetailStatus() {
        val allDocStatus = mViewModel!!.getDocumentStatus(
            PreferenceKey.ALL_DOCUMENTS_STATUS.name
        )
        val bankStatus = mViewModel!!.getDocumentStatus(
            PreferenceKey.BANK_STATUS.name
        )
        val dlStatus = mViewModel!!.getDocumentStatus(
            PreferenceKey.DRIVING_STATUS.name
        )
        val frontAadhaarStatus = mViewModel!!.getBooleanStatus(
            PreferenceKey.FRONT_AADHAAR_STATUS
        )
        val backAadhaarStatus = mViewModel!!.getBooleanStatus(
            PreferenceKey.BACK_AADHAAR_STATUS
        )
        val aadhaarStatus = mViewModel!!.getDocumentStatus(
            PreferenceKey.AADHAAR_STATUS.name
        )
        val panStatus = mViewModel!!.getDocumentStatus(
            PreferenceKey.PAN_STATUS.name
        )
        val profileStatus = mViewModel!!.getDocumentStatus(
            PreferenceKey.IS_PROFILE_PIC.name
        )
        isFrontImageUplaod = frontAadhaarStatus
        isBackImageUpload = backAadhaarStatus
        is_adhar_verified = frontAadhaarStatus && backAadhaarStatus
        if ((aadhaarStatus == 1 && is_adhar_verified)|| aadhaarStatus == 3) {

            mBinding.tvPending.text = getString(R.string.verified)
            mBinding.tvPending.setTextColor(
                ContextCompat.getColor(
                    this@UploadDocumentsActivity, R.color.green_92
                )
            )
            mBinding.tvPending.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_verified, 0, 0, 0
            )
            if (isReferral && aadhaarStatus == 3) {
                mBinding.cvAadharcard.visibility = View.GONE
            }
        }
        if (aadhaarStatus == 4) {
            isFrontImageUplaod = false
            isBackImageUpload = false
            is_adhar_verified = false
            mBinding.tvPending.text = getString(R.string.rejected)
            mBinding.tvPending.setTextColor(
                ContextCompat.getColor(
                    this@UploadDocumentsActivity, R.color.red_4B
                )
            )
            mBinding.tvPending.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.reject_status, 0, 0, 0
            )
        }
        if (panStatus == 1 || panStatus == 3) {
            is_pan_verified = true
            if (panStatus == 1 || panStatus == 3) {
                doc_file.is_pan = true
                mBinding.pancardStatus.text = getString(R.string.verified)
            }
            mBinding.pancardStatus.setTextColor(
                ContextCompat.getColor(
                    this@UploadDocumentsActivity, R.color.green_92
                )
            )
            mBinding.pancardStatus.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_verified, 0, 0, 0
            )
            if (isReferral && panStatus == 3) {
                mBinding.cvPancard.visibility = View.GONE
            }
        }
        if (panStatus == 4) {
            is_pan_verified = false
            mBinding.pancardStatus.text = getString(R.string.rejected)
            mBinding.pancardStatus.setTextColor(
                ContextCompat.getColor(
                    this@UploadDocumentsActivity, R.color.red_4B
                )
            )
            mBinding.pancardStatus.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.reject_status, 0, 0, 0
            )
        }
        if (bankStatus == 1 || bankStatus == 2 || bankStatus == 3) {
            is_bank_verified = true
            mBinding.bankStatus.text = getString(R.string.verified)
            mBinding.bankStatus.setTextColor(
                ContextCompat.getColor(
                    this@UploadDocumentsActivity, R.color.green_92
                )
            )
            mBinding.bankStatus.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_verified, 0, 0, 0
            )
            if (bankStatus == 2) {
                isChequeUploadSuceessful = true
                mBinding.bankStatus.text = getString(R.string.cheque_uploaded)
            }
            if (isReferral && bankStatus == 3) {
                mBinding.cvBankaccount.visibility = View.GONE
            }
        }
        if (bankStatus == 4) {
            is_bank_verified = false
            mBinding.bankStatus.text = getString(R.string.rejected)
            mBinding.bankStatus.setTextColor(
                ContextCompat.getColor(
                    this@UploadDocumentsActivity, R.color.red_4B
                )
            )
            mBinding.bankStatus.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.reject_status, 0, 0, 0
            )
        }
        if (dlStatus == 1 || dlStatus == 3) {
            is_dl_verified = true
            mBinding.dlStatus.text = getString(R.string.available)

            mBinding.dlStatus.setTextColor(
                ContextCompat.getColor(
                    this@UploadDocumentsActivity, R.color.green_92
                )
            )
            mBinding.dlStatus.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_verified, 0, 0, 0
            )
            if (isReferral && dlStatus == 3) {
                mBinding.cvDrivingLicense.visibility = View.GONE
            }
        }
        if (dlStatus == 4) {
            is_dl_verified = false
            mBinding.dlStatus.text = getString(R.string.rejected)
            mBinding.dlStatus.setTextColor(
                ContextCompat.getColor(
                    this@UploadDocumentsActivity, R.color.red_4B
                )
            )
            mBinding.dlStatus.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.reject_status, 0, 0, 0
            )
        }
        if (profileStatus == 1) {
            isProfileUploaded = true
        }
        if (allDocStatus == 1) {
            manageSubmitButton()
        }
    }

    private fun uploadDocImages(token: String) {
        if (!doc_file.img_profile.isNullOrEmpty()) {
            isProfileUploaded = true
            uploadImages(
                "${BASE_URL + doc_file.img_profile}&auth=${token}",
                mBinding.profilePicture,
                R.drawable.ic_profile
            )
            mBinding.profilePicture.scaleX = 1f
        }

        if (!doc_file.img_aadhar_first.isNullOrEmpty() && isFrontImageUplaod) {
            uploadImages(
                "${BASE_URL + doc_file.img_aadhar_first}&auth=${token}",
                mBinding.ivClickFront,
                R.drawable.ic_click
            )
            mBinding.ivClickFront.layoutParams.height = 200
            mBinding.ivClickFront.requestLayout()
            mBinding.ivClickFront.scaleType = ImageView.ScaleType.FIT_XY
        }

        if (!doc_file.img_aadhar_second.isNullOrEmpty() && isBackImageUpload) {
            uploadImages(
                "${BASE_URL + doc_file.img_aadhar_second}&auth=${token}",
                mBinding.aadharAppCompatImageView,
                R.drawable.ic_click
            )
            mBinding.aadharAppCompatImageView.layoutParams.height = 200
            mBinding.aadharAppCompatImageView.requestLayout()
            mBinding.aadharAppCompatImageView.scaleType = ImageView.ScaleType.FIT_XY
        }
    }

    fun uploadImages(imgUrl: String, imageHolder: ImageView, placeHolder: Int) {
        Glide.with(this).load(imgUrl).diskCacheStrategy(DiskCacheStrategy.NONE).error(placeHolder)
            .skipMemoryCache(true).placeholder(R.drawable.placeholder).into(imageHolder)
    }

    fun manageBankButton() {
        if (isValid_Bank_Acc_Number(bottomSheetBankDetailsLayoutBinding.edAccountNo.text.toString()) && isValidIFSCCode(
                bottomSheetBankDetailsLayoutBinding.edIfsc.text.toString()
            ) && isIfscValid
        ) {
            bottomSheetBankDetailsLayoutBinding.continueAppCompatButton1.isEnabled = true
            bottomSheetBankDetailsLayoutBinding.continueAppCompatButton1.setBackgroundResource(R.drawable.button)
            bottomSheetBankDetailsLayoutBinding.continueAppCompatButton1.setTextColor(
                ContextCompat.getColor(
                    applicationContext, R.color.white
                )
            )
        } else {
            bottomSheetBankDetailsLayoutBinding.continueAppCompatButton1.isEnabled = false
            bottomSheetBankDetailsLayoutBinding.continueAppCompatButton1.setBackgroundResource((R.drawable.disable_button))
            bottomSheetBankDetailsLayoutBinding.continueAppCompatButton1.setTextColor(
                ContextCompat.getColor(
                    applicationContext, R.color.grey_A3
                )
            )
        }
    }
}