package com.snapstream.app.ui.model

data class ImgBBResponse(
    val data: ImgData,
    val success: Boolean,
    val status: Int
)

data class ImgData(
    val id: String,
    val url: String,
    val display_url: String
)