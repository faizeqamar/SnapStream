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
    fun saveImageOrUpload(bitmap: Bitmap, apiKey: String, networkStatus: ConnectivityObserver.Status) {
        viewModelScope.launch {
            val imageData = compressBitmap(bitmap)
            Log.d(TAG, "Handling image of size: ${imageData.size} bytes")
            repository.saveImageToDbOrUpload(apiKey, imageData, networkStatus)
        }
    }

    fun uploadPendingImages(apiKey: String){
        viewModelScope.launch {
            repository.uploadPendingImages(apiKey)
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
