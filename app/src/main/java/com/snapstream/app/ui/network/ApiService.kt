package com.snapstream.app.ui.network

import com.snapstream.app.ui.model.ImgBBResponse
import com.snapstream.app.ui.model.UserResponse
import com.snapstream.app.ui.repository.CloudinaryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("users/{user_id}")
    fun getUser(
        @Path("user_id") userId: Int
    ): Call<UserResponse>

    @Multipart
    @POST("upload")
    fun uploadImage(
        @Query("key") apiKey: String,
        @Part image: MultipartBody.Part
    ): Call<ImgBBResponse>
}