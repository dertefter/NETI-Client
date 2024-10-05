package com.dertefter.ficus.fragments.e_library

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentELibraryBinding
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.Status
import com.google.android.material.color.MaterialColors


class ELibrarySearchFragment : Fragment(R.layout.fragment_e_library) {

    var netiCore: NETICore? = null

    lateinit var binding: FragmentELibraryBinding

    var adapter: ELibrarySearchListAdapter? = null



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore

        binding = FragmentELibraryBinding.bind(view)
        adapter = ELibrarySearchListAdapter(this)
        binding.appBarLayout.setStatusBarForegroundColor(
            MaterialColors.getColor(binding.appBarLayout, com.google.android.material.R.attr.colorSurface))
        setupSearchbar()
        setupRecyclerView()
        binding.topAppBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        observeSearch()

    }

    fun setupRecyclerView(){
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 1)
    }

    fun setupSearchbar(){
        binding?.searchEditText?.doOnTextChanged { text, start, before, count ->
            val search_term = text.toString()
            netiCore?.client?.elibraryViewModel?.search(search_term)
        }
    }
    fun observeSearch(){
        netiCore?.client?.elibraryViewModel?.searchItemsLiveData?.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                    binding.errorView.visibility = View.GONE
                }

                Status.SUCCESS -> {
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.errorView.visibility = View.GONE
                    adapter?.setData(it.data)

                }

                Status.ERROR -> {
                    binding.recyclerView.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                    binding.errorView.visibility = View.VISIBLE
                }
            }
        }
    }



}