package com.dertefter.ficus.fragments.schedule.person_schedule

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dertefter.neticore.data.schedule.Week

class ScheduleViewPagerPersonAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    val personSite: String?
) :

    FragmentStateAdapter(fragmentManager, lifecycle) {
    var weeksList: List<Week>? = null

    fun setWeeks(weeks: List<Week>?){
        if (weeks == null){
            weeksList = listOf()
        }else{
            weeksList = weeks
        }
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return weeksList?.size ?: 0
    }

    override fun createFragment(position: Int): SchedulePersonWeekFragment {
        val week = weeksList?.get(position)
        val fragment = SchedulePersonWeekFragment()
        val bundle = Bundle()
        bundle.putString("weekQuery", week?.weekQuery)
        bundle.putString("personId", personSite!!)
        fragment.arguments = bundle
        return fragment
    }
}