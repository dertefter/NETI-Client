package com.dertefter.neticore.data.schedule

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SessiaScheduleItem(
    val title: String,
    var time: String? = null,
    var date: String? = null,
    var type: String? = null,
    var aud: String? = null,
    var personLink: String? = null,
    val dayName: String?  = null,
): Parcelable