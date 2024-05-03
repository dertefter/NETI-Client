package com.dertefter.ficus.fragments.messages.read_message

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.ImageGetter
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentReadMessageBinding
import com.dertefter.ficus.databinding.FragmentReadNewsBinding
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.Status
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso


class ReadMessageFragment : Fragment(R.layout.fragment_read_message) {
    var netiCore: NETICore? = null

    lateinit var binding: FragmentReadMessageBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore

        binding = FragmentReadMessageBinding.bind(view)

        binding.title.text = arguments?.getString("title")

        observeMessageContent()

        setupAppBarButtons()
        setupFab()

        netiCore?.client?.messagesViewModel?.readMessage(mesId = arguments?.getString("mesId")!!)

    }


    fun setupFab(){
        binding.scrollUpFab.hide()
        binding.nestedScrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY > 0){
                binding.scrollUpFab.show()
            }else{
                binding.scrollUpFab.hide()
            }
        }
        binding.scrollUpFab.setOnClickListener {
            binding?.nestedScrollView?.smoothScrollTo(0,0)
            binding?.appBarLayout?.setExpanded(true)
        }
    }

    fun setupAppBarButtons(){
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.deleteMessageButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setMessage("Вы уверены, что хотите удалить это сообщение?")
                .setPositiveButton("Удалить") { dialog, which ->
                    observeDeleteMessage()
                    netiCore?.client?.messagesViewModel?.deleteMessage(arguments?.getString("mesId")!!)

                }
                .setNegativeButton("Отмена") { dialog, which ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    fun observeDeleteMessage(){
        netiCore?.client?.messagesViewModel?.deleteMessageTaskLiveData?.observe(viewLifecycleOwner) {
            when (it.status){
                Status.SUCCESS -> {
                    findNavController().navigate(R.id.messagesFragment)
                }
                Status.ERROR -> {
                    Toast.makeText(requireContext(), "Не удалось удалить сообщение", Toast.LENGTH_SHORT).show()
                }
                Status.LOADING -> {
                    //todo
                }
            }
        }
    }

    fun observeMessageContent(){
        netiCore?.client?.messagesViewModel?.readMessagesLiveData?.observe(viewLifecycleOwner) {
            when (it.status){
                Status.SUCCESS -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.contentView.visibility = View.VISIBLE
                    binding.errorView.visibility = View.GONE
                    if (!it.data.isNullOrEmpty()){
                        displayHtml(it.data.toString(), binding.contentView)
                        ObjectAnimator.ofFloat(binding.contentView, "alpha", 0f, 1f).apply {
                            duration = 250
                            start()
                        }
                    }
                }
                Status.ERROR -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.contentView.visibility = View.GONE
                    binding.errorView.visibility = View.VISIBLE
                    binding.retryButton.setOnClickListener {
                        netiCore?.client?.messagesViewModel?.readMessage(mesId = arguments?.getString("mesId")!!)
                    }

                }
                Status.LOADING -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.contentView.visibility = View.GONE
                    binding.errorView.visibility = View.GONE
                }
            }
        }
    }


    private fun displayHtml(html: String, textView: TextView) {

        // Creating object of ImageGetter class you just created
        val imageGetter = ImageGetter(resources, textView)

        // Using Html framework to parse html
        val styledText= HtmlCompat.fromHtml(html,
            HtmlCompat.FROM_HTML_MODE_COMPACT,
            imageGetter,null)

        // to enable image/link clicking
        textView.movementMethod = LinkMovementMethod.getInstance()

        // setting the text after formatting html and downloading and setting images
        textView.text = styledText
    }



}