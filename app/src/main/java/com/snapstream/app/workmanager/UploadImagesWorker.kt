package com.snapstream.app.workmanager

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.snapstream.app.repository.ImageUploadRepository
import kotlinx.coroutines.runBlocking

class UploadImagesWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val repository: ImageUploadRepository // Pass your repository here
) : Worker(context, workerParams) {
    private val TAG = "UploadImagesWorker"

    override fun doWork(): Result {
        Log.d(TAG, "Do Work  function called....")
        return try {
            val apiKey = inputData.getString("API_KEY") ?: return Result.failure()
            runBlocking {
                Log.d(TAG, "Starting to upload pending images...")
                repository.uploadPendingImages(apiKey) // Call your method to upload old images
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload images from WorkManager", e)
            Result.failure()
        }
    }
}

