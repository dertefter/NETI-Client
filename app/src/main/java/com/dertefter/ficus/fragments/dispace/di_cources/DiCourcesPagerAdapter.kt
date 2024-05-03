package com.dertefter.ficus.fragments.dispace.di_cources

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dertefter.ficus.fragments.dispace.di_cources.di_cource_search.DiCourceFavFragment
import com.dertefter.ficus.fragments.dispace.di_cources.di_cource_search.DiCourceSearchFragment

class DiCourcesPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :

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

    override fun createFragment(position: Int): Fragment {
        if (position == 0){
            val fragment = DiCourceSearchFragment()
            return fragment
        }else{
            val fragment = DiCourceFavFragment()
            return fragment
        }
    }
}