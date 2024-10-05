package com.snapstream.app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.snapstream.app.ui.model.UserResponse
import com.snapstream.app.ui.repository.UserRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _user = MutableLiveData<UserResponse>()
    val user: LiveData<UserResponse> get() = _user

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun fetchUser(userId: Int) {
        userRepository.getUser(userId).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    _user.value = response.body()
                } else {
                    _error.value = response.errorBody()?.string() ?: "Unknown error"
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                _error.value = t.message
            }
        })
    }
}
