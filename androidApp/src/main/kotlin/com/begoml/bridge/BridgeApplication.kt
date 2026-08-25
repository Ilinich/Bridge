package com.begoml.bridge

import android.app.Application
import com.begoml.bridge.di.startBridge
import org.koin.android.ext.koin.androidContext

class BridgeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startBridge { androidContext(this@BridgeApplication) }
    }
}
