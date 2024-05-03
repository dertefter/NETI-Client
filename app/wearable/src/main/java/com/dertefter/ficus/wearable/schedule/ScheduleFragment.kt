package com.dertefter.ficus.wearable.schedule

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.dertefter.ficus.wearable.Ficus
import com.dertefter.ficus.wearable.R
import com.dertefter.ficus.wearable.databinding.FragmentScheduleBinding
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.Status
import com.dertefter.neticore.data.schedule.Week
import com.dertefter.neticore.local.AppPreferences
import com.google.android.material.color.MaterialColors
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ScheduleFragment : Fragment(R.layout.fragment_schedule) {

    var netiCore: NETICore? = null

    lateinit var binding: FragmentScheduleBinding

    var adapter: ScheduleViewPagerAdapter? = null

    var currentWeek: Int? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentScheduleBinding.bind(view)
        adapter = ScheduleViewPagerAdapter(childFragmentManager, lifecycle, AppPreferences.compact_schedule == true)
        binding.viewPager.adapter = adapter

        netiCore = (activity?.application as Ficus).netiCore

        binding.appBarLayout.setStatusBarForegroundColor(
            MaterialColors.getColor(binding.appBarLayout, com.google.android.material.R.attr.colorSurface))
        observeGroupInfo()
        Log.e("schedule", "onViewCreated")
    }



    fun observeGroupInfo(){
        netiCore?.client?.scheduleViewModel?.currentGroupLiveData?.observe(viewLifecycleOwner){
            if (it != null && it.title.isNotEmpty()){
                if (netiCore?.client?.scheduleViewModel?.weeksLiveData?.value?.data?.get(0)?.groupTitle != it.title){
                    netiCore?.client?.updateWeeks()
                }
                observeWeeksInfo()
                binding.noGroup.visibility = View.GONE
            }else{
                binding.noGroup.visibility = View.VISIBLE
            }
        }
    }

    fun observeWeeksInfo(){
        netiCore?.client?.scheduleViewModel?.weeksLiveData?.observe(viewLifecycleOwner){
            when (it.status){
                Status.LOADING -> {
                    setupViewPager(null)
                }
                Status.ERROR -> {
                    setupViewPager(null)
                }
                Status.SUCCESS -> {
                    val weeks = it.data
                    setupViewPager(weeks)
                }

            }
        }
    }

    fun setupViewPager(weeks: List<Week>?) {
        adapter?.setWeeks(weeks)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = weeks?.get(position)?.weekTitle
            if (weeks?.get(position)?.isCurrent == true){
                currentWeek = position
                CoroutineScope(Dispatchers.Main).launch {
                    tab.setCustomView(R.layout.tab_item2)
                    tab.view.findViewById<android.widget.TextView>(R.id.text).text = weeks[position].weekTitle
                    tab.select()
                }
            }
        }.attach()
    }

    fun setupAppBar(title: String = "", isIndividual: Boolean){
        CoroutineScope(Dispatchers.Main).launch {
            binding.toolbar.title = title
        }

    }

}