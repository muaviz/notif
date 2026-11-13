package com.notif.restyler

import android.app.Application

class RestylerApplication : Application() {
    companion object {
        lateinit var instance: RestylerApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
