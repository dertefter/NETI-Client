package com.dertefter.ficus.fragments.search_person

import android.os.Bundle
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentSearchPersonBinding
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.Person
import com.dertefter.neticore.data.Status
import com.google.android.material.shape.MaterialShapeDrawable

class SearchPersonFragment : Fragment(R.layout.fragment_search_person) {

    var netiCore: NETICore? = null

    lateinit var binding: FragmentSearchPersonBinding

    var adapter: PersonListAdapter? = null
    var search_term = ""
    var page = 1
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore
        binding = FragmentSearchPersonBinding.bind(view)
        adapter = PersonListAdapter(this)
        setupToolbar()
        setupRecyclerView()
        setupSearchbar()
        observePersonList()
        if (netiCore?.client?.searchPersonViewModel?.personListLiveData?.isInitialized != true){
            page = 1
            netiCore?.client?.searchPersonViewModel?.updatePersonList(search_term, page)
        }

    }


    fun setupRecyclerView(){
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 1)
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                if (lastVisibleItemPosition + 3 <= (adapter!!.getLastPosition())) {
                    page += 1
                    binding?.progressBar?.visibility = View.VISIBLE
                    netiCore?.client?.searchPersonViewModel?.updatePersonList(search_term, page)
                }
            }
        })
    }

    fun setupSearchbar(){
        binding?.searchEditText?.doOnTextChanged { text, start, before, count ->
            search_term = text.toString()
            page = 1
            netiCore?.client?.searchPersonViewModel?.updatePersonList(search_term, page)
        }
    }

    fun setupToolbar(){
        binding?.appBarLayout?.statusBarForeground = MaterialShapeDrawable.createWithElevationOverlay(context)
        binding?.topAppBar?.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    fun observePersonList(){
        netiCore?.client?.searchPersonViewModel?.personListLiveData?.observe(viewLifecycleOwner){
            when (it.status){
                Status.LOADING -> {
                    binding?.progressBar?.visibility = View.VISIBLE
                    binding?.recyclerView?.visibility = View.GONE
                }
                Status.SUCCESS -> {
                    binding?.progressBar?.visibility = View.INVISIBLE
                    binding?.recyclerView?.visibility = View.VISIBLE
                    adapter?.setPersonList(it.data)
                }
                Status.ERROR -> {
                    binding?.progressBar?.visibility = View.INVISIBLE
                    binding?.recyclerView?.visibility = View.GONE
                }
            }
        }
    }


    fun openPerson(currentItem: Person) {
        val action = SearchPersonFragmentDirections.actionSearchPersonFragmentToPersonPageFragment(personLink = currentItem.site, personName = null)
        findNavController().navigate(action)
    }


}