package com.dertefter.ficus.fragments.sessia_results

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dertefter.neticore.data.schedule.Week
import com.dertefter.neticore.data.sessia_results.SessiaResults

class SessiaPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :

    FragmentStateAdapter(fragmentManager, lifecycle) {
    var itemsList: List<SessiaResults>? = null

    fun setItems(items: List<SessiaResults>?){
        if (items == null){
            itemsList = listOf()
        }else{
            itemsList = items
        }
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return itemsList?.size ?: 0
    }

    override fun createFragment(position: Int): SessiaItemFragment {
        val item = itemsList?.get(position)
        val fragment = SessiaItemFragment()
        fragment.arguments = Bundle().apply {
            putString("title", item?.title)
        }
        return fragment
    }
}