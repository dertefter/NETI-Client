package com.dertefter.ficus.fragments.schedule.schedule_week_fragment_another

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dertefter.ficus.fragments.schedule.schedule_week.ScheduleWeekFragment
import com.dertefter.neticore.data.schedule.Day
import com.dertefter.neticore.data.schedule.Week

class ScheduleViewPagerAdapterAnother(fragmentManager: FragmentManager, lifecycle: Lifecycle) :

    FragmentStateAdapter(fragmentManager, lifecycle) {
    private var dayList: List<Day>? = null

    fun setDayList(days: List<Day>?){
        Log.e("gggggg", "setting day list $days")
        if (days == null){
            dayList = listOf()
        }else{
            dayList = days
        }
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return dayList?.size ?: 0
    }

    override fun createFragment(position: Int): ScheduleDayFragment {
        val day = dayList?.get(position)
        val args = ScheduleDayFragmentArgs(day!!).toBundle()
        args.putInt("index", position)
        val fragment = ScheduleDayFragment()
        fragment.arguments = args
        return fragment
    }
}