package com.snapstream.app.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapstream.app.repository.ImageUploadRepository
import com.snapstream.app.utils.ConnectivityObserver
import com.snapstream.app.utils.NetworkConnectivityObserver
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

//class ImageUploadViewModel(private val repository: ImageUploadRepository) : ViewModel() {
//    val TAG = "ImageUploadViewModel"
//    fun uploadImage(bitmap: Bitmap, apiKey: String) {
//        viewModelScope.launch {
//            val imageData = compressBitmap(bitmap)
//            Log.d(TAG, "Uploading image of size: ${imageData.size} bytes")
//
//            val result = repository.uploadImage(apiKey, imageData)
//            result.onSuccess { imageUrl ->
//                Log.d(TAG, "Image uploaded successfully: $imageUrl")
//            }.onFailure { error ->
//                Log.e(TAG, "Image upload error: ${error.message}")
//            }
//        }
//    }
//
//    private fun compressBitmap(bitmap: Bitmap): ByteArray {
//        val stream = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
//        return stream.toByteArray()
//    }
//}






class ImageUploadViewModel(
    private val repository: ImageUploadRepository
) : ViewModel() {

    private val TAG = "ImageUploadViewModel"

    /**
     * Initiates the process of either uploading the image directly or saving it in Room DB.
     *
     * @param bitmap The image to be uploaded, represented as a Bitmap.
     * @param apiKey The API key used for authenticating the image upload request to the server.
     */
    fun saveImageOrUpload(bitmap: Bitmap, apiKey: String) {
        viewModelScope.launch {
            val imageData = compressBitmap(bitmap)
            Log.d(TAG, "Handling image of size: ${imageData.size} bytes")
            repository.saveImageToDbOrUpload(apiKey, imageData)
        }
    }

    /**
     * Compresses the provided Bitmap image into a byte array in JPEG format.
     *
     * @param bitmap The image to be compressed, represented as a Bitmap.
     * @return A ByteArray representing the compressed image data.
     */
    private fun compressBitmap(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        return stream.toByteArray()
    }
}
