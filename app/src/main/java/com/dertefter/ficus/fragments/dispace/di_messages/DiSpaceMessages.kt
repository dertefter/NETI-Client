package com.dertefter.ficus.fragments.dispace.di_messages

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentDiSpaceMessagesBinding
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.Status
import com.dertefter.neticore.data.dispace.di_messages.DiSenderPerson
import com.google.android.material.color.MaterialColors


class DiSpaceMessages : Fragment(R.layout.fragment_di_space_messages) {

    var netiCore: NETICore? = null

    lateinit var binding: FragmentDiSpaceMessagesBinding

    var adapter: DiChatListAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore
        binding = FragmentDiSpaceMessagesBinding.bind(view)
        setupRecyclerView()
        observeChatList()

        binding.appBarLayout.setStatusBarForegroundColor(MaterialColors.getColor(binding.appBarLayout, com.google.android.material.R.attr.colorSurface))



        if ( netiCore?.diSpaceClient?.diMessagesViewModel?.chatListLiveData?.value?.data.isNullOrEmpty()){
            netiCore?.diSpaceClient?.diMessagesViewModel?.updateSenderList()
        }
    }

    fun setupRecyclerView(){
        adapter = DiChatListAdapter(this)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }


    fun openChat(person: DiSenderPerson) {
        val action = DiSpaceMessagesDirections.actionDiSpaceMessagesToDiChatViewFragment(person)
        findNavController().navigate(action)
    }

    fun observeChatList(){
        netiCore?.diSpaceClient?.diMessagesViewModel?.chatListLiveData?.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    binding.shimmer.visibility = View.GONE
                    if (it.data != null) {
                        adapter?.setChatList(it.data)
                    }
                }

                Status.LOADING -> {
                    binding.shimmer.visibility = View.VISIBLE
                }

                Status.ERROR -> {//TODO
                    binding.shimmer.visibility = View.GONE
                }
            }
        }
    }
}