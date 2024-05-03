package com.dertefter.ficus.fragments.dispace.di_cources

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentDiSpaceCourcesBinding
import com.dertefter.ficus.fragments.messages.MessagesViewPagerAdapter
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.dispace.di_cources.DiCourse
import com.google.android.material.color.MaterialColors
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class DiSpaceCources : Fragment(R.layout.fragment_di_space_cources) {

       lateinit var binding: FragmentDiSpaceCourcesBinding

       lateinit var adapter: DiCourcesPagerAdapter

       var netiCore: NETICore? = null

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            binding = FragmentDiSpaceCourcesBinding.bind(view)
            netiCore = (activity?.application as Ficus).netiCore

            binding.appBarLayout.setStatusBarForegroundColor(
                MaterialColors.getColor(binding.appBarLayout, com.google.android.material.R.attr.colorSurface))


            val tabList = listOf("Поиск курсов", "Избранное")
            setupViewPager(tabList)

            binding.searchEditText.doOnTextChanged { text, start, before, count ->
                netiCore?.diSpaceClient?.diCourcesViewModel?.searchCourse(text.toString(), "1")
            }
        }

    fun setupViewPager(tabList: List<String>?) {
        adapter = DiCourcesPagerAdapter(childFragmentManager, lifecycle)
        adapter?.setTabList(tabList)
        binding.viewPager.offscreenPageLimit = 1
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabList?.get(position)
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 0){
                    binding.searchBar.visibility = View.VISIBLE
                    ObjectAnimator.ofFloat(binding.searchBar, "alpha", 0.5f, 1f).apply {
                        duration = 160
                        start()
                    }
                }else{
                    binding.searchBar.visibility = View.GONE
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // Do something when tab is unselected
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // Do something when tab is reselected
            }
        })

    }

    fun openCourse(course: DiCourse?) {
        val action = DiSpaceCourcesDirections.actionDiSpaceCourcesToCourseViewFragment(course!!)
        findNavController().navigate(action)
    }


}