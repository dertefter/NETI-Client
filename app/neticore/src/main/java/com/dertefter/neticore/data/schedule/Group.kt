package com.dertefter.neticore.data.schedule

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Group(
    val title: String,
    var isIndividual: Boolean = false,
): Parcelable
