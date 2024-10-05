package com.dertefter.ficus.fragments.search_group

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentSearchGroupBinding
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.Status
import com.dertefter.neticore.data.schedule.Group
import com.google.android.material.shape.MaterialShapeDrawable

class SearchGroupFragment : Fragment(R.layout.fragment_search_group) {

    var netiCore: NETICore? = null

    lateinit var binding: FragmentSearchGroupBinding

    var adapter: GroupListAdapter? = null

    var grTitle = ""
    var grTitleTwo = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore
        binding = FragmentSearchGroupBinding.bind(view)
        adapter = GroupListAdapter(this)
        setupToolbar()
        setupRecyclerView()
        setupSearchbar()
        observeGroupList()
        observeUserInfo()
        observeGroupInfo()

        netiCore?.client?.scheduleViewModel?.loadGroupList()
    }

    fun observeGroupInfo(){
        netiCore?.client?.scheduleViewModel?.currentGroupLiveData?.observe(viewLifecycleOwner){
            if (it != null && it.title.isNotEmpty()){
                grTitle = it.title
                if (grTitleTwo == grTitle){
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }

    fun observeSavedGroups(){
        netiCore?.client?.scheduleViewModel?.savedGroupsLiveData?.observe(viewLifecycleOwner){
            if (it != null){

            }
        }
    }



    fun setGroup(group: Group){
        grTitleTwo = group.title
        netiCore?.setGroup(group)
    }

    fun setupRecyclerView(){
        binding.recyclerView.adapter = adapter
        val orientation = resources.configuration.orientation
        if (orientation == 1){
            binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        } else{
            binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 4)
        }
    }
    fun setupSearchbar(){
        binding?.searchEditText?.doOnTextChanged { text, start, before, count ->
            netiCore?.client?.scheduleViewModel?.loadGroupList(text.toString())
        }
    }

    fun setupToolbar(){
        binding?.appBarLayout?.statusBarForeground = MaterialShapeDrawable.createWithElevationOverlay(context)
        binding?.topAppBar?.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    fun observeGroupList(){
        netiCore?.client?.scheduleViewModel?.groupListLiveData?.observe(viewLifecycleOwner){
            if (it != null){
                when (it.status){
                    Status.LOADING -> {
                        binding?.progressBar?.visibility = View.VISIBLE
                        binding?.recyclerView?.visibility = View.GONE
                    }
                    Status.SUCCESS -> {
                        binding?.progressBar?.visibility = View.GONE
                        binding?.recyclerView?.visibility = View.VISIBLE
                        adapter?.setGroupList(it.data)
                    }
                    Status.ERROR -> {
                        binding?.progressBar?.visibility = View.GONE
                        binding?.recyclerView?.visibility = View.GONE
                    }
                }
            }

        }
    }

    fun observeUserInfo(){
        netiCore?.client?.userInfoViewModel?.userInfoLiveData?.observe(viewLifecycleOwner){
            if (it.status == Status.SUCCESS){
                Log.e("User SUCCESS", it.data.toString())
                if (it.data?.group != null){
                    adapter?.addIndividual(it.data?.group!!)
                }
            }
        }
    }


}