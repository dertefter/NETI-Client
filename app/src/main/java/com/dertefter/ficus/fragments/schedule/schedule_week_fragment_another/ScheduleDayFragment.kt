package com.dertefter.ficus.fragments.schedule.schedule_week_fragment_another

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.MainActivity
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentScheduleWeekBinding
import com.dertefter.ficus.fragments.schedule.ScheduleFragment
import com.dertefter.ficus.fragments.schedule.schedule_week.ScheduleWeekRecyclerViewAdapter
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.Status
import com.dertefter.neticore.data.schedule.Day

class ScheduleDayFragment : Fragment(R.layout.fragment_schedule_week) {

    var netiCore: NETICore? = null

    lateinit var binding: FragmentScheduleWeekBinding

    var day: Day? = null

    override fun onResume() {
        super.onResume()


    }

    var adapter: ScheduleDayRecyclerViewAdapter? = null



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore

        binding = FragmentScheduleWeekBinding.bind(view)

        adapter = ScheduleDayRecyclerViewAdapter(this)
        binding.recyclerView.adapter = adapter
        val layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        binding.recyclerView.layoutManager = layoutManager
        val itemDecorator = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        itemDecorator.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.divider)!!)
        binding.recyclerView.addItemDecoration(itemDecorator)
        binding.shimmer.visibility = View.GONE
        day = ScheduleDayFragmentArgs.fromBundle(requireArguments()).day

        adapter?.setDayList(day!!.lessons, false)

    }


}