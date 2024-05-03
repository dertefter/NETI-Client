package com.dertefter.ficus.fragments.messages

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentMessagesChatListBinding
import com.dertefter.ficus.databinding.FragmentScheduleWeekBinding
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.Status
import com.dertefter.neticore.data.messages.SenderPerson

class MessagesTabFragment : Fragment(R.layout.fragment_messages_chat_list) {

    var netiCore: NETICore? = null

    lateinit var binding: FragmentMessagesChatListBinding

    var tab: Int? = null
    var adapter: ChatListAdapter? = null

    override fun onResume() {
        super.onResume()
        (parentFragment as MessagesFragment).binding?.appBarLayout?.liftOnScrollTargetViewId = binding?.recyclerView?.id!!
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore

        binding = FragmentMessagesChatListBinding.bind(view)

        tab = arguments?.getInt("tab")

        setupRecyclerView()
        Log.e("tab", netiCore?.client?.messagesViewModel?.senderListLiveData1?.value?.data.toString())
        if (netiCore?.client?.messagesViewModel?.senderListLiveData1?.value?.data == null){
            netiCore?.client?.messagesViewModel?.updateSenderList(0)
        }
        if (netiCore?.client?.messagesViewModel?.senderListLiveData2?.value?.data == null){
            netiCore?.client?.messagesViewModel?.updateSenderList(1)
        }
        observeMessages(tab!!)
    }

    private fun setupRecyclerView() {
        adapter = ChatListAdapter(this)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val itemDecorator = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        itemDecorator.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.divider)!!)
        binding.recyclerView.addItemDecoration(itemDecorator)
    }

    fun openChat(v: View, chatItem: SenderPerson) {
        (parentFragment as MessagesFragment).openChat(v, chatItem)
    }

    fun observeMessages(tab: Int){
        val vm = if (tab == 0) netiCore?.client?.messagesViewModel?.senderListLiveData1 else netiCore?.client?.messagesViewModel?.senderListLiveData2
        vm?.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    (parentFragment as MessagesFragment).binding.swiperefresh.isRefreshing = true
                    Log.e("onLoading", "onLoading")
                    binding.shimmer.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                }

                Status.SUCCESS -> {
                    (parentFragment as MessagesFragment).binding.swiperefresh.isRefreshing = false
                    binding.shimmer.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                    val senderList = it.data
                    adapter?.setChatList(senderList)
                }

                Status.ERROR -> {
                    (parentFragment as MessagesFragment).binding.swiperefresh.isRefreshing = false
                }
            }
        }
    }

}