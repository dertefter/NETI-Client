package com.dertefter.ficus

import android.app.Application
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.local.AppPreferences
import com.google.android.material.color.DynamicColors
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class Ficus: Application() {

    var netiCore: NETICore? = null

    override fun onCreate() {
        super.onCreate()
        SmartAsyncPolicyHolder.INSTANCE.init(applicationContext)
        netiCore = NETICore()
        netiCore?.initialize(this)

    }

}