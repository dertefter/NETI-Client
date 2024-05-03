package com.dertefter.ficus.fragments.schedule.schedule_week

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
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
        weekTitle = arguments?.getString("weekTitle")
        isCurrent = arguments?.getBoolean("isCurrent")
        groupTitle = arguments?.getString("groupTitle")
        isIndividual = arguments?.getBoolean("isIndividual")

        (parentFragment as ScheduleFragment).setupAppBar(groupTitle!!, isIndividual!!)

        val week = netiCore?.client?.scheduleViewModel?.weeksLiveData?.value?.data?.find { week -> week.weekQuery == weekQuery }

        binding.retryButton.setOnClickListener {
            netiCore?.getScheduleWeek(week)
        }
        observeSchedule()

        netiCore?.getScheduleWeek(week)

    }

    fun observeSchedule(){
        netiCore?.client?.scheduleViewModel?.scheduleListLiveData?.observe(viewLifecycleOwner){
            when (it.status){
                Status.LOADING -> {
                    binding.shimmer.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                    binding.errorView.visibility = View.VISIBLE
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
                        binding.errorView.visibility = View.GONE
                        if (it.data?.get((weekQuery?.toInt() ?: 0) - 1)?.days.isNullOrEmpty()){
                            binding.errorView.visibility = View.GONE
                            binding.shimmer.visibility = View.VISIBLE
                            binding.recyclerView.visibility = View.GONE
                            if (it.data?.get((weekQuery?.toInt() ?: 0) - 1)?.isError == true){
                                binding.shimmer.visibility = View.GONE
                                binding.recyclerView.visibility = View.GONE
                                binding.errorView.visibility = View.VISIBLE
                            }
                        }else{
                            binding.shimmer.visibility = View.GONE
                            binding.recyclerView.visibility = View.VISIBLE
                            adapter?.setDayList(it.data?.get((weekQuery?.toInt() ?: 0) - 1)?.days)
                        }

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