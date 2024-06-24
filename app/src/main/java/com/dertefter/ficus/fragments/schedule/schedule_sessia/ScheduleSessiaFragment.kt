package com.dertefter.ficus.fragments.schedule.schedule_sessia

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentScheduleSessiaBinding
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.Status
import com.dertefter.neticore.data.schedule.SessiaScheduleItem
import com.google.android.material.color.MaterialColors


class ScheduleSessiaFragment : Fragment(R.layout.fragment_schedule_sessia) {
    var netiCore: NETICore? = null

    lateinit var binding: FragmentScheduleSessiaBinding

    var adapter: ScheduleSessiaListAdapter? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore
        binding = FragmentScheduleSessiaBinding.bind(view)
        adapter  =  ScheduleSessiaListAdapter(this)
        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager  = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val itemDecorator = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        itemDecorator.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.divider)!!)
        binding.recyclerview.addItemDecoration(itemDecorator)
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.appBarLayout.setStatusBarForegroundColor(
            MaterialColors.getColor(binding.appBarLayout, com.google.android.material.R.attr.colorSurface))

        observeSessia()
        getSessiaSchedule()
        ViewCompat.setOnApplyWindowInsetsListener(binding.recyclerview) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            binding.recyclerview.updatePadding(bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

    }

    fun openDialog(item: SessiaScheduleItem){
        val modalBottomSheet = ScheduleSessiaBottomSheet.newInstance(item)
        modalBottomSheet.show(parentFragmentManager, ScheduleSessiaBottomSheet.TAG)
    }
    fun observeSessia(){
        netiCore?.client?.scheduleViewModel?.sessiaScheduleLiveData?.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {binding.loading.visibility = View.VISIBLE}
                Status.SUCCESS -> {
                    binding.loading.visibility = View.GONE
                    adapter!!.setList(it.data)

                }
                Status.ERROR -> {binding.loading.visibility = View.GONE}
            }
        }
    }

    fun getSessiaSchedule() {
        netiCore?.client?.scheduleViewModel?.loadSessiaSchedule()
    }

}