package com.dertefter.ficus.fragments.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.MainActivity
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentSettingsBinding
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.AuthorizationState
import com.dertefter.neticore.data.Status
import com.dertefter.neticore.local.AppPreferences
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.MaterialShapeDrawable
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
        binding.switchSchedule.setOnCheckedChangeListener { buttonView, isChecked ->
            setSchedule(isChecked)
        }
        observeAuthState()

    }

    fun initSettings(){
        binding.switchMonet.isChecked = appPreferences.monet != false
        binding.switchMonetNews.isChecked = appPreferences.monet_news != false
        binding.switchMessagesAvatars.isChecked = appPreferences.messages_avatars == true
        setSchedule(appPreferences.vertical_schedule == true)
    }

    fun setSchedule(vertical_schedule: Boolean){
        appPreferences.vertical_schedule = vertical_schedule
        if (vertical_schedule){
            binding.switchSchedule.isChecked = true
        }else{
            binding.switchSchedule.isChecked = false
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