package com.snapstream.app.ui.application

import android.app.Application
import com.snapstream.app.ui.di.appModule
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(appModule)
        }
    }
}