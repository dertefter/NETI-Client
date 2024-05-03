package com.dertefter.neticore.data.messages

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Message (
    val mesId: String,
    val title: String,
    val text: String,
    var isNew: Boolean = false,
    val date: String,
): Parcelable