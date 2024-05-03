package com.dertefter.ficus.fragments.profile

import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentProfileDialogBinding
import com.dertefter.ficus.ficus_old.ProfileData
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.AuthorizationState
import com.dertefter.neticore.data.Status
import kotlinx.coroutines.launch


class ProfileDialogFragment : DialogFragment(R.layout.fragment_profile_dialog) {

    var netiCore: NETICore? = null

    lateinit var binding: FragmentProfileDialogBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        netiCore = (activity?.application as Ficus).netiCore

        binding = FragmentProfileDialogBinding.bind(view)

        observeAuthState()
        observePhoto()
        setupButtons()


    }

    fun setupButtons(){
        binding.settingsButton.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
            dismiss()
        }
        binding.helpButton.setOnClickListener {
            findNavController().navigate(R.id.aboutFragment)
            dismiss()
        }
    }

    fun showAuthCard(v: Boolean){
        if (v){
            binding?.authCard?.visibility = View.VISIBLE
            binding?.buttonLogin?.setOnClickListener {
                (parentFragment as ProfileFragment).navigateToAuthFragment()
                dismiss()
            }
        }else{
            binding?.authCard?.visibility = View.GONE
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    fun observeAuthState(){
        lifecycleScope.launch {
            netiCore?.client?.authorizationStateViewModel?.uiState?.collect {
                Log.e("ProfileDialogFragment", "observeAuthState: $it")
                when(it){
                    AuthorizationState.AUTHORIZED -> {
                        observeUserInfo()
                        showAuthCard(false)
                        netiCore?.getUserInfo()
                    }
                    AuthorizationState.AUTHORIZED_WITH_ERROR -> {
                        showAuthCard(false)
                        binding?.profileView?.visibility = View.VISIBLE
                        binding?.shimmer?.visibility = View.GONE
                    }
                    AuthorizationState.UNAUTHORIZED -> {
                        showAuthCard(true)
                        binding?.profileView?.visibility = View.VISIBLE
                        binding?.shimmer?.visibility = View.GONE
                    }
                    AuthorizationState.LOADING -> {
                        showAuthCard(false)
                        binding?.profileView?.visibility = View.GONE
                        binding?.shimmer?.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    fun observePhoto(){
        netiCore?.client?.userInfoViewModel?.userPhotoLiveData?.observe(viewLifecycleOwner){
            when (it.status){
                Status.LOADING -> {
                    binding.personAvatarPlaceholder.visibility = View.GONE
                }
                Status.ERROR -> {
                    binding.personAvatarPlaceholder.visibility = View.GONE
                }
                Status.SUCCESS -> {
                    val path = it.data
                    if (path != null){
                        binding.personAvatarPlaceholder.visibility = View.VISIBLE
                        val options = BitmapFactory.Options()
                        options.inSampleSize = 1
                        val b = BitmapFactory.decodeFile(path,
                            options
                        )
                        binding.personAvatarPlaceholder.setImageBitmap(b)
                    }
                }
            }
        }
    }


    fun observeUserInfo(){
        netiCore?.client?.userInfoViewModel?.userInfoLiveData?.observe(viewLifecycleOwner){
            when (it.status){
                Status.LOADING -> {
                    binding?.profileView?.visibility = View.GONE
                    binding?.shimmer?.visibility = View.VISIBLE
                    Log.e("ProfileDialogFragment", "observeUserInfo: LOADING")
                }
                Status.ERROR -> { Log.e("ProfileDialogFragment", "observeUserInfo: ERROR")
                    binding?.profileView?.visibility = View.GONE
                    binding?.shimmer?.visibility = View.VISIBLE
                }
                Status.SUCCESS -> {
                    binding?.profileView?.visibility = View.VISIBLE
                    binding?.shimmer?.visibility = View.GONE
                    val user = it.data
                    if (user != null){
                        binding?.name?.text = user.name
                        binding?.mail?.visibility = View.VISIBLE

                    }
                    binding.profileView.setOnClickListener {
                        val intent = Intent(requireContext(), ProfileData::class.java)
                        startActivity(intent)
                    }
                }
            }
        }
    }

}