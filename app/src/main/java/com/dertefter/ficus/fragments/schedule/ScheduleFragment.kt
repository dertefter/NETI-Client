package com.dertefter.ficus.fragments.schedule

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.MainActivity
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentScheduleBinding
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.Status
import com.dertefter.neticore.data.schedule.Week
import com.dertefter.neticore.local.AppPreferences
import com.google.android.material.color.MaterialColors
import com.google.android.material.tabs.TabLayout
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


        binding?.todayWeekFab?.hide()
        binding.toolbar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.set_group -> {
                    val action = ScheduleFragmentDirections.actionScheduleFragmentToSearchGroupFragment()
                    findNavController().navigate(action)
                    true
                }
                R.id.search_person -> {
                    val action = ScheduleFragmentDirections.actionScheduleFragmentToSearchPersonFragment()
                    findNavController().navigate(action)
                    true
                }
                else -> false
            }
        }
        binding.appBarLayout.setStatusBarForegroundColor(
            MaterialColors.getColor(binding.appBarLayout, com.google.android.material.R.attr.colorSurface))
        netiCore?.checkGroup()
        binding.swiperefresh.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            netiCore?.client?.updateWeeks()
        })
        observeGroupInfo()
        observeUserInfo()

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
                binding.setGroupButton.setOnClickListener {
                    findNavController().navigate(R.id.action_scheduleFragment_to_searchGroupFragment)
                }
                binding.noGroup.visibility = View.VISIBLE

            }
        }
    }

    fun observeWeeksInfo(){
        netiCore?.client?.scheduleViewModel?.weeksLiveData?.observe(viewLifecycleOwner){
            when (it.status){
                Status.LOADING -> {
                    binding.swiperefresh.isRefreshing = true
                    setupViewPager(null)
                }
                Status.ERROR -> {
                    binding.swiperefresh.isRefreshing = false
                    setupViewPager(null)
                    (activity as MainActivity).createNotificationSnackbar("Ошибка загрузки расписания!", "Проверьте подключение к интернету", "error")

                }
                Status.SUCCESS -> {
                    binding.swiperefresh.isRefreshing = false
                    val weeks = it.data
                    setupViewPager(weeks)
                }

            }
        }
    }

    fun setupViewPager(weeks: List<Week>?) {
        adapter?.setWeeks(weeks)
        if (binding.viewPager.adapter == null){
            binding.viewPager.adapter = adapter
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = weeks?.get(position)?.weekTitle
            if (weeks?.get(position)?.isCurrent == true){
                currentWeek = position
                tab.setCustomView(R.layout.tab_item2)
                tab.view.findViewById<android.widget.TextView>(R.id.text).text = weeks[position].weekTitle
            }
        }.attach()
        if (currentWeek != null){
            binding.tabLayout.getTabAt(currentWeek!!)?.select()
            binding.todayWeekFab.setOnClickListener {
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(currentWeek!!), true)
            }
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position != currentWeek && currentWeek != null){
                    binding?.todayWeekFab?.show()
                } else{
                    binding?.todayWeekFab?.hide()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    fun setupAppBar(title: String = "", isIndividual: Boolean){
        CoroutineScope(Dispatchers.Main).launch {
            if (isIndividual){
                binding.toolbarCollapse.title = "Индивидуальное расписание"
                binding.toolbarCollapse.subtitle = title
            }else{
                binding.toolbarCollapse.title= "Расписание занятий"
                binding.toolbarCollapse.subtitle = title
            }

        }

    }

    fun observeUserInfo(){
       netiCore?.client?.userInfoViewModel?.userInfoLiveData?.observe(viewLifecycleOwner){
           when (it.status){
               Status.SUCCESS -> {
                   binding.syncGroup.visibility = View.GONE
               }
               Status.ERROR -> {
                   binding.syncGroup.visibility = View.GONE
                     }
               Status.LOADING -> {
                   binding.syncGroup.visibility = View.VISIBLE
               }
           }
       }
    }

}