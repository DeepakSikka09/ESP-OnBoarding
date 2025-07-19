package com.ecomexpress.oneBoarding.ui.base

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.ecomexpress.oneBoarding.R
import com.ecomexpress.oneBoarding.data.datasource.local.preference.PreferenceKey
import com.ecomexpress.oneBoarding.data.repository.OneBoardingRepository
import com.ecomexpress.oneBoarding.databinding.BottomsheetNeedhelpBinding
import com.ecomexpress.oneBoarding.databinding.CustomDialogMessageBinding
import com.ecomexpress.oneBoarding.databinding.ProgressbarLayoutBinding
import com.ecomexpress.oneBoarding.utils.comman.AppPermissionsRunTime
import com.ecomexpress.oneBoarding.utils.comman.CommonUtils.addGrantedPermission
import com.ecomexpress.oneBoarding.utils.comman.CommonUtils.isInternetAvailable
import com.ecomexpress.oneBoarding.utils.comman.CommonUtils.navigateToCall
import com.ecomexpress.oneBoarding.utils.comman.CommonUtils.navigateToMap
import com.ecomexpress.oneBoarding.utils.language_support.LocalizationActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject


abstract class BaseActivity<VM : ViewModel, VB : ViewDataBinding> : LocalizationActivity() {
    var mViewModel: VM? = null
    lateinit var mBinding: VB
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    var permissionId = 44
    private val permissionCode = 2
    private var multiplePermissionCounter = 0
    private lateinit var dialog: Dialog
    private lateinit var networkDialog: Dialog
    private var pinCodeDataMutableLiveData = MutableLiveData<Int>()
    var dcLat: Double = 0.0
    var dcLong: Double = 0.0

    @Inject
    lateinit var repository: OneBoardingRepository

