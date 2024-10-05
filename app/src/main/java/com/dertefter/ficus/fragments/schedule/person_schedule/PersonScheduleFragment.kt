package com.dertefter.ficus.fragments.schedule.person_schedule

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.MainActivity
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentScheduleBinding
import com.dertefter.ficus.databinding.FragmentSchedulePersonBinding
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.Person
import com.dertefter.neticore.data.Status
import com.dertefter.neticore.data.schedule.Week
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.tabs.TabLayoutMediator

class PersonScheduleFragment : Fragment(R.layout.fragment_schedule_person) {

    var netiCore: NETICore? = null

    lateinit var binding: FragmentSchedulePersonBinding

    lateinit var person: Person

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore

        binding = FragmentSchedulePersonBinding.bind(view)
        binding?.appBarLayout?.statusBarForeground = MaterialShapeDrawable.createWithElevationOverlay(context)
        person = PersonScheduleFragmentArgs.fromBundle(requireArguments()).person

        observeWeeksInfo()

        binding?.toolbar?.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.toolbar.title = person.name
        binding.toolbar.subtitle = "Расписание сотрудника"

        netiCore?.client?.schedulePersonModel?.loadWeekList()
    }




    fun observeWeeksInfo(){
        netiCore?.client?.schedulePersonModel?.weeksLiveData?.observe(viewLifecycleOwner){
            when (it.status){
                Status.LOADING -> {
                }
                Status.ERROR -> {
                    setupViewPager(null)
                    binding.progressBar.visibility = View.INVISIBLE
                    (activity as MainActivity).createNotificationSnackbar("Ошибка загрузки расписания!", "Проверьте подключение к интернету", "error")

                }
                Status.SUCCESS -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    val weeks = it.data
                    setupViewPager(weeks, person.site)
                }

            }
        }
    }

    fun setupViewPager(weeks: List<Week>?, personSite: String? = null) {
        val adapter = ScheduleViewPagerPersonAdapter(childFragmentManager, lifecycle, personSite)
        adapter.setWeeks(weeks)
        Log.e("ababababa", weeks.toString())
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = weeks?.get(position)?.weekTitle
        }.attach()

    }
}