package com.ecomexpress.oneBoarding.ui.onBoard.activity

import android.os.Bundle
import android.webkit.WebViewClient
import com.ecomexpress.oneBoarding.R
import com.ecomexpress.oneBoarding.databinding.ActivityWebViewBinding
import com.ecomexpress.oneBoarding.ui.base.BaseActivity
import com.ecomexpress.oneBoarding.ui.onBoard.viewmodel.WebViewModel
import com.ecomexpress.oneBoarding.utils.comman.AppConstants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WebViewActivity : BaseActivity<WebViewModel, ActivityWebViewBinding>() {

    override fun getLayout(): Int = R.layout.activity_web_view
    override fun getViewModelClass(): Class<WebViewModel> = WebViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.lifecycleOwner = this
        mBinding.webView.settings.javaScriptEnabled = true
        mBinding.webView.loadUrl(intent.getStringExtra(AppConstants.WEB_URL)!!)
        mBinding.webView.webViewClient = WebViewClient()
    }
}