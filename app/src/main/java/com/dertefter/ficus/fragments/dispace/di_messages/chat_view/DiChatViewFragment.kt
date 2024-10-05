package com.dertefter.ficus.fragments.dispace.di_messages.chat_view

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentDiChatViewBinding
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.Status
import com.dertefter.neticore.data.dispace.di_messages.DiSenderPerson
import com.squareup.picasso.Picasso


class DiChatViewFragment : Fragment(R.layout.fragment_di_chat_view) {

    var netiCore: NETICore? = null

    lateinit var binding: FragmentDiChatViewBinding

    var adapter: DiMessageListAdapter? = null

    lateinit var person : DiSenderPerson

    var page = 1

    var remain = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        enterTransition = inflater.inflateTransition(R.transition.slide)
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore
        person = DiChatViewFragmentArgs.fromBundle(requireArguments()).person

        binding = FragmentDiChatViewBinding.bind(view)

        binding.name.text = "${person.surname} ${person.name} ${person.patronymic}"

        Picasso.get().load("https://dispace.edu.nstu.ru/files/images/photos/b_IMG_"+person.photo).resize(200,200).centerCrop().into(binding.personAvatarPlaceholder)

        setupRecyclerView()

        getMessages()

        observeMessageList()

        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.textField.doOnTextChanged { text, start, before, count ->
            binding.sendButton.isEnabled = text?.length!! > 0
        }
        binding.sendButton.setOnClickListener {
            sendMessage()
            binding.textField.setText("")
        }





    }

    fun getMessages(){
        Log.e("pageee", page.toString())
        netiCore?.diSpaceClient?.diMessagesViewModel?.updateMessageList(page, person.companion_id)
    }

    fun sendMessage(){
        netiCore?.diSpaceClient?.diMessagesViewModel?.sendmes(binding.textField.text.toString(), person.companion_id)
    }


    fun setupRecyclerView(){
        adapter = DiMessageListAdapter(this, person.companion_id)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE){
                    ObjectAnimator.ofFloat(binding.dateContainer, "alpha", 1f, 0f).apply {
                        duration = 169
                        startDelay = 180
                        start()
                    }
                }else if (newState == RecyclerView.SCROLL_AXIS_VERTICAL){
                    ObjectAnimator.ofFloat(binding.dateContainer, "alpha", 0f, 1f).apply {
                        duration = 169
                        start()
                    }
                    val vpos = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    val date = adapter?.getItem(vpos)?.date
                    binding.dateTextView.text = date
                }
            }
        })
        val itemDecorator = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        itemDecorator.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.divider)!!)
        binding.recyclerView.addItemDecoration(itemDecorator)
    }




    fun observeMessageList(){
        netiCore?.diSpaceClient?.diMessagesViewModel?.messageListLiveData?.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    if (it.data != null) {
                        remain = it.data!!.remain
                        adapter?.remain = remain
                        if (page != 1){
                            adapter?.addMessageList(it.data!!.messages, binding.recyclerView)
                        }else{
                            adapter?.setMessageList(it.data!!.messages)
                        }
                        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                                val canScrollToTop = recyclerView.canScrollVertically(-1)
                                if (!canScrollToTop){
                                    if (remain > 0){
                                        page++
                                        getMessages()
                                    }

                                }
                            }
                        })

                        if (page == 1){
                            binding.recyclerView.scrollToPosition(adapter!!.itemCount - 1)
                        }
                    }

                }

                Status.LOADING -> {
                }

                Status.ERROR -> {

                }
            }
        }
    }
}