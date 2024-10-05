package com.dertefter.ficus.fragments.schedule.person_schedule

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentScheduleWeekBinding
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.Status

class SchedulePersonWeekFragment : Fragment(R.layout.fragment_schedule_week) {

    var netiCore: NETICore? = null

    lateinit var binding: FragmentScheduleWeekBinding

    var weekQuery: String? = null
    var weekTitle: String? = null
    var isCurrent: Boolean? = null
    var groupTitle: String? = null
    var isIndividual: Boolean? = null

    override fun onResume() {
        super.onResume()
        (parentFragment as PersonScheduleFragment).binding?.appBarLayout?.liftOnScrollTargetViewId = binding?.recyclerView?.id!!
    }

    var adapter: ScheduleWeekRecyclerViewAdapter? = null



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore

        binding = FragmentScheduleWeekBinding.bind(view)
        binding.recyclerView.updatePadding(top = 8)
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
        var personSite = arguments?.getString("personId")!!.split("/")
        val personId = personSite[personSite.size - 2]
        val week = netiCore?.client?.schedulePersonModel?.weeksLiveData?.value?.data?.find { week -> week.weekQuery == weekQuery }
        netiCore?.client?.schedulePersonModel?.loadScheduleWeek(personId!! , week!!, )
        observeSchedule()
    }

    fun observeSchedule(){
        netiCore?.client?.schedulePersonModel?.scheduleListLiveData?.observe(viewLifecycleOwner){
            when (it.status){
                Status.LOADING -> {
                    binding.shimmer.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                }
                Status.SUCCESS -> {
                    try{
                        if (it.data?.get((weekQuery?.toInt() ?: 0) - 1)?.days.isNullOrEmpty()){
                            binding.shimmer.visibility = View.VISIBLE
                            binding.recyclerView.visibility = View.GONE
                        }else{
                            binding.shimmer.visibility = View.GONE
                            binding.recyclerView.visibility = View.VISIBLE
                            adapter?.setDayList(it.data?.get((weekQuery?.toInt() ?: 0) - 1)?.days)
                        }
                    }catch (e: Exception){
                        binding.shimmer.visibility = View.GONE
                        binding.recyclerView.visibility = View.GONE
                    }


                }
                Status.ERROR -> {
                    binding.shimmer.visibility = View.GONE
                    binding.recyclerView.visibility = View.GONE
                }
            }
        }
    }

}