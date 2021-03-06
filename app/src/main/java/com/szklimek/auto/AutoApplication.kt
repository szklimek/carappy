package com.szklimek.auto

import android.app.Application
import com.szklimek.auto.obd.ObdService
import com.szklimek.base.Log
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.dsl.module

class AutoApplication : Application(), KoinComponent {

    override fun onCreate() {
        super.onCreate()
        Log.d("Starting application, koin init")
        startKoin {
            androidContext(this@AutoApplication)
            modules(appModule)
        }
    }
}

val appModule = module {
    factory { PreferenceManager(androidContext()) }
    single { ObdService(get()) }
}
