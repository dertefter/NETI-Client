package com.dertefter.neticore.data.schedule

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Lesson(
    val title: String,
    var timeStart: String? = null,
    var timeEnd: String? = null,
    var type: String? = null,
    var aud: String? = null,
    var person: String? = null,
    var groups: String? = null
): Parcelable