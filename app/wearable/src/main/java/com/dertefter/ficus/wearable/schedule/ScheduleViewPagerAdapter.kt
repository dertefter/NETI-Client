package com.dertefter.ficus.wearable.schedule

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dertefter.ficus.wearable.schedule.schedule_week.ScheduleWeekFragment
import com.dertefter.neticore.data.schedule.Week

class ScheduleViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle, val b: Boolean) :

    FragmentStateAdapter(fragmentManager, lifecycle) {
    var weeksList: List<Week>? = null
    var compact_view = false
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


    override fun createFragment(position: Int): Fragment {
        val week = weeksList?.get(position)
        val fragment = ScheduleWeekFragment()
        val bundle = Bundle()
        bundle.putString("weekQuery", week?.weekQuery)
        bundle.putString("weekTitle", week?.weekTitle)
        bundle.putBoolean("isCurrent", week?.isCurrent!!)
        bundle.putString("groupTitle", week?.groupTitle)
        bundle.putBoolean("isIndividual", week?.isIndividual!!)
        fragment.arguments = bundle
        return fragment
    }
}