    @LayoutRes
    abstract fun getLayout(): Int
    abstract fun getViewModelClass(): Class<VM>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, getLayout())
        mViewModel = ViewModelProvider(this)[getViewModelClass()]
        mBinding.lifecycleOwner = this
        dialog = Dialog(this)
        networkDialog = Dialog(this)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    fun snackBar(message: String?, status: Boolean) {
        val customView = layoutInflater.inflate(R.layout.toast_layout, null)
        customView.findViewById<TextView>(R.id.tv_status).text = message
        when (status) {
            true -> {
                Snackbar.make(mBinding.root, message!!, Snackbar.LENGTH_LONG).apply {
                    this.setTextMaxLines(resources.getInteger(R.integer.snack_count))
                    this.view.setBackgroundColor(
                        ContextCompat.getColor(this@BaseActivity, R.color.transparent)
                    )
                    customView.findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.container)
                        .setBackgroundResource(R.drawable.success_toast_bg)
                    customView.findViewById<TextView>(R.id.tv_status)
                        .setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.success_toast_icon, 0, 0, 0
                        )
                    val layout = this.view as (Snackbar.SnackbarLayout)
                    layout.addView(customView)
                    show()
                }
            }

            false -> {
                Snackbar.make(mBinding.root, message!!, Snackbar.LENGTH_LONG).apply {
                    this.setTextMaxLines(resources.getInteger(R.integer.snack_count))
                    this.view.setBackgroundColor(
                        ContextCompat.getColor(this@BaseActivity, R.color.transparent)
                    )
                    customView.findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.container)
                        .setBackgroundResource(R.drawable.error_toast_bg)
                    customView.findViewById<TextView>(R.id.tv_status)
                        .setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.error_toast_icon, 0, 0, 0
                        )
                    val layout = this.view as (Snackbar.SnackbarLayout)
                    layout.addView(customView)
                    show()
                }
            }
        }
    }

    fun openHelpBottomSheet() {
        val dialog = BottomSheetDialog(this, R.style.otpsheetDialogTheme)
        val bottomSheetNeedHelpBinding: BottomsheetNeedhelpBinding =
            BottomsheetNeedhelpBinding.inflate(layoutInflater)
        dialog.setCancelable(true)
        dialog.setContentView(bottomSheetNeedHelpBinding.root)
        dialog.show()
        getLatLong()
        bottomSheetNeedHelpBinding.ivCross.setOnClickListener {
            dialog.cancel()
        }
        bottomSheetNeedHelpBinding.callNumberTv.setOnClickListener {
            // phone permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.CALL_PHONE),permissionCode)
            }
            else
            {
                navigateToCall(this, bottomSheetNeedHelpBinding.callNumberTv.text.toString())
            }
        }
        bottomSheetNeedHelpBinding.navigateIcon.setOnClickListener {
            //internet permission
            if(isInternetAvailable(this)) {
                navigateToMap(this, dcLat, dcLong)
            }else{
                snackBar("no internet connection",false)
            }
        }
        lifecycleScope.launch(Dispatchers.IO) {
            bottomSheetNeedHelpBinding.fullLocation.text =
                repository.getDataStoreContext().getString(PreferenceKey.USER_DC_ADDRESS.name) ?: ""
            bottomSheetNeedHelpBinding.callNumberTv.text =
                repository.getDataStoreContext().getString(PreferenceKey.DC_NUMBER.name) ?: ""
        }
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {}
    }

    fun getLatLong() {
        lifecycleScope.launch {
            dcLat = repository.getDataStoreContext().getDouble(PreferenceKey.DC_LATITUDE.name)!!
            dcLong = repository.getDataStoreContext().getDouble(PreferenceKey.DC_LONGITUDE.name)!!
        }
    }

    // method to check
    // if location is enabled
    fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    fun openGPS() {
        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        checkPermissionGrant(permissions, requestCode, grantResults)
    }

    private fun checkPermissionGrant(
        permissions: Array<String>, requestCode: Int, grantResults: IntArray
    ) {
        var isDefine = false
        var i = 0
        while (i < grantResults.size) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                multiplePermissionCounter++
                isDefine = true
                break
            }
            i++
        }
        if (isDefine) {
            if (multiplePermissionCounter >= 2) {
                AppPermissionsRunTime.aDialogOnPermissionDenied(this)
            } else {
                AppPermissionsRunTime.checkPermission(
                    this, addGrantedPermission(permissions), requestCode
                )
            }
        } else {
            when (requestCode) {
                permissionCode -> {
                    setLocationListener(this)
                }

                else -> {
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
                }
            }
        }
    }

    fun setLocationListener(activity: Activity) {
        // List of common permissions
        val commonPermissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.CALL_PHONE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Check permissions including POST_NOTIFICATIONS for Android 13 and higher
            val permissionsToCheck = commonPermissions + Manifest.permission.POST_NOTIFICATIONS
            val missingPermissions = permissionsToCheck.filter {
                ActivityCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
            }
            if (missingPermissions.isNotEmpty()) {
                // Request missing permissions
                ActivityCompat.requestPermissions(activity, missingPermissions.toTypedArray(), permissionCode)
                return
            }
        } else {
            // Check common permissions for lower Android versions
            val missingPermissions = commonPermissions.filter {
                ActivityCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
            }
            if (missingPermissions.isNotEmpty()) {
                // Request missing permissions
                ActivityCompat.requestPermissions(activity, missingPermissions.toTypedArray(), permissionCode)
                return
            }
        }

        // Initialize the FusedLocationProviderClient
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)

        // Get the current location
        fusedLocationProviderClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
                override fun isCancellationRequested() = false
            }
        ).addOnSuccessListener { location: Location? ->
            if (location != null) {
                val lat = location.latitude
                val lon = location.longitude
                val geocoder = Geocoder(activity, Locale.getDefault())

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(lat, lon, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            if (addresses.isNotEmpty()) {
                                setCurrentLocation(lat, lon, addresses[0].postalCode)
                            } else {
                                showToast("No address found", false)
                            }
                        }
                        override fun onError(errorMessage: String?) {
                            errorMessage?.let { showToast(it, false) }
                        }
                    })
                } else {
                    try {
                        val addresses = geocoder.getFromLocation(lat, lon, 1)
                        if (addresses != null) {
                            if (addresses.isNotEmpty()) {
                                setCurrentLocation(lat, lon, addresses[0].postalCode)
                            } else {
                                showToast("No address found", false)
                            }
                        }
                    } catch (ex: Exception) {
                        showDialog()
                    }
                }
            }
        }
    }

    fun setCurrentLocation(lat: Double, long: Double, currentPinCode: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            repository.getDataStoreContext().putDouble(PreferenceKey.USER_LATITUDE.name, lat)
            repository.getDataStoreContext().putDouble(PreferenceKey.USER_LONGITUDE.name, long)
            repository.getDataStoreContext()
                .putString(PreferenceKey.USER_PINCODE.name, currentPinCode)
        }
    }

    fun setNavigator(
        imageView: AppCompatImageView,
        imageView2: AppCompatImageView?,
        view1: View,
        view3: View,
        view4: View?,
        textView: AppCompatTextView,
        textView2: AppCompatTextView?,
        textView3: AppCompatTextView?
    ) {
        imageView.background = ContextCompat.getDrawable(this, R.drawable.green_circle)
        imageView2?.background = ContextCompat.getDrawable(this, R.drawable.green_circle)
        view1.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
        view3.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
        view4?.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
        textView.setTextColor(ContextCompat.getColor(this, R.color.green))
        textView2?.setTextColor(ContextCompat.getColor(this, R.color.green))
        textView3?.setTextColor(ContextCompat.getColor(this, R.color.green))

    }

    fun analyticsToAllScreen(screenName: String, className: String) {
        val params = Bundle()
        params.putString("open_time", "Analytics check Ok")
        FirebaseAnalytics.getInstance(this).logEvent(FirebaseAnalytics.Event.SELECT_ITEM, params)

        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, className)
        FirebaseAnalytics.getInstance(this).logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }


    fun progressDialog(
        context: Activity = this, parentBinding: ViewDataBinding = mBinding
    ): Dialog {
        val binding: ProgressbarLayoutBinding
        if (!dialog.isShowing) {
            val params = dialog.window!!.attributes
            params.width = WindowManager.LayoutParams.MATCH_PARENT // set as full width
            params.height = WindowManager.LayoutParams.MATCH_PARENT//full height
            dialog.window?.setGravity(Gravity.CENTER_HORIZONTAL)
            binding = ProgressbarLayoutBinding.inflate(
                LayoutInflater.from(parentBinding.root.context),
                parentBinding.root as ViewGroup,
                false
            )
            dialog.setContentView(binding.root, params)
            dialog.window!!.setBackgroundDrawableResource(
                R.color.transparent
            )
            dialog.setCancelable(false)

        }
        return dialog
    }

    fun showToast(message: String, status: Boolean) {
        val customToastLayout = layoutInflater.inflate(R.layout.toast_layout, null)
        val customToast = Toast(this)
        customToast.view = customToastLayout
        customToastLayout.findViewById<TextView>(R.id.tv_status).text = message
        customToast.setGravity(Gravity.FILL_HORIZONTAL or Gravity.BOTTOM, 0, 10)
        customToast.duration = Toast.LENGTH_SHORT
        if (status) {
            customToastLayout.findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.container)
                .setBackgroundResource(R.drawable.success_toast_bg)
            customToastLayout.findViewById<TextView>(R.id.tv_status)
                .setCompoundDrawablesWithIntrinsicBounds(R.drawable.success_toast_icon, 0, 0, 0)
        } else {
            customToastLayout.findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.container)
                .setBackgroundResource(R.drawable.error_toast_bg)
            customToastLayout.findViewById<TextView>(R.id.tv_status)
                .setCompoundDrawablesWithIntrinsicBounds(R.drawable.error_toast_icon, 0, 0, 0)

        }
        customToast.show()
    }

    fun logButtonClick(buttonName: String, customData: Map<String, Any> = emptyMap()) {
        val bundle = Bundle()
        for ((key, value) in customData) {
            when (value) {
                is String -> bundle.putString(key, value)
                is Int -> bundle.putInt(key, value)
                is Double -> bundle.putDouble(key, value)
                // Add other types as needed
            }
        }
        FirebaseAnalytics.getInstance(this).logEvent(buttonName, bundle)
    }

    private fun showDialog() {
        networkDialog.setCancelable(false)
        val dialogBinding: CustomDialogMessageBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.custom_dialog_message,
            mBinding.root as ViewGroup,
            false
        )
        networkDialog.setContentView(dialogBinding.root)
        networkDialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        networkDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogBinding.btn.setOnClickListener {
            networkDialog.dismiss()
        }
        if (!this.isFinishing) {
            if (!networkDialog.isShowing) {
                networkDialog.show()
            }
        }

    }

    override fun onBackPressed() {
        super.onBackPressedDispatcher.onBackPressed()
        overridePendingTransition(
            R.anim.slide_in_left, R.anim.slide_out_right
        )
    }


}

