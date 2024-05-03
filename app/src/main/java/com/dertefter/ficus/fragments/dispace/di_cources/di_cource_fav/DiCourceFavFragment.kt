package com.dertefter.ficus.fragments.dispace.di_cources.di_cource_search

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentDiCourcesFavBinding
import com.dertefter.ficus.fragments.dispace.di_cources.DiSpaceCources
import com.dertefter.ficus.fragments.dispace.di_cources.di_cource_fav.DiCourseListFavAdapter
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.Status
import com.dertefter.neticore.data.dispace.di_cources.DiCourse

class DiCourceFavFragment : Fragment(R.layout.fragment_di_cources_fav) {

    var netiCore: NETICore? = null

    lateinit var binding: FragmentDiCourcesFavBinding

    lateinit var adapter: DiCourseListFavAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore
        binding = FragmentDiCourcesFavBinding.bind(view)
        setupRecyclerView()
        observeFavCouseList()
        (parentFragment as DiSpaceCources).binding.appBarLayout.liftOnScrollTargetViewId = binding.recyclerView.id
        netiCore?.diSpaceClient?.diCourcesViewModel?.updateFav(1)
    }

    override fun onResume() {
        super.onResume()

    }

    fun setupRecyclerView(){
        adapter = DiCourseListFavAdapter(this)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }

    fun observeFavCouseList(){
        netiCore?.diSpaceClient?.diCourcesViewModel?.favCourseListLiveData?.observe(viewLifecycleOwner) {
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

    fun openCourse(course: DiCourse?) {
        (parentFragment as DiSpaceCources).openCourse(course)
    }

    fun removeFav(currentItem: DiCourse) {
        netiCore?.diSpaceClient?.diCourcesViewModel?.removeFav(currentItem.id.toString())
    }

    fun addFav(currentItem: DiCourse) {
        netiCore?.diSpaceClient?.diCourcesViewModel?.addFav(currentItem.id.toString())
    }

}