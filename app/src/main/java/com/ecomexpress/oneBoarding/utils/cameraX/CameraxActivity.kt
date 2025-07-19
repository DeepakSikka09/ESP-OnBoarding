package com.ecomexpress.oneBoarding.utils.cameraX

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.ecomexpress.oneBoarding.R
import com.ecomexpress.oneBoarding.databinding.ActivityCameraxBinding
import com.ecomexpress.oneBoarding.utils.comman.AppConstants.ONLY_FRONT_ENABLE
import com.theartofdev.edmodo.cropper.CropImage
import java.io.File
import java.util.*
import java.util.concurrent.ExecutionException

class CameraxActivity : AppCompatActivity(), View.OnClickListener {

    private var imageCapture: ImageCapture? = null
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    lateinit var activityCameraxBinding: ActivityCameraxBinding
    private var flashMode: Int = ImageCapture.FLASH_MODE_OFF
    private var camera: Camera? = null
    private val MY_CAMERA_REQUEST_CODE = 100
    var cameraCheck: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityCameraxBinding = ActivityCameraxBinding.inflate(layoutInflater)
        setContentView(activityCameraxBinding.getRoot())
        cameraCheck = intent.getBooleanExtra(ONLY_FRONT_ENABLE, false)
        activityCameraxBinding.btnSwitchCamera.setOnClickListener(this)
        activityCameraxBinding.btnTakePicture.setOnClickListener(this)
        activityCameraxBinding.cameraFlash.setOnClickListener(this)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                MY_CAMERA_REQUEST_CODE
            );
        }
        if (cameraCheck) {
            lensFacing = CameraSelector.LENS_FACING_FRONT
            activityCameraxBinding.cameraFlash.visibility = View.GONE
            activityCameraxBinding.btnSwitchCamera.visibility = View.GONE
        } else {
            lensFacing = CameraSelector.LENS_FACING_BACK
            activityCameraxBinding.ovalFace.visibility = View.GONE
        }
        startCamera()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnSwitchCamera -> {
                if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
                    lensFacing = CameraSelector.LENS_FACING_BACK
                    activityCameraxBinding.btnSwitchCamera.setImageResource(R.drawable.ic_outline_camera_rear)
                } else {
                    lensFacing = CameraSelector.LENS_FACING_FRONT
                    activityCameraxBinding.btnSwitchCamera.setImageResource(R.drawable.ic_outline_camera_front)
                }
                startCamera()
            }

            R.id.btnTakePicture -> {
                val folder = File("${filesDir}")
                if (!folder.exists()) {
                    folder.mkdirs()
                }
                val file = File(folder, System.currentTimeMillis().toString() + ".jpg")

                imageCapture?.takePicture(ImageCapture.OutputFileOptions.Builder(
                    file
                ).build(),
                    ContextCompat.getMainExecutor(this),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            // Image was successfully saved to `outputFileResults.savedUri`
                            Log.d(
                                "zsss",
                                Objects.requireNonNull(outputFileResults.getSavedUri()).toString()
                            )
                            if(!cameraCheck) {
                                val uri = Uri.fromFile(file)

                                val intent = CropImage.activity(uri).setAllowRotation(true)
                                    .getIntent(this@CameraxActivity)
                                imageCropperLauncher.launch(intent)
                            }else {
                                val intent = Intent()
                                intent.putExtra("data", file)
                                intent.putExtra("flag", true)
                                setResult(RESULT_OK, intent)
                                this@CameraxActivity.finish()
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {

                        }
                    })
            }

            R.id.camera_flash -> {
                if (flashMode == ImageCapture.FLASH_MODE_OFF) {
                    flashMode = ImageCapture.FLASH_MODE_ON
                    activityCameraxBinding.cameraFlash.setImageResource(R.drawable.ic_flash_on)
                } else {
                    flashMode = ImageCapture.FLASH_MODE_OFF
                    activityCameraxBinding.cameraFlash.setImageResource(R.drawable.ic_flash_off)
                }
                imageCapture?.flashMode = flashMode
            }

            R.id.ic_back_arrow -> {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }
    var imageCropperLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
                when (result.resultCode) {
                    RESULT_OK -> {
                        CropImage.getActivityResult(result.data)?.let { cropResult ->
                            val resultUri = cropResult.uri
                            val intent = Intent()
                            intent.putExtra("data", resultUri.toFile())
                            intent.putExtra("flag", true)
                            setResult(RESULT_OK, intent)
                            this@CameraxActivity.finish()
                        }
                    }
                }

        }
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            try {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                bindPreview(cameraProvider)
            } catch (e: ExecutionException) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            } catch (e: InterruptedException) {
            }
        }, ContextCompat.getMainExecutor(this))
    }

    open fun bindPreview(cameraProvider: ProcessCameraProvider) {
        //Preview
        val preview = Preview.Builder().build()
            .also { it.setSurfaceProvider(activityCameraxBinding.previewView.surfaceProvider) }
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY) //to manage image quality
            .setFlashMode(flashMode).build()

        cameraProvider.unbindAll()
        try {
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture
            )
        } catch (exc: Exception) {
            Log.e("bindCameraUseCases", "Use case binding failed", exc)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show()
            } else {
                finish()
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }
}