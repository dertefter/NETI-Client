package com.dertefter.neticore.data

data class NETICoreMetaData(
    val version_code: String = "0.0.1",
    val version_name: String = "indev",
    val name: String = "NETICore",
    val author: String = "dertefter",
    var initialized: Boolean = false,
)
