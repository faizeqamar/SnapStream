package com.snapstream.app.workmanager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.snapstream.app.repository.ImageUploadRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class UploadImagesWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    // Injecting the repository with Koin
    private val repository: ImageUploadRepository by inject()

    private val TAG = "UploadImagesWorker"

    override suspend fun doWork(): Result {
        Log.d(TAG, "Do Work function called....")
        return try {
            val apiKey = inputData.getString("API_KEY") ?: return Result.failure()

            Log.d(TAG, "Starting to upload pending images...")
            withContext(Dispatchers.IO) {
                repository.uploadPendingImages(apiKey) // Call your method to upload images
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload images from WorkManager", e)
            Result.failure()
        }
    }
}

