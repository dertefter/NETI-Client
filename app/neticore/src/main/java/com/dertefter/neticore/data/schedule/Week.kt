package com.dertefter.neticore.data.schedule

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class Week(
    var weekQuery: String,
    var weekTitle: String,
    var isCurrent: Boolean,
    var groupTitle: String = "",
    val isIndividual: Boolean,
    var days: @RawValue List<Day>? = null,
    var isLoadingSchedule: Boolean = false,
): Parcelable