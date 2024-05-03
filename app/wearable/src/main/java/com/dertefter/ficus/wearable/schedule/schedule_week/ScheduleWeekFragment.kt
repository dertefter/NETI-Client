package com.dertefter.ficus.wearable.schedule.schedule_week

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.ContextCompat
import androidx.core.view.InputDeviceCompat
import androidx.core.view.MotionEventCompat
import androidx.core.view.ViewConfigurationCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.wear.widget.WearableLinearLayoutManager
import com.dertefter.ficus.wearable.CustomScrollingLayoutCallback
import com.dertefter.ficus.wearable.schedule.ScheduleFragment
import com.dertefter.ficus.wearable.Ficus
import com.dertefter.ficus.wearable.R
import com.dertefter.ficus.wearable.databinding.FragmentScheduleWeekBinding
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class ScheduleWeekFragment : Fragment(R.layout.fragment_schedule_week) {

    var netiCore: NETICore? = null

    lateinit var binding: FragmentScheduleWeekBinding

    var weekQuery: String? = null
    var weekTitle: String? = null
    var isCurrent: Boolean? = null
    var groupTitle: String? = null
    var isIndividual: Boolean? = null

    override fun onResume() {
        super.onResume()
        (parentFragment as ScheduleFragment).binding?.appBarLayout?.liftOnScrollTargetViewId = binding?.recyclerView?.id!!
        (parentFragment as ScheduleFragment).setupAppBar(groupTitle!!, isIndividual!!)
        CoroutineScope(Dispatchers.Main).launch {
            binding.recyclerView.smoothScrollToPosition(adapter?.scrolledTo ?: 0)
        }

    }



    var adapter: ScheduleWeekRecyclerViewAdapter? = null



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore

        binding = FragmentScheduleWeekBinding.bind(view)
        if (adapter == null){
            adapter = ScheduleWeekRecyclerViewAdapter(this)
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.apply {
            layoutManager = WearableLinearLayoutManager(requireContext(), CustomScrollingLayoutCallback())
            isEdgeItemsCenteringEnabled = true
        }


        weekQuery = arguments?.getString("weekQuery")
        weekTitle = arguments?.getString("weekTitle")
        isCurrent = arguments?.getBoolean("isCurrent")
        groupTitle = arguments?.getString("groupTitle")
        isIndividual = arguments?.getBoolean("isIndividual")

        (parentFragment as ScheduleFragment).setupAppBar(groupTitle!!, isIndividual!!)

        val week = netiCore?.client?.scheduleViewModel?.weeksLiveData?.value?.data?.find { week -> week.weekQuery == weekQuery }

        binding.retryButton.setOnClickListener {
            netiCore?.getScheduleWeek(week)
        }
        netiCore?.getScheduleWeek(week)
        observeSchedule()
    }

    fun observeSchedule(){
        netiCore?.client?.scheduleViewModel?.scheduleListLiveData?.observe(viewLifecycleOwner){
            when (it.status){
                Status.LOADING -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                    binding.errorView.visibility = View.VISIBLE
                }
                Status.SUCCESS -> {
                    try{
                        binding.errorView.visibility = View.GONE
                        if (it.data?.get((weekQuery?.toInt() ?: 0) - 1)?.days.isNullOrEmpty()){
                            binding.errorView.visibility = View.GONE
                            binding.progressBar.visibility = View.VISIBLE
                            binding.recyclerView.visibility = View.GONE
                            if (it.data?.get((weekQuery?.toInt() ?: 0) - 1)?.isError == true){
                                binding.progressBar.visibility = View.GONE
                                binding.recyclerView.visibility = View.GONE
                                binding.errorView.visibility = View.VISIBLE
                            }
                        }else{
                            binding.progressBar.visibility = View.GONE
                            binding.recyclerView.visibility = View.VISIBLE
                            adapter?.setDayList(it.data?.get((weekQuery?.toInt() ?: 0) - 1)?.days)
                        }

                    }catch (e: Exception){
                        binding.progressBar.visibility = View.GONE
                        binding.recyclerView.visibility = View.GONE
                        binding.errorView.visibility = View.VISIBLE
                    }


                }
                Status.ERROR -> {
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerView.visibility = View.GONE
                    binding.errorView.visibility = View.VISIBLE
                }
            }
        }
    }

}