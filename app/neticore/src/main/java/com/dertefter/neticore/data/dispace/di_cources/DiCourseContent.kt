package com.dertefter.neticore.data.dispace.di_cources

data class DiCourseContent(
    val type: String,
    val htmlContent: String? = null,
    val attachmentLink: String? = null,
    val attachmentName: String? = null,
    var imagesChips: List<String>? = null

)
