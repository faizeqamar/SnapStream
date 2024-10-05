package com.snapstream.app.di

import com.snapstream.app.database.AppDatabase
import com.snapstream.app.network.RetrofitClient
import com.snapstream.app.repository.ImageUploadRepository
import com.snapstream.app.utils.NetworkConnectivityObserver
import com.snapstream.app.viewmodel.ImageUploadViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Initialize the Room database
    single { AppDatabase.getDatabase(androidContext()) }

    // Provide the DAO from the Room database
    single { get<AppDatabase>().imageDao() }

    // Provide the ApiService
    single { RetrofitClient.api }

    // Provide the NetworkConnectivityObserver
    single { NetworkConnectivityObserver(androidContext()) }

    // Provide the ImageUploadRepository
    single { ImageUploadRepository(get(), get(), get()) }

    // Provide the ImageUploadViewModel
    viewModel { ImageUploadViewModel(get()) }
}
