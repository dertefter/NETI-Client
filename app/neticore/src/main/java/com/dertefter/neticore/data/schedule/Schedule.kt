package com.dertefter.neticore.data.schedule

data class Schedule(
    var days: List<Day>? = null,
    var isError: Boolean = false
)
