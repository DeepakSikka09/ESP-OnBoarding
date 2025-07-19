package com.ecomexpress.oneBoarding.utils.comman

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.ecomexpress.oneBoarding.R
import com.ecomexpress.oneBoarding.utils.enums.MyPermissionConstants


object AppPermissionsRunTime {

    private var requiredPermissionsList: ArrayList<String>? = null
    private var requiredPermissionMsgs: ArrayList<String>? = null

    fun checkPermission(
        mActivity: Activity,
        rqstedPermissionsList: ArrayList<MyPermissionConstants>,
        PERMISSION_REQUEST_CODE: Int
    ): Boolean {
        if (requiredPermissionsList == null) {
            requiredPermissionsList = ArrayList()
            requiredPermissionMsgs = ArrayList()
        } else {
            requiredPermissionsList!!.clear()
            requiredPermissionMsgs!!.clear()
        }
        if (rqstedPermissionsList != null && rqstedPermissionsList.size > 0) {
            for (requestedPermission in rqstedPermissionsList) {
                when (requestedPermission) {
                    MyPermissionConstants.PERMISSION_ACCESS_FINE_LOCATION -> addPermission(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        mActivity
                    )
                    MyPermissionConstants.PERMISSION_ACCESS_COARSE_LOCATION -> addPermission(
                        Manifest.permission.ACCESS_COARSE_LOCATION, mActivity
                    )
                    MyPermissionConstants.PERMISSION_CAMERA -> addPermission(
                        Manifest.permission.CAMERA,
                        mActivity
                    )
                    MyPermissionConstants.PERMISSION_WRITE_EXTERNAL_STORAGE ->
                        addPermission(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            mActivity
                        )
                    MyPermissionConstants.PERMISSION_READ_EXTERNAL_STORAGE -> addPermission(
                        Manifest.permission.READ_EXTERNAL_STORAGE, mActivity
                    )
                }
            }
        }
        return if (requiredPermissionsList!!.size > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mActivity.requestPermissions(
                    requiredPermissionsList!!.toTypedArray(),
                    PERMISSION_REQUEST_CODE
                )
            }
            false
        } else {
            true
        }
    }

    private fun addPermission(permission: String, mActivity: Activity) {
        if (ContextCompat.checkSelfPermission(
                mActivity,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requiredPermissionsList!!.add(permission)
        }
    }

    fun aDialogOnPermissionDenied(mContext: Context) {
        val alertDialogBuilder = AlertDialog.Builder(mContext)
        alertDialogBuilder.setTitle(mContext.resources.getString(R.string.alert))
        alertDialogBuilder.setMessage(mContext.resources.getString(R.string.reGrantPermissionMsg))
        alertDialogBuilder.setPositiveButton(
            mContext.resources.getString(R.string.action_settings)
        ) { dialog, which ->
            dialog.dismiss()
            val uri = Uri.fromParts("package", mContext.packageName, null)
            val settingsIntent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                uri
            )
            settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            mContext.startActivity(settingsIntent)
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

}