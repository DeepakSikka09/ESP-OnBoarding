package com.ecomexpress.oneBoarding.ui.onBoard.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.ecomexpress.oneBoarding.R
import com.ecomexpress.oneBoarding.databinding.ActivitySelfieBinding
import com.ecomexpress.oneBoarding.ui.base.BaseActivity
import com.ecomexpress.oneBoarding.ui.onBoard.viewmodel.SelfieViewModel
import com.ecomexpress.oneBoarding.utils.comman.CommonUtils
import com.ecomexpress.oneBoarding.utils.comman.CommonUtils.flip
import com.ecomexpress.oneBoarding.utils.comman.CommonUtils.getBase64String
import com.ecomexpress.oneBoarding.utils.comman.CommonUtils.getCircularBitmap
import com.ecomexpress.oneBoarding.utils.comman.onError
import com.ecomexpress.oneBoarding.utils.comman.onLoading
import com.ecomexpress.oneBoarding.utils.comman.onSuccess
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class SelfieActivity : BaseActivity<SelfieViewModel, ActivitySelfieBinding>(),
    View.OnClickListener {

    override fun getLayout(): Int = R.layout.activity_selfie
    override fun getViewModelClass(): Class<SelfieViewModel> = SelfieViewModel::class.java
    lateinit var file: File

    lateinit var bitmap: Bitmap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.selfie = mViewModel
        mBinding.lifecycleOwner = this
        file = intent.getSerializableExtra("file") as File
        val imageString: String? = CommonUtils.compressImages(file, this)
        bitmap = BitmapFactory.decodeFile(imageString)
        bitmap = getCircularBitmap(bitmap)
        mBinding.btnConfirm.setOnClickListener(this)
        mBinding.btnRetake.setOnClickListener(this)
        mBinding.ivSelfie.setImageBitmap(bitmap)
        lifecycleScope.launch {
            mViewModel!!.uploadProfileImgFlow.collect {
                it.onLoading {
                    progressDialog(this@SelfieActivity).show()
                }
                it.onError {
                    showToast(it.message!!, false)
                    progressDialog(this@SelfieActivity).dismiss()
                }
                it.onSuccess {
                    if (it.success) {
                        val intent = Intent()
                        intent.putExtra("data", file)
                        intent.putExtra("retake", false)
                        showToast(it.description, true)
                        setResult(RESULT_OK, intent)
                        this@SelfieActivity.finish()
                    } else {
                        progressDialog(this@SelfieActivity).dismiss()
                        showToast(it.description, false)
                    }
                }
            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {
            mBinding.btnConfirm.id -> {
                val imgBase64 = getBase64String(flip(bitmap))
                mViewModel!!.uploadImage(imgBase64!!)
            }

            mBinding.btnRetake.id -> {
                val customData = mapOf("Screen" to "SelfieActivity", "key1" to 30)
                logButtonClick("btnRetake", customData)
                val intent = Intent()
                intent.putExtra("retake", true)
                intent.putExtra("data", file)
                setResult(RESULT_OK, intent)
                this@SelfieActivity.finish()
            }
        }
    }
}