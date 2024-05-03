package com.dertefter.ficus.fragments.search_person

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.MainActivity
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentScheduleBinding
import com.dertefter.ficus.databinding.FragmentSearchGroupBinding
import com.dertefter.ficus.databinding.FragmentSearchPersonBinding
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.Person
import com.dertefter.neticore.data.Status
import com.dertefter.neticore.data.schedule.Group
import com.dertefter.neticore.data.schedule.Week
import com.google.android.material.color.MaterialColors
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.tabs.TabLayoutMediator

class SearchPersonFragment : Fragment(R.layout.fragment_search_person) {

    var netiCore: NETICore? = null

    lateinit var binding: FragmentSearchPersonBinding

    var adapter: PersonListAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore
        binding = FragmentSearchPersonBinding.bind(view)
        adapter = PersonListAdapter(this)
        setupToolbar()
        setupRecyclerView()
        setupSearchbar()
        observePersonList()

        netiCore?.client?.searchPersonViewModel?.updatePersonList("")
    }


    fun setupRecyclerView(){
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 1)
        val insetTypes =
            WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.systemBars()
        val insets = ViewCompat.getRootWindowInsets(activity?.window!!.decorView)
        val bottom = insets?.getInsets(insetTypes)?.bottom
        binding.recyclerView.updatePadding(bottom = bottom!!)
    }

    fun setupSearchbar(){
        binding?.searchEditText?.doOnTextChanged { text, start, before, count ->
            netiCore?.client?.searchPersonViewModel?.updatePersonList(text.toString())
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
                    binding?.progressBar?.visibility = View.GONE
                    binding?.recyclerView?.visibility = View.VISIBLE
                    adapter?.setPersonList(it.data)
                }
                Status.ERROR -> {
                    binding?.progressBar?.visibility = View.GONE
                    binding?.recyclerView?.visibility = View.GONE
                }
            }
        }
    }


    fun openPerson(currentItem: Person) {
        val action = SearchPersonFragmentDirections.actionSearchPersonFragmentToPersonPageFragment(currentItem)
        findNavController().navigate(action)
    }


}