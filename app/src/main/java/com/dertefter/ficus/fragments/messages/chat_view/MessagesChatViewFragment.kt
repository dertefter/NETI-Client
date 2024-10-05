package com.dertefter.ficus.fragments.messages.chat_view

import android.animation.ObjectAnimator
import android.animation.TimeInterpolator
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionInflater
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentMessagesChatViewBinding
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.Person
import com.dertefter.neticore.data.messages.Message
import com.google.android.material.color.MaterialColors
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.squareup.picasso.Picasso


class MessagesChatViewFragment : Fragment(R.layout.fragment_messages_chat_view) {

    var netiCore: NETICore? = null

    lateinit var binding: FragmentMessagesChatViewBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore

        val chatItem = MessagesChatViewFragmentArgs.fromBundle(requireArguments()).chatItem

        binding = FragmentMessagesChatViewBinding.bind(view)

        val timeInterpolator : TimeInterpolator =
            android.view.animation.PathInterpolator(0.4f, 0f, 0.2f, 1f)
        enterTransition =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
                ?.apply {
                    interpolator = timeInterpolator
                }
        sharedElementEnterTransition =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
                ?.apply {
                    interpolator = timeInterpolator
                }
        binding.personCard.transitionName = chatItem.name
        binding.recyclerView.alpha = 0f
        ObjectAnimator.ofFloat(binding.recyclerView, "alpha", 1f).apply {
            startDelay = 300
            duration = 200
            start()
        }

        binding.personName.text = chatItem.name
        val liveData = MutableLiveData<Person?>()
        liveData.observeForever {
            if (it != null){
                val p = it
                if (!it.pic.isNullOrEmpty()){
                    Picasso.get().load(it.pic).resize(200,200).centerCrop().into(binding.personAvatarPlaceholder)
                    ObjectAnimator.ofFloat(binding.personAvatarPlaceholder, "alpha", 0f, 1f).setDuration(100).start()
                }
                binding.mail.text = it.mail
                ObjectAnimator.ofFloat(binding.mail, "alpha", 0f, 1f).setDuration(100).start()
                binding.personCard.setOnClickListener {
                    findNavController().navigate(R.id.personPageFragment, Bundle().apply {
                        putString("personName", null)
                        putString("personLink", p.site)
                    })
                }
            }
        }

        netiCore!!.personHelper!!.retrivePersonByName(chatItem.name, liveData)

        val messages = chatItem.messages
        setupRecyclerView(messages)
        setupAppBar()
    }

    fun setupAppBar(){
        binding.topAppBarLayout.setStatusBarForegroundColor(MaterialColors.getColor(binding.topAppBarLayout, com.google.android.material.R.attr.colorSurface))

        binding.topAppBar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    fun setupRecyclerView(messages: List<Message>) {
        val adapter = MessagesListAdapter(this)
        adapter.setMessageList(messages)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val divider = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL /*or LinearLayoutManager.HORIZONTAL*/)
        binding.recyclerView.addItemDecoration(divider)
        binding.recyclerView.adapter = adapter
    }

    fun openMessage(currentItem: Message) {
        removeIsNewOnMessage(currentItem)
        val action = MessagesChatViewFragmentDirections.actionMessagesChatViewFragmentToReadMessageFragment(currentItem.mesId, currentItem.title)
        findNavController().navigate(action)

    }

    fun removeIsNewOnMessage(chatItem: Message){
        netiCore?.client?.messagesViewModel?.removeIsNewOnMessage(chatItem)
    }

}