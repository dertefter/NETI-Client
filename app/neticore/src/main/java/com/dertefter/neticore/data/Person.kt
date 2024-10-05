package com.dertefter.neticore.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Person(
    val name: String,
    val shortName: String,
    val mail: String?,
    val site: String?,
    val pic: String?,
    var hasTimetable: Boolean = false,
    val contacts__card_address: String? = null,
    val contacts__card_time: String? = null,
    val about_disc: String? = null,
    val personPost: String? = null,
    val personProfiles: String? = null,

): Parcelable
