package com.begoml.bridge

import android.app.Application
import com.begoml.bridge.di.startBridge
import com.begoml.bridge.host.hostModules
import com.begoml.bridge.core.background.BackgroundRefresh
import org.koin.android.ext.koin.androidContext

class BridgeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val koin = startBridge(hostModules = hostModules()) {
            androidContext(this@BridgeApplication)
        }
        koin.koin.get<BackgroundRefresh>().schedule()
    }
}
