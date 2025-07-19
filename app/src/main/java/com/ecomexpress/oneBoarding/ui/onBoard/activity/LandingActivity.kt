package com.ecomexpress.oneBoarding.ui.onBoard.activity

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.ecomexpress.oneBoarding.BuildConfig
import com.ecomexpress.oneBoarding.R
import com.ecomexpress.oneBoarding.data.model.CommonResponse
import com.ecomexpress.oneBoarding.data.model.onBoarding.BannerData
import com.ecomexpress.oneBoarding.databinding.ActivityLandingBinding
import com.ecomexpress.oneBoarding.ui.base.BaseActivity
import com.ecomexpress.oneBoarding.ui.onBoard.adapter.ViewPagerAdapter
import com.ecomexpress.oneBoarding.ui.onBoard.viewmodel.LandingViewModel
import com.ecomexpress.oneBoarding.utils.comman.AppConstants
import com.ecomexpress.oneBoarding.utils.comman.launchNewActivity
import com.ecomexpress.oneBoarding.utils.comman.onError
import com.ecomexpress.oneBoarding.utils.comman.onLoading
import com.ecomexpress.oneBoarding.utils.comman.onSuccess
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

@AndroidEntryPoint
class LandingActivity : BaseActivity<LandingViewModel, ActivityLandingBinding>(),
    View.OnClickListener {
    var timer = 0
    private var currentSelection: Int = -1
    var images = ArrayList<BannerData>()
    override fun getLayout(): Int = R.layout.activity_landing
    override fun getViewModelClass(): Class<LandingViewModel> = LandingViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.landing = mViewModel
        mBinding.lifecycleOwner = this
        setLocationListener(this)

        mBinding.viewPager2.registerOnPageChangeCallback(viewPagerPageChangeListener)
        bannerService()
        collectData()
        mBinding.JoinBtn.setOnClickListener(this)
        mBinding.termsCondition.setOnClickListener(this)
        mBinding.privacyPolicy.setOnClickListener(this)
        mBinding.tvVersion.text = "Version ${BuildConfig.VERSION_NAME}"
        analyticsToAllScreen("Landing_Activity", this@LandingActivity.localClassName)
    }

    override fun onResume() {
        super.onResume()
        if (!isLocationEnabled()) {
            openGPS()
        }
    }

    private val viewPagerPageChangeListener: ViewPager2.OnPageChangeCallback =
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                addBottomIndicators(position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        }

    private fun bannerService() {
        mViewModel!!.callToBannerImageService()
        val timer = Timer()
        timer.scheduleAtFixedRate(
            MyTimer(), 5000, 6000
        )
    }

    inner class MyTimer : TimerTask() {
        override fun run() {
            this@LandingActivity.runOnUiThread {
                if (mBinding.viewPager2.currentItem < images.size - 1) {
                    timer += 1
                    mBinding.viewPager2.currentItem = timer
                } else if (mBinding.viewPager2.currentItem == images.size - 1) {
                    mBinding.viewPager2.currentItem = 0
                    timer = 0
                }
            }
        }
    }

    private fun addBottomIndicators(currentPage: Int) {
        if (currentSelection == currentPage)
            return
        currentSelection = currentPage
        mBinding.dotsLinearLayout.removeAllViews()
        if (images.size > 1) {
            val dotsCurrent = arrayOfNulls<ImageView?>(images.size)
            dotsCurrent.indices.forEach { i ->
                dotsCurrent[i] = ImageView(this)
                dotsCurrent[i]!!.setImageResource(R.drawable.nonselecteditem_dot)
                dotsCurrent[i]!!.setPadding(5, 5, 5, 0)
                mBinding.dotsLinearLayout.addView(dotsCurrent[i])
            }
            if (dotsCurrent.isNotEmpty()) dotsCurrent[currentPage]?.setImageResource(R.drawable.selecteditem_dot)
        }
    }

    private fun collectData() {
        lifecycleScope.launch(Dispatchers.Main) {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                mViewModel!!.commonBannerResponse.collect { it ->
                    it.onLoading { progressDialog().show() }.onSuccess {
                        progressDialog().dismiss()
                        val result = it as CommonResponse
                        result.data?.banner_data?.let { it1 -> images.addAll(it1) }
                        addBottomIndicators(0)
                        val adapter = result.data?.banner_data?.let { it1 ->
                            ViewPagerAdapter(
                                this@LandingActivity, it1
                            )
                        }
                        mBinding.viewPager2.adapter = adapter
                    }.onError {
                        progressDialog(this@LandingActivity).dismiss()
                        mBinding.NoImageiv.visibility = View.VISIBLE
                        snackBar(it.message.toString(), false)
                    }
                }
            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {
            mBinding.JoinBtn.id -> {
                mViewModel!!.getTermsRead()?.let {
                    if (it) {
                        val customData = mapOf("Screen" to "LandingActivity", "key1" to 2)
                        logButtonClick("Join_button", customData)
                        launchNewActivity<SignUpActivity> { }
                    } else {
                        snackBar(getString(R.string.read_instruction), false)
                    }
                }
            }

            mBinding.termsCondition.id -> {
                mViewModel!!.setTermsRead()
                val customData = mapOf("Screen" to "LandingActivity", "key1" to 3)
                logButtonClick("termsCondition", customData)
                launchNewActivity<WebViewActivity> {
                    putExtra(AppConstants.WEB_URL, (BuildConfig.BASE_URL + getString(R.string.termsurl)))
                }
            }

            mBinding.privacyPolicy.id -> {
                val customData = mapOf("Screen" to "LandingActivity", "key1" to 4)
                logButtonClick("privacyPolicy", customData)
                launchNewActivity<WebViewActivity> {
                    putExtra(AppConstants.WEB_URL, getString(R.string.privacy_policy_url))
                }
            }
        }
    }
}