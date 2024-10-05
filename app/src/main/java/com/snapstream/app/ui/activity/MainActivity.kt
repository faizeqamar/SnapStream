package com.snapstream.app.ui.activity

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.snapstream.app.databinding.ActivityMainBinding
import com.snapstream.app.viewmodel.CameraViewModel
import com.snapstream.app.viewmodel.ImageUploadViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val cameraViewModel: CameraViewModel by viewModels()
    private val executor = Executors.newSingleThreadExecutor()
    private var imageCapture: ImageCapture? = null
    private var captureRunnable: Runnable? = null
    private val viewModel: ImageUploadViewModel by inject()


    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
                val apiKey = "206938ce1faee7a1be38d6b0dab2488c"
                viewModel.saveImageOrUpload(bitmap, apiKey)

                // Convert Bitmap to ByteArray
//                val byteArray = bitmapToByteArray(bitmap)
//                val byteArray = compressBitmap(bitmap)
//
//                    if (byteArray != null) {
//                    // Upload the byte array to ImgBB
//                    uploadImageToImgBB(byteArray)
//                } else {
//                    Log.e(TAG, "Failed to convert bitmap to byte array")
//                }
                // Process the captured image
                cameraViewModel.processCapturedImage(bitmap)
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




//    private fun uploadImageToImgBB(imageData: ByteArray) {
//        val apiKey = "206938ce1faee7a1be38d6b0dab2488c"
//        Log.d(TAG, "Uploading image of size: ${imageData.size} bytes")
//
//        // Prepare the byte array for upload
//        val mediaType = "image/*".toMediaTypeOrNull()
//        val requestBody = RequestBody.create(mediaType, imageData)
//        val imagePart = MultipartBody.Part.createFormData("image", "image.png", requestBody)
//
//        // Make the API call to upload the image
//        val call = RetrofitClient.api.uploadImage(apiKey, imagePart)
//        call.enqueue(object : Callback<ImgBBResponse> {
//            override fun onResponse(call: Call<ImgBBResponse>, response: Response<ImgBBResponse>) {
//                if (response.isSuccessful) {
//                    val imageUrl = response.body()?.data?.display_url
//                    Log.d(TAG, "Image uploaded successfully: $imageUrl")
//                } else {
//                    Log.e(TAG, "Image upload failed: ${response.errorBody()?.string()}")
//                }
//            }
//
//            override fun onFailure(call: Call<ImgBBResponse>, t: Throwable) {
//                when (t) {
//                    is IOException -> Log.e(TAG, "Network error: ${t.message}")
//                    else -> Log.e(TAG, "Image upload error: ${t.message}")
//                }
//            }
//        })
//    }
//
//    private fun compressBitmap(bitmap: Bitmap): ByteArray {
//        val stream = ByteArrayOutputStream()
//        // Compress the Bitmap to JPEG format with 80% quality
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
//        return stream.toByteArray()
//    }

}
