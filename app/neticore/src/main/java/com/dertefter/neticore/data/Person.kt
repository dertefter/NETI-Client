package com.dertefter.neticore.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Person(
    val name: String,
    val mail: String?,
    val site: String?,
    val pic: String?,
    var hasTimetable: Boolean = false,
    var id: String? = null
): Parcelable
