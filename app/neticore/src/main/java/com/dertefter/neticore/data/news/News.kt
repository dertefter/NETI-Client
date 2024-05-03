package com.dertefter.neticore.data.news

data class News(
    val newsid: String,
    val title: String,
    val date: String,
    val imageUrl: String?,
    val tag: String,
    val type: String,
    var error: String? = null
)
