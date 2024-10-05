package com.snapstream.app.viewmodel
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CameraViewModel : ViewModel() {

    private val TAG = "CameraViewModel"

    /**
     * Process the captured image.
     *
     * @param bitmap The captured image as a Bitmap.
     */
    fun processCapturedImage(bitmap: Bitmap) {
        // Launch a coroutine for image processing
        viewModelScope.launch {
            // Perform any image processing here (e.g., resizing, compression)
            val processedImage = resizeImage(bitmap, 800, 600)

            // Prepare the image for upload (convert to ByteArray, etc.)
            val imageData = convertBitmapToByteArray(processedImage)
            Log.d(TAG, "ImageData: $imageData")
            // TODO: Call the upload function with imageData
        }
    }

    /**
     * Resize the image to the specified width and height.
     *
     * @param bitmap The original image.
     * @param width The desired width.
     * @param height The desired height.
     * @return The resized Bitmap.
     */
    private suspend fun resizeImage(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return withContext(Dispatchers.Default) {
            Bitmap.createScaledBitmap(bitmap, width, height, true)
        }
    }

    /**
     * Convert a Bitmap to a ByteArray.
     *
     * @param bitmap The Bitmap to convert.
     * @return The resulting ByteArray.
     */
    private suspend fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        return withContext(Dispatchers.IO) {
            val stream = java.io.ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            stream.toByteArray()
        }
    }
}
