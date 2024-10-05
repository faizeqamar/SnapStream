package com.snapstream.app.ui.di

import com.snapstream.app.ui.network.RetrofitClient
import com.snapstream.app.ui.repository.UserRepository
import com.snapstream.app.ui.viewmodel.UserViewModel
import org.koin.dsl.module

val appModule = module {
    single { RetrofitClient.instance }
    single { UserRepository(get()) }
    single { UserViewModel(get()) }
}
