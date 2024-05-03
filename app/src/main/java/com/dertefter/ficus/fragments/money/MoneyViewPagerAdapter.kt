package com.dertefter.ficus.fragments.money

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dertefter.neticore.data.schedule.Week

class MoneyViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :

    FragmentStateAdapter(fragmentManager, lifecycle) {
    private var tabList: List<String>? = null

    fun setTabList(tabs: List<String>?){
        if (tabs == null){
            tabList = listOf()
        }else{
            tabList = tabs
        }
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return tabList?.size ?: 0
    }

    override fun createFragment(position: Int): MoneyTabFragment {
        val fragment = MoneyTabFragment()
        val bundle = Bundle()
        bundle.putString("tab", tabList?.get(position))
        fragment.arguments = bundle
        return fragment
    }
}