package com.begoml.bridge

import android.app.Application
import com.begoml.bridge.di.startBridge
import com.begoml.bridge.foundation.background.BackgroundRefresh
import org.koin.android.ext.koin.androidContext

class BridgeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val koin = startBridge { androidContext(this@BridgeApplication) }
        koin.koin.get<BackgroundRefresh>().schedule()
    }
}
