package com.ecomexpress.oneBoarding.utils.comman

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.ContentResolver
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.*
import android.media.ExifInterface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Environment
import android.provider.Settings
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.widget.AppCompatEditText
import com.ecomexpress.oneBoarding.utils.enums.MyPermissionConstants
import com.google.android.material.textfield.TextInputLayout
import java.io.*
import java.util.regex.Matcher
import java.util.regex.Pattern


object CommonUtils {
    /**
     *  hideKeyboard
     */
    fun hideKeyboard(activity: Activity) {
        val view = activity.currentFocus
        val methodManager = activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (view != null) {
            methodManager.hideSoftInputFromWindow(
                view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS
            )
        }

    }
    fun hideKeyboardForDialog(activity: Activity,ss: AppCompatEditText) {
        val im: InputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        im.hideSoftInputFromWindow(ss.windowToken, 0)
    }

    fun showSoftKeyboard(activity: Activity, editText: EditText?) {
        if (editText == null) return
        val imm: InputMethodManager =
            activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, 0)
    }


    fun hideKeyBoard(activity: Activity, ss: View) {
        val im: InputMethodManager =
            activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        im.hideSoftInputFromWindow(ss.applicationWindowToken, 0)
    }

    fun compressImages(tempFile: File, activity: Activity): String {
        val filePath: String = tempFile.path
        var scaledBitmap: Bitmap? = null
        val options = BitmapFactory.Options()
        /*by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
      you try the use the bitmap here, you will get null.*/
        options.inJustDecodeBounds = true
        var bmp = BitmapFactory.decodeFile(filePath, options)
        var actualHeight = options.outHeight
        var actualWidth = options.outWidth
        /*max Height and width values of the compressed image is taken as 1123x794*/
        val maxHeight = 1123.0f
        val maxWidth = 794.0f
        var imgRatio = (actualWidth / actualHeight).toFloat()
        val maxRatio = maxWidth / maxHeight
        /*width and height values are set maintaining the aspect ratio of the image*/
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight
                actualWidth = (imgRatio * actualWidth).toInt()
                actualHeight = maxHeight.toInt()
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth
                actualHeight = (imgRatio * actualHeight).toInt()
                actualWidth = maxWidth.toInt()
            } else {
                actualHeight = maxHeight.toInt()
                actualWidth = maxWidth.toInt()
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics = activity.windowManager.currentWindowMetrics
                val insets =
                    windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
                actualWidth = windowMetrics.bounds.width() - insets.left - insets.right
                actualHeight = windowMetrics.bounds.height() - insets.bottom - insets.top
            } else {
                val displayMetrics = DisplayMetrics()
                activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
                actualHeight = displayMetrics.heightPixels
                actualWidth = displayMetrics.widthPixels
            }
        }
        /*setting inSampleSize value allows to load a scaled down version of the original image*/options.inSampleSize =
            calculateInSampleSize(options, actualWidth, actualHeight)
        /*inJustDecodeBounds set to false to load the actual bitmap*/
        options.inJustDecodeBounds = false
        /*this options allow android to claim the bitmap memory if it runs low on memory*/
        options.inPurgeable = true
        options.inInputShareable = true
        options.inTempStorage = ByteArray(16 * 1024)
        try {
            /*load the bitmap from its path*/
            bmp = BitmapFactory.decodeFile(filePath, options)
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }
        val ratioX = actualWidth / options.outWidth.toFloat()
        val ratioY = actualHeight / options.outHeight.toFloat()
        val middleX = actualWidth / 2.0f
        val middleY = actualHeight / 2.0f
        val scaleMatrix = Matrix()
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)
        assert(scaledBitmap != null)
        val canvas = Canvas(scaledBitmap!!)
        canvas.setMatrix(scaleMatrix)
        canvas.drawBitmap(
            bmp, middleX - bmp.width / 2, middleY - bmp.height / 2, Paint(Paint.FILTER_BITMAP_FLAG)
        )
        //apply water mark on scaled bitmap. This image will synced to server.
        //No way to find employee id and location when location and employee id is not null uncomment below line.
        //Water mark will show on image.
        //scaledBitmap = applyWaterMark(scaledBitmap, empCode);
        //apply water mark on scaled bitmap. This image will synced to server.
        /*check the rotation of the image and display it properly*/
        val exif: ExifInterface
        try {
            exif = ExifInterface(filePath)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
            //  Log.d("EXIF", "Exif: " + orientation);
            val matrix = Matrix()
            if (orientation == 6) {
                matrix.postRotate(90f)
                //    Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180f)
                //    Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270f)
                //    Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(
                scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true
            )
            scaledBitmap = applyWaterMark(scaledBitmap)
        } catch (e: IOException) {
            //SathiLogger.e(e.getMessage());
            e.printStackTrace()
        }
        val out: FileOutputStream
        val file_size: Int = java.lang.String.valueOf(tempFile.length() / 1024).toInt()
        Log.d("check size", file_size.toString())
        val filename: String = tempFile.path
        try {
            out = FileOutputStream(filename)
            /*write the compressed bitmap at the destination specified by filename.*/scaledBitmap!!.compress(
                Bitmap.CompressFormat.JPEG, 85, out
            )
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return filename
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        val totalPixels = (width * height).toFloat()
        val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }
        return inSampleSize
    }

    private fun applyWaterMark(bmp: Bitmap): Bitmap? {
        val w = bmp.width
        val h = bmp.height
        val result = Bitmap.createBitmap(w, h, bmp.config)
        val canvas = Canvas(result)
        canvas.drawBitmap(bmp, 0f, 0f, null)
        val paint = Paint()
        paint.textSize = 18f
        paint.color = Color.YELLOW
        paint.isAntiAlias = true
        paint.isUnderlineText = true
        val point = Point()
        Log.e("TAG", "bmp w: " + bmp.width + ", h:" + bmp.height)
        if (bmp.width > bmp.height) {
            val temp = point.x
            point.x = point.y
            point.y = temp
        }
        return result
    }

    fun checkMobileNumberValidation(mobileNumber: String): Boolean {
        val regex = Regex("^[6-9]\\d{9}$")
        return regex.matches(mobileNumber)
    }

    fun getDeviceVersion(): String = Build.VERSION.RELEASE.toString()

    fun getDeviceID(contentResolver: ContentResolver): String = Settings.Secure.getString(
        contentResolver, Settings.Secure.ANDROID_ID
    )

    fun getDeviceModel(): String = Build.MODEL
    fun getDeviceName(): String = Build.MANUFACTURER

    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        var isOnline = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val capabilities: NetworkCapabilities? =
                connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork())
            isOnline =
                capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            val activeNetworkInfo: NetworkInfo? = connectivityManager.getActiveNetworkInfo()
            isOnline = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting

        }
        return isOnline
    }

    fun getBase64String(bitmap: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)
        val imageBytes = baos.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    fun addGrantedPermission(permissions: Array<String>): ArrayList<MyPermissionConstants> {
        val myPermissionConstantsArrayList: ArrayList<MyPermissionConstants> = ArrayList()
        for (element in permissions) {
            when (element) {
                Manifest.permission.CAMERA -> myPermissionConstantsArrayList.add(
                    MyPermissionConstants.PERMISSION_CAMERA
                )

                Manifest.permission.WRITE_EXTERNAL_STORAGE -> myPermissionConstantsArrayList.add(
                    MyPermissionConstants.PERMISSION_WRITE_EXTERNAL_STORAGE
                )

                Manifest.permission.ACCESS_COARSE_LOCATION -> myPermissionConstantsArrayList.add(
                    MyPermissionConstants.PERMISSION_ACCESS_COARSE_LOCATION
                )

                Manifest.permission.ACCESS_FINE_LOCATION -> myPermissionConstantsArrayList.add(
                    MyPermissionConstants.PERMISSION_ACCESS_FINE_LOCATION
                )
            }
        }

        return myPermissionConstantsArrayList
    }


    fun isValidPanCardNo(panCardNo: String?): Boolean {
        // Regex to check valid PAN Card number.
        val regex = "[A-Z]{3}P[A-Z]\\d{4}[A-Z]"
        // Compile the ReGex
        val p: Pattern = Pattern.compile(regex)
        // If the PAN Card number
        // is empty return false
        if (panCardNo == null) {
            return false
        }
        // Pattern class contains matcher() method
        // to find matching between given
        // PAN Card number using regular expression.
        val m: Matcher = p.matcher(panCardNo)
        // Return if the PAN Card number
        // matched the ReGex
        return m.matches()
    }

    fun isValidIFSCCode(str: String?): Boolean {
        // Regex to check valid IFSC Code.
        val regex = "^[A-Z]{4}0[A-Z0-9]{6}$"
        // Compile the ReGex
        val p = Pattern.compile(regex)
        // If the string is empty
        // return false
        if (str == null) {
            return false
        }
        // Pattern class contains matcher()
        // method to find matching between
        // the given string and
        // the regular expression.
        val m = p.matcher(str)
        // Return if the string
        // matched the ReGex
        return m.matches()
    }

    fun setError(edPanNo: TextInputLayout, StringToSet: String) {
        edPanNo.error = StringToSet
    }

    fun isValid_Bank_Acc_Number(bank_account_number: String?): Boolean {
        // Regex to check valid BANK ACCOUNT NUMBER Code
        val regex = "^[0-9]{9,18}$"
        // Compile the ReGex
        val p = Pattern.compile(regex)
        // If the bank_account_number Code
        // is empty return false
        if (bank_account_number == null) {
            return false
        }
        // Pattern class contains matcher() method
        // to find matching between given
        // bank_account_number Code using regular
        // expression.
        val m = p.matcher(bank_account_number)
        // Return if the bank_account_number Code
        // matched the ReGex
        return if (m.matches()) true else false
    }

    fun navigateToCall(context: Context, number: String?) {
        if (!number.isNullOrEmpty()) {
            val intentDial = Intent(
                Intent.ACTION_CALL, Uri.parse("tel:$number")
            ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intentDial)
        }
    }

    fun navigateToMap(context: Context, dcLat: Double, dcLong: Double) {
        val uri = "http://maps.google.co.in/maps?daddr=$dcLat,$dcLong&mode=bike"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        context.startActivity(intent)
    }

    fun flip(src: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.preScale(-1.0f, 1.0f)
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
    }

    fun downloadAPK(
        url: String,
        downloadManager: DownloadManager,
        appName: String,
        context: Context
    ): Long {
        val uri = Uri.parse(url)

        val request = DownloadManager.Request(uri).apply {
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            setTitle("$appName Download")
            setDescription("Downloading $appName...")

            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "$appName.apk"
            )
            setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_DOWNLOADS,
                file.absolutePath
            )
        }
        return downloadManager.enqueue(request)
    }

    fun autoInstall(uriLocation: Uri, context: Context) {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.N) {
            val install = Intent(Intent.ACTION_VIEW)
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            install.data = uriLocation
            context.startActivity(install)
        } else {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uriLocation, "application/vnd.android.package-archive")
            val resInfoList: List<ResolveInfo> = context.getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resInfoList) {
                context.grantUriPermission(
                    context.getApplicationContext().getPackageName() + ".fileprovider",
                    uriLocation,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            intent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.startActivity(intent)
        }
    }

    fun getCircularBitmap(srcBitmap: Bitmap?): Bitmap {
        val squareBitmapWidth = kotlin.math.min(srcBitmap!!.width, srcBitmap.height)
        val dstBitmap = Bitmap.createBitmap(
            squareBitmapWidth,  // Width
            squareBitmapWidth,  // Height
            Bitmap.Config.ARGB_8888 // Config
        )
        val canvas = Canvas(dstBitmap)
        val paint = Paint()
        paint.isAntiAlias = true
        val rect = Rect(0, 0, squareBitmapWidth, squareBitmapWidth)
        val mDrawableRadius: Float =
            Math.min(rect.height() / 2.0f, rect.width() / 2.0f)

        canvas.drawCircle(rect.exactCenterX(), rect.exactCenterY(), mDrawableRadius, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        val left = ((squareBitmapWidth - srcBitmap.width) / 2).toFloat()
        val top = ((squareBitmapWidth - srcBitmap.height) / 2).toFloat()
        canvas.drawBitmap(srcBitmap, left, top, paint)
        srcBitmap.recycle()
        return dstBitmap
    }

}
