package com.dertefter.neticore.data.dispace.di_messages

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DiSenderPerson(
    val companion_id: String,
    val date: String,
    val group_title: String?,
    val id: String,
    val is_new: String,
    val is_read: String,
    val last_msg: String,
    val message: String,
    val name: String,
    val patronymic: String,
    val photo: String?,
    val surname: String,
    val theme: String?,
    val w_color: String,
    val w_title: String,
    val workspace_id: String
): Parcelable
