package com.snapstream.app.ui.repository

import com.snapstream.app.ui.model.UserResponse
import com.snapstream.app.ui.network.ApiService
import retrofit2.Call

class UserRepository(private val apiService: ApiService) {
    fun getUser(userId: Int): Call<UserResponse> {
        return apiService.getUser(userId)
    }
}
