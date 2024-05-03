package com.dertefter.ficus.wearable

import android.app.Application
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.local.AppPreferences
import com.google.android.material.color.DynamicColors

class Ficus: Application() {

    var netiCore: NETICore? = null

    override fun onCreate() {
        super.onCreate()

        netiCore = NETICore()
        netiCore?.initialize(this)

    }

}