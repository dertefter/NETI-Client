package com.dertefter.neticore.data

import com.dertefter.neticore.data.schedule.Group

data class User(
    var name: String,
    var fullName: String,
    var group: Group,
)
