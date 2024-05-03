package com.dertefter.ficus.fragments.sessia_results

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.findNavController
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.MainActivity
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentScheduleBinding
import com.dertefter.ficus.databinding.FragmentSessiaResultsBinding
import com.dertefter.ficus.fragments.dispace.di_cources.course_view.ModalBottomSheet
import com.dertefter.ficus.fragments.sessia_results.share_sessia.ShareSessiaBottomSheet
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.Status
import com.dertefter.neticore.data.schedule.Week
import com.dertefter.neticore.data.sessia_results.SessiaResults
import com.google.android.material.color.MaterialColors
import com.google.android.material.tabs.TabLayoutMediator

class SessiaResultsFragment : Fragment(R.layout.fragment_sessia_results) {

    var netiCore: NETICore? = null

    lateinit var binding: FragmentSessiaResultsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore

        binding = FragmentSessiaResultsBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.appBarLayout.setStatusBarForegroundColor(MaterialColors.getColor(binding.appBarLayout, com.google.android.material.R.attr.colorSurface))
        netiCore?.client?.sessiaViewModel?.getLink()
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId){
                R.id.share -> {
                    val modalBottomSheet = ShareSessiaBottomSheet()
                    modalBottomSheet.show(parentFragmentManager, ShareSessiaBottomSheet.TAG)
                    true
                }
                else -> false
            }
        }


        netiCore?.client?.sessiaViewModel?.updateSessiaResults()

        observeSessiaInfo()
    }

    fun observeSessiaInfo(){
        netiCore?.client?.sessiaViewModel?.sessiaResultsLiveData?.observe(viewLifecycleOwner){
            when (it.status){
                Status.LOADING -> {
                    setupViewPager(null)
                    binding.progressBar.visibility = View.VISIBLE
                }
                Status.ERROR -> {
                    setupViewPager(null)
                    binding.progressBar.visibility = View.INVISIBLE
                    (activity as MainActivity).createNotificationSnackbar("Ошибка загрузки результатов сессии!", "Проверьте подключение к интернету", "error")

                }
                Status.SUCCESS -> {
                    Log.e("SessiaResultsFragment", "observeSessiaInfo: ${it.data}")
                    binding.progressBar.visibility = View.INVISIBLE
                    val weeks = it.data
                    setupViewPager(weeks)
                }

            }
        }
    }

    fun setupViewPager(items: List<SessiaResults>?) {
        val adapter = SessiaPagerAdapter(childFragmentManager, lifecycle)
        adapter.setItems(items)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = items?.get(position)?.title
        }.attach()
        binding.tabLayout.selectTab(binding.tabLayout.getTabAt(binding.tabLayout.tabCount - 1))

    }

}