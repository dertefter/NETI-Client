package com.dertefter.neticore.data.campus_pass

data class CampusPassData(
    val isRegistered: Boolean,
    val campusPass: CampusPass? = null,
    val campusPassDates: MutableList<CampusPassDate>? = null,
    val campusPassTimes: MutableList<CampusPassTime>? = null
)
