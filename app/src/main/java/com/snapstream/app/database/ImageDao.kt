package com.snapstream.app.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query


@Dao
interface ImageDao {
    @Insert
    suspend fun insertImage(image: ImageEntity)

    @Query("SELECT * FROM images WHERE isUploaded = :status")
    fun getPendingImages(status: Boolean): List<ImageEntity>

    @Query("UPDATE images SET isUploaded = :status WHERE id = :id")
    suspend fun updateImageStatus(id: Int, status: Boolean)

    @Query("DELETE FROM images WHERE id = :id")
    suspend fun deleteImage(id: Int)
}
