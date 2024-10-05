package com.dertefter.ficus.fragments.schedule

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.core.view.iterator
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.MainActivity
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentScheduleBinding
import com.dertefter.ficus.fragments.schedule.schedule_rules_dialog.ScheduleRulesDialogFragment
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

    var savedGroupsAdapter: SavedGroupsAdapter? = null

    var currentWeek: Int? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentScheduleBinding.bind(view)
        adapter = ScheduleViewPagerAdapter(childFragmentManager, lifecycle, AppPreferences.vertical_schedule == false)
        savedGroupsAdapter = SavedGroupsAdapter(this)
        binding.viewPager.adapter = adapter
        binding.savedGroupsRecyclerview.adapter = savedGroupsAdapter
        binding.savedGroupsRecyclerview.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false )
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
                R.id.sessia -> {
                    val action = ScheduleFragmentDirections.actionScheduleFragmentToScheduleSessiaFragment()
                    findNavController().navigate(action)
                    true
                }
                R.id.edit -> {
                    val dialog = ScheduleRulesDialogFragment()
                    dialog.show(childFragmentManager, "schedule_rules_dialog")
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
        observeWeeksInfo()
        observeSavedGroups()
    }

    fun updateSavedGroups(){
        netiCore?.client?.scheduleViewModel?.getSavedGroups()
    }
    override fun onResume() {
        super.onResume()
        updateSavedGroups()
    }

    fun observeSavedGroups(){
        netiCore?.client?.scheduleViewModel?.savedGroupsLiveData?.observe(viewLifecycleOwner){
            savedGroupsAdapter?.setData(it)
        }
    }
    fun observeGroupInfo(){
        netiCore?.client?.scheduleViewModel?.currentGroupLiveData?.observe(viewLifecycleOwner){
            if (it != null && it.title.isNotEmpty()){
                netiCore?.client?.updateWeeks()
                setupAppBar(it.title, it.isIndividual)
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
            Log.e("observeWeeksInfo", it.toString())
            when (it.status){
                Status.LOADING -> {
                    binding.swiperefresh.isRefreshing = true
                }
                Status.ERROR -> {
                    binding.swiperefresh.isRefreshing = false
                    (activity as MainActivity).createNotificationSnackbar("Что-то пошло не так", "Ошибка загрузки учебных недель", "error")

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
        currentWeek = weeks?.indexOfFirst { it.isCurrent == true }
        adapter?.setWeeks(weeks)

        binding.viewPager.offscreenPageLimit = 4
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = weeks?.get(position)?.weekTitle
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val week = netiCore?.client?.scheduleViewModel?.weeksLiveData?.value?.data?.find { week -> week.weekQuery == (tab.position + 1).toString() }
                if (week != null){
                    netiCore?.client?.scheduleViewModel?.loadScheduleWeek(week)
                }
                if (tab.position != currentWeek && currentWeek != null){
                    binding?.todayWeekFab?.show()
                } else{
                    binding?.todayWeekFab?.hide()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        if (currentWeek != null){
            Handler(Looper.getMainLooper()).postDelayed({
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(currentWeek!!), true)
                val week = netiCore?.client?.scheduleViewModel?.weeksLiveData?.value?.data?.find { week -> week.isCurrent == true }
                if (week != null){
                    netiCore?.client?.scheduleViewModel?.loadScheduleWeek(week)
                }
            }, 300)

            binding.todayWeekFab.setOnClickListener {
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(currentWeek!!), true)
            }
        }
    }

    fun setupAppBar(title: String = "", isIndividual: Boolean){
        CoroutineScope(Dispatchers.Main).launch {
            if (isIndividual){
                binding.toolbarCollapse.subtitle = "Индивидуальное расписание"
                binding.toolbarCollapse.title = title
            }else{
                binding.toolbarCollapse.subtitle= "Расписание занятий"
                binding.toolbarCollapse.title = title
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