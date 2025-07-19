package com.ecomexpress.oneBoarding.utils.cameraX


import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.ecomexpress.oneBoarding.R
import com.ecomexpress.oneBoarding.databinding.ActivityScannerBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors


class Scanner : AppCompatActivity() {
    lateinit var activityScannerBinding: ActivityScannerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityScannerBinding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(activityScannerBinding.getRoot())
        startAnimation()
        val processCameraProvider = ProcessCameraProvider.getInstance(this)
        processCameraProvider.addListener(Runnable {
            try {
                val cameraProvider: ProcessCameraProvider = processCameraProvider.get()
                bindPreview(cameraProvider)
            } catch (e: ExecutionException) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            } catch (e: InterruptedException) {
                Log.d("error", e.toString())
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        //1. Configure the camera( understand available resolution and make decision based on that
        val preview = Preview.Builder()
            .build()
            .also { it.setSurfaceProvider(activityScannerBinding.previewView.getSurfaceProvider()) }
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_CODABAR,
                Barcode.FORMAT_AZTEC,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_CODE_39,
                Barcode.FORMAT_CODE_93,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_PDF417
            )
            .build()
        val scanner = BarcodeScanning.getClient(options)
        val imageAnalysis = ImageAnalysis.Builder().build()

        imageAnalysis.setAnalyzer(
            Executors.newSingleThreadExecutor()
        ) { image: ImageProxy? -> image?.let { processImageProxy(scanner, it) } }


        try {
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
        } catch (e: IllegalStateException) {
            // If the use case has already been bound to another lifecycle or method is not called on main thread.
            Log.e("TAG", e.message!!)
        } catch (e: IllegalArgumentException) {
            // If the provided camera selector is unable to resolve a camera to be used for the given use cases.
            Log.e("TAG", e.message!!)
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(barcodeScanner: BarcodeScanner, imageProxy: ImageProxy) {
        val inputImage = Objects.requireNonNull(imageProxy.image)?.let {
            InputImage.fromMediaImage(
                it,
                imageProxy.getImageInfo().getRotationDegrees()
            )
        }

        inputImage?.let {
            barcodeScanner.process(it)
                .addOnSuccessListener(OnSuccessListener<List<Barcode>> { barcodes ->
                    if (barcodes.size > 0) {
                        MediaPlayer.create(this, R.raw.beep_sound).start()
                        barcodes[0].rawValue?.let { Log.d("barcode", it) }
                    }
                }).addOnFailureListener(OnFailureListener { })
                .addOnCompleteListener(OnCompleteListener<List<Barcode?>?> {
                    imageProxy.image?.close()
                    imageProxy.close()
                })
        }
    }

    fun startAnimation() {

        val animation: Animation =
            AnimationUtils.loadAnimation(this, R.anim.up_down_anim)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
            }

            override fun onAnimationEnd(animation: Animation) {
                activityScannerBinding.bar.setVisibility(View.GONE)
            }

            override fun onAnimationRepeat(animation: Animation) {
            }
        })
        activityScannerBinding.bar.setVisibility(View.VISIBLE)
        activityScannerBinding.bar.startAnimation(animation)
    }
}