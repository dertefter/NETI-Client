package com.dertefter.ficus.fragments.messages

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentMessagesBinding
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.AuthorizationState
import com.dertefter.neticore.data.messages.SenderPerson
import com.google.android.material.color.MaterialColors
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch


class MessagesFragment : Fragment(R.layout.fragment_messages) {

    var netiCore: NETICore? = null

    lateinit var binding: FragmentMessagesBinding

    var adapter: MessagesViewPagerAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore

        binding = FragmentMessagesBinding.bind(view)

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        binding.appBarLayout.setStatusBarForegroundColor(
            MaterialColors.getColor(binding.appBarLayout, com.google.android.material.R.attr.colorSurface))
        observeAuthState()




    }

    fun setupViewPager(tabList: List<String>?) {
        adapter = MessagesViewPagerAdapter(childFragmentManager, lifecycle)
        adapter?.setTabList(tabList)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabList?.get(position)
        }.attach()
    }


    fun observeAuthState(){
        lifecycleScope.launch {
            netiCore?.client?.authorizationStateViewModel?.uiState?.collect {
                when(it){
                    AuthorizationState.AUTHORIZED -> {
                        binding.authCard.visibility = View.GONE
                        binding.authErrorCard.visibility = View.GONE
                        val tabList = listOf("От преподавателей и служб", "Прочее")
                        setupViewPager(tabList)

                        binding.swiperefresh.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
                            netiCore?.client?.messagesViewModel?.updateSenderList(0)
                            netiCore?.client?.messagesViewModel?.updateSenderList(1)
                        })


                    }
                    AuthorizationState.AUTHORIZED_WITH_ERROR -> {
                        binding.authCard.visibility = View.GONE
                        binding.authErrorCard.visibility = View.VISIBLE
                        binding.buttonLogOut.setOnClickListener {
                            netiCore?.logOut()
                        }
                        binding.buttonTryAgain.setOnClickListener {
                            netiCore?.checkAuthorization()
                        }
                        setupViewPager(null)
                    }
                    AuthorizationState.UNAUTHORIZED -> {
                        binding.authErrorCard.visibility = View.GONE
                        binding.authCard.visibility = View.VISIBLE
                        binding.buttonLogin.setOnClickListener {
                            val action = MessagesFragmentDirections.actionMessagesFragmentToAuthFragment()
                            findNavController().navigate(action)
                        }
                        setupViewPager(null)
                    }
                    AuthorizationState.LOADING -> {
                        binding.authCard.visibility = View.GONE
                        binding.authErrorCard.visibility = View.GONE
                        setupViewPager(null)
                    }
                }
            }
        }
    }

    fun openChat(v: View, chatItem: SenderPerson) {
        val action = MessagesFragmentDirections.actionMessagesFragmentToMessagesChatViewFragment(chatItem)
        val extras = FragmentNavigatorExtras(
                v to chatItem.name,
        )
        findNavController().navigate(action, extras)
    }

}