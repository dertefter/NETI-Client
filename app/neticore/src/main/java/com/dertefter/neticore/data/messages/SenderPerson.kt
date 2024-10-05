package com.dertefter.neticore.data.messages

import android.os.Parcelable
import com.dertefter.neticore.data.Person
import kotlinx.parcelize.Parcelize

@Parcelize
data class SenderPerson(
    var name: String,
    var messages: MutableList<Message>,
    var isNew: Boolean = messages.any { it.isNew },
    val last_message_theme: String = messages.last().title,
    val last_message_text: String = messages.last().text,
    val last_message_date: String = messages.last().date,
    var personLink: String? = null
): Parcelable