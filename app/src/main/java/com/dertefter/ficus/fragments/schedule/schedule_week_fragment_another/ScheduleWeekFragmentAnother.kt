package com.dertefter.ficus.fragments.schedule.schedule_week_fragment_another

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
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
    var weekTitle: String? = null
    var isCurrent: Boolean? = null
    var groupTitle: String? = null
    var isIndividual: Boolean? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore
        binding = FragmentScheduleWeekAnotherBinding.bind(view)

        weekQuery = arguments?.getString("weekQuery")
        weekTitle = arguments?.getString("weekTitle")
        isCurrent = arguments?.getBoolean("isCurrent")
        groupTitle = arguments?.getString("groupTitle")
        isIndividual = arguments?.getBoolean("isIndividual")

        (parentFragment as ScheduleFragment).setupAppBar(groupTitle!!, isIndividual!!)

        val week = netiCore?.client?.scheduleViewModel?.weeksLiveData?.value?.data?.find { week -> week.weekQuery == weekQuery }

        if (netiCore?.client?.scheduleViewModel?.scheduleListLiveData?.value?.data?.get((weekQuery?.toInt() ?: 0) - 1)?.days.isNullOrEmpty()){
            netiCore?.getScheduleWeek(week)
        }

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
                    CoroutineScope(Dispatchers.Main).launch {
                        tab.select()
                    }
                }
            }.attach()
        }
    }

    fun observeSchedule(){
        netiCore?.client?.scheduleViewModel?.scheduleListLiveData?.observe(viewLifecycleOwner){
            when (it.status){
                Status.LOADING -> {
                    binding.shimmer.visibility = View.VISIBLE
                    binding.viewPager.visibility = View.GONE
                }
                Status.SUCCESS -> {
                    try{
                        val ids = AppWidgetManager.getInstance(requireActivity().application).getAppWidgetIds(
                            ComponentName(
                                requireActivity().application,
                                ScheduleWidget::class.java
                            )
                        )
                        val myWidget = ScheduleWidget()
                        myWidget.onUpdate(requireContext(), AppWidgetManager.getInstance(requireContext()), ids)
                        if (it.data?.get((weekQuery?.toInt() ?: 0) - 1)?.isError == true){
                            binding.errorView.visibility = View.VISIBLE
                            setupViewPager(null)
                            binding.shimmer.visibility = View.GONE
                            binding.tabLayout.visibility = View.INVISIBLE
                        }else{
                            binding.tabLayout.visibility = View.VISIBLE
                            binding.errorView.visibility = View.GONE
                            if (it.data?.get((weekQuery?.toInt() ?: 0) - 1)?.days.isNullOrEmpty()){
                                setupViewPager(null)
                                binding.shimmer.visibility = View.GONE
                            }
                            else{
                                setupViewPager(it.data?.get((weekQuery?.toInt() ?: 0) - 1)?.days)
                                binding.shimmer.visibility = View.GONE
                            }
                        }
                        Log.e("testtt", it.data?.get((weekQuery?.toInt() ?: 0) - 1)?.days.toString())



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