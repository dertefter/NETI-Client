package com.dertefter.ficus

import android.content.Context
import com.ms_square.etsyblur.AsyncPolicy
import com.ms_square.etsyblur.SmartAsyncPolicy


enum class SmartAsyncPolicyHolder {
    INSTANCE;

    private var smartAsyncPolicy: AsyncPolicy? = null
    fun init(context: Context) {
        smartAsyncPolicy = SmartAsyncPolicy(context, true)
    }

    fun smartAsyncPolicy(): AsyncPolicy? {
        return smartAsyncPolicy
    }
}
