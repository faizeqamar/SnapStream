package com.snapstream.app.repository

import android.content.Context
import android.util.Log
import com.snapstream.app.database.ImageDao
import com.snapstream.app.database.ImageEntity
import com.snapstream.app.network.ApiService
import com.snapstream.app.utils.ConnectivityObserver
import com.snapstream.app.utils.NetworkConnectivityObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody

///**
// * Repository class responsible for handling image upload operations to the ImgBB server.
// *
// * It provides a method to upload an image represented as a byte array and returns
// * the result as a URL string upon successful upload.
// *
// * @param apiService defines the API endpoints for image uploading.
// */
//class ImageUploadRepository(private val apiService: ApiService) {
//
//   // This function is used to uploads an image as a multipart request to the server
//    suspend fun uploadImage(apiKey: String, imageData: ByteArray): Result<String> {
//        return try {
//            val mediaType = "image/*".toMediaTypeOrNull()
//            val requestBody = RequestBody.create(mediaType, imageData)
//            val imagePart = MultipartBody.Part.createFormData("image", "image.jpg", requestBody)
//
//            // Make the API call to upload the image
//            val response = apiService.uploadImage(apiKey, imagePart)
//
//            if (response.isSuccessful) {
//                val imageUrl = response.body()?.data?.display_url
//                Result.success(imageUrl ?: "")
//            } else {
//                Result.failure(Exception("Image upload failed: ${response.errorBody()?.string()}"))
//            }
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//}




/**
 * Repository class responsible for handling image upload operations to the ImgBB server
 * and storing images in the Room database when offline.
 *
 * This repository observes network connectivity and uploads any pending images when the network becomes available.
 *
 * @param apiService defines the API endpoints for image uploading.
 * @param imageDao Room database DAO for storing images locally.
 * @param networkObserver observes network status changes.
 */
class ImageUploadRepository(
    private val apiService: ApiService,
    private val imageDao: ImageDao,
    private val networkObserver: NetworkConnectivityObserver
) {

    private val TAG = "ImageUploadViewModel"
    init {
        // Observe network changes and trigger pending uploads when network is available
        observeNetworkConnectivity()
    }

    /**
     * Uploads an image to the server if the network is available, or stores it in the Room database for future upload.
     * @param apiKey API key for image upload.
     * @param imageData Image data as ByteArray to be uploaded.
     */
    suspend fun saveImageToDbOrUpload(apiKey: String, imageData: ByteArray) {
        if (networkObserver.observe().first() == ConnectivityObserver.Status.Available) {
            val result = uploadImage(apiKey, imageData)
            if (result.isSuccess) {
                Log.d(TAG, "Image uploaded successfully")
            } else {
                saveImageLocally(imageData)
                Log.d(TAG, "Saved image locally due to upload failure")
            }
        } else {
            saveImageLocally(imageData)
            Log.d(TAG, "Saved image locally because offline")
        }
    }

    /**
     * Upload image to the ImgBB server.
     * @param apiKey API key for the image upload.
     * @param imageData Image data as ByteArray.
     * @return Result containing success or failure with error message.
     */
    private suspend fun uploadImage(apiKey: String, imageData: ByteArray): Result<String> {
        return try {
            val mediaType = "image/*".toMediaTypeOrNull()
            val requestBody = RequestBody.create(mediaType, imageData)
            val imagePart = MultipartBody.Part.createFormData("image", "image.jpg", requestBody)

            val response = apiService.uploadImage(apiKey, imagePart)
            if (response.isSuccessful) {
                val imageUrl = response.body()?.data?.display_url
                Result.success(imageUrl ?: "")
            } else {
                Result.failure(Exception("Image upload failed: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Saves an image in the Room database for later upload.
     * @param imageData Image data as ByteArray.
     */
    private suspend fun saveImageLocally(imageData: ByteArray) {
        val imageEntity = ImageEntity(imageData = imageData, isUploaded = false)
        imageDao.insertImage(imageEntity)
    }

    /**
     * Uploads all pending images stored in Room when the network becomes available.
     * @param apiKey API key for the image upload.
     */
    suspend fun uploadPendingImages(apiKey: String) {
        val pendingImages = imageDao.getPendingImages(false)
        for (image in pendingImages) {
            val result = uploadImage(apiKey, image.imageData)
            if (result.isSuccess) {
                imageDao.updateImageStatus(image.id, true)
                Log.d(TAG, "Uploaded image from Room DB")
            } else {
                Log.e(TAG, "Failed to upload image from Room DB")
            }
        }
    }

    /**
     * Observes network connectivity using NetworkConnectivityObserver and uploads pending images
     * when the network becomes available.
     */
    private fun observeNetworkConnectivity() {
        CoroutineScope(Dispatchers.IO).launch {
            networkObserver.observe().collect { status ->
                when (status) {
                    ConnectivityObserver.Status.Available -> {
                        Log.d(TAG, "Network is available, uploading pending images.")
                        uploadPendingImages("YourApiKeyHere") // Replace with actual API key
                    }
                    ConnectivityObserver.Status.Lost, ConnectivityObserver.Status.Unavailable -> {
                        Log.d(TAG, "Network is unavailable.")
                    }
                    else -> {
                        Log.d(TAG, "Network status: $status")
                    }
                }
            }
        }
    }
}
