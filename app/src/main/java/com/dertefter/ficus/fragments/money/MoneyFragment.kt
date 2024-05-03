package com.dertefter.ficus.fragments.money

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentMessagesBinding
import com.dertefter.ficus.databinding.FragmentMoneyBinding
import com.dertefter.ficus.fragments.messages.MessagesFragment
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.AuthorizationState
import com.dertefter.neticore.data.Status
import com.dertefter.neticore.data.messages.SenderPerson
import com.google.android.material.color.MaterialColors
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch


class MoneyFragment : Fragment(R.layout.fragment_money) {

    var netiCore: NETICore? = null

    lateinit var binding: FragmentMoneyBinding

    var adapter: MoneyViewPagerAdapter? = null



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore

        binding = FragmentMoneyBinding.bind(view)

        binding.appBarLayout.setStatusBarForegroundColor(
            MaterialColors.getColor(binding.appBarLayout, com.google.android.material.R.attr.colorSurface))

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        observeYearInfo()
        netiCore?.client?.moneyViewModel?.updateYears()
    }

    fun observeYearInfo(){
        netiCore?.client?.moneyViewModel?.yearsLiveData?.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    binding.loading.visibility = View.VISIBLE
                }

                Status.SUCCESS -> {
                    binding.loading.visibility = View.GONE
                    Log.e("sssssssssss", it.data.toString())
                    setupViewPager(it.data)
                }

                Status.ERROR -> {
                    binding.loading.visibility = View.GONE
                }
            }
        }
    }

    fun setupViewPager(tabList: List<String>?) {
        adapter = MoneyViewPagerAdapter(childFragmentManager, lifecycle)
        adapter?.setTabList(tabList)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabList?.get(position)
        }.attach()
    }


}