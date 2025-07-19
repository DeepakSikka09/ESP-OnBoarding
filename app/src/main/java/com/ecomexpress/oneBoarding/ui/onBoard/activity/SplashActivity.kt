package com.ecomexpress.oneBoarding.ui.onBoard.activity

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.ecomexpress.oneBoarding.R
import com.ecomexpress.oneBoarding.databinding.ActivitySplashBinding
import com.ecomexpress.oneBoarding.ui.base.BaseActivity
import com.ecomexpress.oneBoarding.ui.onBoard.viewmodel.SplashViewModel
import com.ecomexpress.oneBoarding.utils.comman.launchNewActivity
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class SplashActivity : BaseActivity<SplashViewModel, ActivitySplashBinding>() {
    private lateinit var appUpdateManager: AppUpdateManager
    var listener: InstallStateUpdatedListener? = null
    override fun getLayout(): Int = R.layout.activity_splash

    override fun getViewModelClass(): Class<SplashViewModel> = SplashViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appUpdateManager = AppUpdateManagerFactory.create(this)
    }

    override fun onResume() {
        super.onResume()
        registerListener()
        onUpdateCheck()
    }

    private fun onUpdateCheck() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS || info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                if (info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    startUpdate(info)
                }
            } else {
                moveToLanguageScreen()
            }
        }.addOnFailureListener {
            moveToLanguageScreen()
        }
    }

    private fun startUpdate(appUpdateInfo: AppUpdateInfo) {
        appUpdateManager.startUpdateFlowForResult(
            appUpdateInfo,
            activityLauncher,
            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
        )
    }


    private fun moveToLanguageScreen() {
        removeListener()
        lifecycleScope.launch {
            delay(3000)
            launchNewActivity<ChooseLanguageActivity> { }
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeListener()
    }

    private fun removeListener() {
        if (listener != null) {
            appUpdateManager.unregisterListener(listener!!)
            listener = null
        }
    }

    private fun registerListener() {
        if (listener == null) {
            listener = InstallStateUpdatedListener { installState ->
                if (installState.installStatus() == InstallStatus.DOWNLOADED) {
                    appUpdateManager.completeUpdate()
                } else if (installState.installStatus() == InstallStatus.INSTALLED) {
                    moveToLanguageScreen()
                }
            }
            appUpdateManager.registerListener(listener!!)
        }
    }

    private val activityLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == RESULT_CANCELED) {
                showToast(getString(R.string.update_cancel), false)
            }
            if (it.resultCode == ActivityResult.RESULT_IN_APP_UPDATE_FAILED) {
                showToast(getString(R.string.update_fail), false)
            }
        }
}