package com.dertefter.ficus.fragments.schedule.schedule_week_fragment_another

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentScheduleWeekAnotherBinding
import com.dertefter.ficus.fragments.schedule.ScheduleFragment
import com.dertefter.ficus.widgets.ScheduleWidget
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.Status
import com.dertefter.neticore.data.schedule.Day
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ScheduleWeekFragmentAnother : Fragment(R.layout.fragment_schedule_week_another) {

    var netiCore: NETICore? = null

    lateinit var binding: FragmentScheduleWeekAnotherBinding

    var weekQuery: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore
        binding = FragmentScheduleWeekAnotherBinding.bind(view)

        weekQuery = arguments?.getString("weekQuery")

        val week = netiCore?.client?.scheduleViewModel?.weeksLiveData?.value?.data?.find { week -> week.weekQuery == weekQuery }

        netiCore?.getScheduleWeek(week)

        binding.retryButton.setOnClickListener {
            binding.errorView.visibility = View.GONE
            netiCore?.getScheduleWeek(week)
        }


        observeSchedule()
    }

    override fun onResume() {
        super.onResume()

    }

    fun setupViewPager(days: List<Day>?) {

        if (days != null){
            val adapter = ScheduleViewPagerAdapterAnother(childFragmentManager, lifecycle)
            binding.viewPager.adapter = adapter
            adapter.setDayList(days)
            TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                tab.text = days[position].title.uppercase()
                if (days[position].today){
                    tab.setCustomView(R.layout.tab_item)
                    tab.view.findViewById<TextView>(R.id.text).text = days[position].title.uppercase()
                }
            }.attach()
            Handler(Looper.getMainLooper()).postDelayed({
                for (d in days){
                    if (d.today){
                        binding.tabLayout.selectTab(binding.tabLayout.getTabAt(days.indexOf(d)))
                    }
                }
            }, 500)
        }
    }

    fun observeSchedule(){
        val livedata = netiCore?.client?.scheduleViewModel?.getLiveDataForWeek(weekQuery!!)
        livedata?.observe(viewLifecycleOwner){
            when (it.status){
                Status.LOADING -> {
                    binding.shimmer.visibility = View.VISIBLE
                    binding.viewPager.visibility = View.GONE
                }
                Status.SUCCESS -> {
                    try{
                        binding.tabLayout.visibility = View.VISIBLE
                        binding.viewPager.visibility = View.VISIBLE
                        binding.errorView.visibility = View.GONE
                        setupViewPager(it.data?.days)
                        binding.shimmer.visibility = View.GONE
                    }catch (e: Exception){
                        binding.shimmer.visibility = View.GONE
                    }


                }
                Status.ERROR -> {
                    binding.shimmer.visibility = View.GONE
                }
            }
        }
    }

}