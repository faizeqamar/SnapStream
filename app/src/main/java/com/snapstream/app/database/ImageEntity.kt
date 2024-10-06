package com.snapstream.app.database

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "images")
data class ImageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imageData: ByteArray,
    val isUploaded: Boolean = false  // Indicates if the image has been uploaded
)
