package com.snapstream.app.ui.activity

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.snapstream.app.databinding.ActivityMainBinding
import com.snapstream.app.utils.ConnectivityObserver
import com.snapstream.app.utils.NetworkConnectivityObserver
import com.snapstream.app.viewmodel.ImageUploadViewModel
import com.snapstream.app.workmanager.UploadImagesWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val executor = Executors.newSingleThreadExecutor()
    private var imageCapture: ImageCapture? = null
    private var captureRunnable: Runnable? = null
    private val viewModel: ImageUploadViewModel by inject()
    private val connectivityObserver: NetworkConnectivityObserver by inject()
    private var networkStatus: ConnectivityObserver.Status = ConnectivityObserver.Status.Unavailable // Default
    private val apiKey = "206938ce1faee7a1be38d6b0dab2488c"



    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkInternetConnection()
        // Lock orientation to portrait
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    override fun onPause() {
        super.onPause()
        // Remove any pending capture runnable to prevent crashes
        captureRunnable?.let { binding.viewFinder.removeCallbacks(it) }
    }

    /**
     * Start the camera and bind use cases.
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview use case
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            // ImageCapture use case
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            // Select back camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind all use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

                // Start continuous image capture
                startImageCapture()

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * Capture images continuously at a set interval.
     */
    private fun startImageCapture() {
        val captureInterval = 10000L // Capture every 1 second

        captureRunnable = object : Runnable {
            override fun run() {
                capturePhoto()
                binding.viewFinder.postDelayed(this, captureInterval)
            }
        }
        captureRunnable?.let {
            binding.viewFinder.post(it)
        }

        Log.d(TAG, "Image capture started!")
    }

    /**
     * Capture a photo and process it.
     */
    private fun capturePhoto() {
        val imageCapture = imageCapture ?: return

        // Capture image
        imageCapture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                // Convert ImageProxy to Bitmap
                val bitmap = imageProxy.toBitmap()
                imageProxy.close()
                onImageCaptured(bitmap)
                Log.d(TAG, "Image captured successfully!")
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
            }
        })
    }


    /**
     * Check if all required permissions are granted.
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Handle the result of permission request.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                // Permission not granted, show a message to the user
                // and close the app or disable camera functionality
            }
        }
    }



    /**
     * Monitors internet connectivity status and triggers actions based on the current state.
     */
    private fun checkInternetConnection() {
        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {
            connectivityObserver.observe().collect { status ->
                networkStatus = status
                when (status) {
                    ConnectivityObserver.Status.Available -> {
                        Log.d(TAG, "***** Internet Connected *****")
                        uploadPendingImages()
                    }

                    ConnectivityObserver.Status.Unavailable -> {
                        Log.d(TAG, "***** Internet Disconnected *****")
                    }

                    ConnectivityObserver.Status.Losing -> {
                        Log.d(TAG, "***** Internet Losing *****")
                    }

                    ConnectivityObserver.Status.Lost -> {
                        Log.d(TAG, "***** Internet Lost *****")
                    }
                }
            }
        }
    }



    /**
     * Passes the captured image to the ViewModel for upload or local storage.
     */
    private fun onImageCaptured(bitmap: Bitmap) {
        viewModel.saveImageOrUpload(bitmap, apiKey, networkStatus)
    }

    private fun uploadPendingImages() {
        Log.d(TAG, "Upload Pending Images function called")
        val workRequest = OneTimeWorkRequestBuilder<UploadImagesWorker>()
            .setInputData(workDataOf("API_KEY" to apiKey))
            .build()
        WorkManager.getInstance(this.applicationContext).enqueue(workRequest)
    }
}
