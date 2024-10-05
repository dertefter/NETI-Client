package com.dertefter.ficus.fragments.schedule.schedule_week

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentScheduleWeekBinding
import com.dertefter.ficus.fragments.schedule.ScheduleFragment
import com.dertefter.ficus.widgets.ScheduleWidget
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScheduleWeekFragment : Fragment(R.layout.fragment_schedule_week) {

    var netiCore: NETICore? = null

    lateinit var binding: FragmentScheduleWeekBinding

    var weekQuery: String? = null


    var adapter: ScheduleWeekRecyclerViewAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore

        binding = FragmentScheduleWeekBinding.bind(view)
        binding.recyclerView.updatePadding(top = 0)
        if (adapter == null){
            adapter = ScheduleWeekRecyclerViewAdapter(this)
        }
        binding.recyclerView.adapter = adapter
        val layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        binding.recyclerView.layoutManager = layoutManager
        val itemDecorator = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        itemDecorator.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.divider)!!)
        binding.recyclerView.addItemDecoration(itemDecorator)

        weekQuery = arguments?.getString("weekQuery")


        binding.retryButton.setOnClickListener {
            val week = netiCore?.client?.scheduleViewModel?.weeksLiveData?.value?.data?.find { week -> week.weekQuery == weekQuery }
            if (week != null){
                netiCore?.client?.scheduleViewModel?.loadScheduleWeek(week)
            }
        }
        observeSchedule()
    }



    fun observeSchedule(){
        if (weekQuery.isNullOrEmpty()){
            return
        }
        val livedata = netiCore?.client?.scheduleViewModel?.getLiveDataForWeek(weekQuery!!)
        livedata?.observe(viewLifecycleOwner){
            Log.e("zzzzzzzzzzz", it.data.toString())
            when (it.status){
                Status.LOADING -> {
                    binding.shimmer.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                    binding.errorView.visibility = View.GONE
                }
                Status.SUCCESS -> {
                    try{
                        binding.shimmer.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.errorView.visibility = View.GONE
                        adapter?.setDayList(it.data?.days)

                    }catch (e: Exception){
                        binding.shimmer.visibility = View.GONE
                        binding.recyclerView.visibility = View.GONE
                        binding.errorView.visibility = View.VISIBLE
                        Log.e("observeSchedule", e.stackTraceToString())
                    }


                }
                Status.ERROR -> {
                    binding.shimmer.visibility = View.GONE
                    binding.recyclerView.visibility = View.GONE
                    binding.errorView.visibility = View.VISIBLE
                }
            }
        }
    }

}