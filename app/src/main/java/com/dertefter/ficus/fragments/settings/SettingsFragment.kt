package com.dertefter.ficus.fragments.settings

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.MainActivity
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentScheduleBinding
import com.dertefter.ficus.databinding.FragmentSearchGroupBinding
import com.dertefter.ficus.databinding.FragmentSearchPersonBinding
import com.dertefter.ficus.databinding.FragmentSettingsBinding
import com.dertefter.ficus.ficus_old.ProfileData
import com.dertefter.ficus.fragments.schedule.ScheduleFragment
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.AuthorizationState
import com.dertefter.neticore.data.Event
import com.dertefter.neticore.data.Person
import com.dertefter.neticore.data.Status
import com.dertefter.neticore.data.schedule.Group
import com.dertefter.neticore.data.schedule.Week
import com.dertefter.neticore.local.AppPreferences
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    var netiCore: NETICore? = null

    lateinit var binding: FragmentSettingsBinding


    lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        enterTransition = inflater.inflateTransition(R.transition.slide)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore
        appPreferences = AppPreferences
        binding = FragmentSettingsBinding.bind(view)
        val insetTypes =
            WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.systemBars()
        val insets = ViewCompat.getRootWindowInsets(activity?.window!!.decorView)
        val bottom = insets?.getInsets(insetTypes)?.bottom
        binding.scrollView.updatePadding(bottom = bottom!!)
        initSettings()
        setupToolbar()
        if (DynamicColors.isDynamicColorAvailable()){
            binding.switchMonet.setOnCheckedChangeListener { buttonView, isChecked ->
                if (appPreferences.monet != isChecked) {
                    appPreferences.monet = isChecked
                    (activity as MainActivity).recreate()
                }
            }


        }else{
            binding.switchMonet.isChecked = false
            binding.switchMonet.isEnabled = false
        }
        binding.switchMonetNews.setOnCheckedChangeListener { buttonView, isChecked ->
            appPreferences.monet_news = isChecked
        }

        binding.switchMessagesAvatars.setOnCheckedChangeListener { buttonView, isChecked ->
            appPreferences.messages_avatars = isChecked
        }
        binding.scheduleWithTabs.setOnClickListener {
            setSchedule(true)
        }
        binding.scheduleWithoutTabs.setOnClickListener {
            setSchedule(false)
        }
        observeAuthState()

    }

    fun initSettings(){
        binding.switchMonet.isChecked = appPreferences.monet != false
        binding.switchMonetNews.isChecked = appPreferences.monet_news != false
        binding.switchMessagesAvatars.isChecked = appPreferences.messages_avatars == true
        setSchedule(appPreferences.compact_schedule == true)
    }

    fun setSchedule(withTabsDays: Boolean){
        appPreferences.compact_schedule = withTabsDays
        if (withTabsDays){
            binding.scheduleWithTabs.foreground.setTint(MaterialColors.getColor(binding.scheduleWithTabs, com.google.android.material.R.attr.colorOnPrimaryContainer))
            binding.scheduleWithTabs.setCardBackgroundColor(MaterialColors.getColor(binding.scheduleWithTabs, com.google.android.material.R.attr.colorPrimaryContainer))
            ObjectAnimator.ofFloat(binding.scheduleWithTabs, "scaleX", 1f).apply {
                duration = 200
                start()
            }
            ObjectAnimator.ofFloat(binding.scheduleWithTabs, "scaleY", 1f).apply {
                duration = 200
                start()
            }
            binding.scheduleWithoutTabs.foreground.setTint(MaterialColors.getColor(binding.scheduleWithoutTabs, com.google.android.material.R.attr.colorSecondary))
            binding.scheduleWithoutTabs.setCardBackgroundColor(MaterialColors.getColor(binding.scheduleWithoutTabs, com.google.android.material.R.attr.colorSurface))
            ObjectAnimator.ofFloat(binding.scheduleWithoutTabs, "scaleX", 0.9f).apply {
                duration = 200
                start()
            }
            ObjectAnimator.ofFloat(binding.scheduleWithoutTabs, "scaleY", 0.9f).apply {
                duration = 200
                start()
            }
        }else{
            binding.scheduleWithoutTabs.foreground.setTint(MaterialColors.getColor(binding.scheduleWithoutTabs, com.google.android.material.R.attr.colorOnPrimaryContainer))
            binding.scheduleWithoutTabs.setCardBackgroundColor(MaterialColors.getColor(binding.scheduleWithoutTabs, com.google.android.material.R.attr.colorPrimaryContainer))
            ObjectAnimator.ofFloat(binding.scheduleWithoutTabs, "scaleX", 1f).apply {
                duration = 200
                start()
            }
            ObjectAnimator.ofFloat(binding.scheduleWithoutTabs, "scaleY", 1f).apply {
                duration = 200
                start()
            }
            binding.scheduleWithTabs.foreground.setTint(MaterialColors.getColor(binding.scheduleWithTabs, com.google.android.material.R.attr.colorSecondary))
            binding.scheduleWithTabs.setCardBackgroundColor(MaterialColors.getColor(binding.scheduleWithTabs, com.google.android.material.R.attr.colorSurface))
            ObjectAnimator.ofFloat(binding.scheduleWithTabs, "scaleX", 0.9f).apply {
                duration = 200
                start()
            }
            ObjectAnimator.ofFloat(binding.scheduleWithTabs, "scaleY", 0.9f).apply {
                duration = 200
                start()
            }

        }
    }

    fun setupToolbar(){
        binding?.appBarLayout?.statusBarForeground = MaterialShapeDrawable.createWithElevationOverlay(context)
        binding?.toolbar?.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    fun observeAuthState(){
        lifecycleScope.launch {
            netiCore?.client?.authorizationStateViewModel?.uiState?.collect {
                when(it){
                    AuthorizationState.AUTHORIZED -> {
                        binding.accountLoginOrLogoutButton.visibility = View.VISIBLE
                        binding.accountInfoTextView.text = ""
                        binding.accountEmail.text = ""
                        binding.accountLoginOrLogoutButton.text = "Выйти"
                        binding.accountLoginOrLogoutButton.setOnClickListener {
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Выход из аккаунта")
                                .setMessage("Вы уверены, что хотите выйти из аккаунта?")
                                .setNeutralButton("Отмена") { dialog, which ->
                                    dialog.dismiss()
                                }
                                .setPositiveButton("Выйти из аккаунта") { dialog, which ->
                                    netiCore?.logOut()
                                }
                                .show()
                        }
                        observeUserInfo()
                    }
                    AuthorizationState.AUTHORIZED_WITH_ERROR -> {
                        binding.accountLoginOrLogoutButton.visibility = View.VISIBLE
                        binding.accountInfoTextView.text = "Гость"
                        binding.accountEmail.text = "Ошибка авторизации"
                        binding.accountLoginOrLogoutButton.text = "Повторить попытку"
                        binding?.accountLoginOrLogoutButton?.setOnClickListener {
                            netiCore?.checkAuthorization()
                        }
                    }
                    AuthorizationState.UNAUTHORIZED -> {
                        binding.accountLoginOrLogoutButton.visibility = View.VISIBLE
                        binding.accountInfoTextView.text = "Гость"
                        binding.accountEmail.text = "Вы не авторизованы"
                        binding.accountLoginOrLogoutButton.text = "Авторизация"
                        binding.accountLoginOrLogoutButton.setOnClickListener {
                            findNavController().navigate(R.id.authFragment)
                        }
                    }
                    AuthorizationState.LOADING -> {
                        binding.accountInfoTextView.text = "Обновляем данные..."
                        binding.accountEmail.text = ""
                        binding.accountLoginOrLogoutButton.visibility = View.GONE
                        binding.accountLoginOrLogoutButton.setOnClickListener {
                        }
                    }
                }
            }
        }
    }

    fun observeUserInfo(){
        netiCore?.client?.userInfoViewModel?.userInfoLiveData?.observe(viewLifecycleOwner){
            when (it.status){
                Status.LOADING -> {
                    binding.accountInfoTextView.text = ""
                    binding.accountEmail.text = ""
                }
                Status.ERROR -> {
                    binding.accountInfoTextView.text = ""
                    binding.accountEmail.text = ""
                }
                Status.SUCCESS -> {
                    val user = it.data
                    if (user != null){
                        binding.accountInfoTextView.text = user.name
                        binding.accountEmail.text = AppPreferences.login
                    }
                }
            }
        }
    }



}