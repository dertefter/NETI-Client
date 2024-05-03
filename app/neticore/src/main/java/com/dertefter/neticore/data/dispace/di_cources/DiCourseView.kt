package com.dertefter.neticore.data.dispace.di_cources

data class DiCourseView(
    val navigationItems: List<String>,
    val contentItems: List<DiCourseContent>? = null,
    var id: String = "",
    var title: String = "",
)