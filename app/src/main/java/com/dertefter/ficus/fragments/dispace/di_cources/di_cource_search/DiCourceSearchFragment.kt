package com.dertefter.ficus.fragments.dispace.di_cources.di_cource_search

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentDiCourcesSearchBinding
import com.dertefter.ficus.fragments.dispace.di_cources.DiSpaceCources
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.Status
import com.dertefter.neticore.data.dispace.di_cources.DiCourse

class DiCourceSearchFragment : Fragment(R.layout.fragment_di_cources_search) {

    var netiCore: NETICore? = null

    lateinit var binding: FragmentDiCourcesSearchBinding

    lateinit var adapter: DiCourseListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore
        binding = FragmentDiCourcesSearchBinding.bind(view)
        setupRecyclerView()
        observeCouseList()
        (parentFragment as DiSpaceCources).binding.appBarLayout.liftOnScrollTargetViewId = binding.recyclerView.id
        netiCore?.diSpaceClient?.diCourcesViewModel?.searchCourse("", "1")
    }


    fun removeFav(currentItem: DiCourse) {
        netiCore?.diSpaceClient?.diCourcesViewModel?.removeFav(currentItem.id.toString())
    }

    fun addFav(currentItem: DiCourse) {
        netiCore?.diSpaceClient?.diCourcesViewModel?.addFav(currentItem.id.toString())
    }

    fun setupRecyclerView(){
        adapter = DiCourseListAdapter(this)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }

    fun observeCouseList(){
        netiCore?.diSpaceClient?.diCourcesViewModel?.courseListLiveData?.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    binding.shimmer.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                }

                Status.SUCCESS -> {
                    binding.shimmer.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                    val data = it.data
                    if (data != null) {
                        adapter.setCourseList(data)
                    }
                }

                Status.ERROR -> {
                    binding.shimmer.visibility = View.GONE
                    binding.recyclerView.visibility = View.GONE
                }
            }
        }
    }

    fun openCourse(currentItem: DiCourse) {
        (parentFragment as DiSpaceCources).openCourse(currentItem)
    }

}