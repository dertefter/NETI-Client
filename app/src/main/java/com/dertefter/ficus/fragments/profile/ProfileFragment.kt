package com.dertefter.ficus.fragments.profile

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentProfileBinding
import com.dertefter.ficus.ficus_old.Docs
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.AuthorizationState
import com.dertefter.neticore.data.Status
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.launch


class ProfileFragment : Fragment(R.layout.fragment_profile) {

    lateinit var binding: FragmentProfileBinding

    var netiCore: NETICore? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore
        binding = FragmentProfileBinding.bind(view)
        binding.appBarLayout.setStatusBarForegroundColor(MaterialColors.getColor(binding.appBarLayout, com.google.android.material.R.attr.colorSurface))

        binding.profileSettings.setOnClickListener {
            val dialogFragment = ProfileDialogFragment()
            dialogFragment.show(childFragmentManager, "My  Fragment")
        }

        binding.tg.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/nstumobile_dev/"))
            startActivity(browserIntent)
        }
        binding.tgHelp.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/ficus_debug/"))
            startActivity(browserIntent)
        }

        binding.searchPerson.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToSearchPersonFragment()
            findNavController().navigate(action)
        }

        binding.elibrary?.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToELibrarySearchFragment()
            findNavController().navigate(action)
        }

        binding.docs.setOnClickListener {
            val intent = Intent(requireContext(), Docs::class.java)
            startActivity(intent)
        }

        binding?.score?.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToSessiaResultsFragment()
            findNavController().navigate(action)
        }

        binding.campusPass.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToCampusPassFragment()
            findNavController().navigate(action)
        }

        binding.downloads.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToFilesFragment()
            findNavController().navigate(action)
        }

        binding.money.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToMoneyFragment()
            findNavController().navigate(action)
        }
        observePhoto()
        observeAuthState()

    }


    fun enableButtons(v: Boolean){
        binding.money.isEnabled = v
        binding.downloads.isEnabled = v
        binding.campusPass.isEnabled = v
        binding.score.isEnabled = v
        binding.docs.isEnabled = v
        binding.elibrary.isEnabled = v
    }

    fun navigateToAuthFragment(){
        findNavController().navigate(R.id.action_profileFragment_to_authFragment)
    }
    fun observePhoto(){
        netiCore?.client?.userInfoViewModel?.userPhotoLiveData?.observe(viewLifecycleOwner){
            Log.e("observePhoto:", it.status.toString())
            when (it.status){
                Status.LOADING -> {
                    binding.personAvatarPlaceholder.setImageBitmap(null)
                }
                Status.ERROR -> {
                    binding.personAvatarPlaceholder.setImageBitmap(null)
                }
                Status.SUCCESS -> {
                    val path = it.data
                    if (path != null){
                        val options = BitmapFactory.Options()
                        options.inSampleSize = 1
                        val b = BitmapFactory.decodeFile(path,
                            options
                        )
                        binding.personAvatarPlaceholder.setImageBitmap(b)
                    } else {
                        binding.personAvatarPlaceholder.setImageBitmap(null)
                    }
                }
            }
        }
    }

    fun observeAuthState(){
        lifecycleScope.launch {
            netiCore?.client?.authorizationStateViewModel?.uiState?.collect {
                when(it){
                    AuthorizationState.AUTHORIZED -> {
                        enableButtons(true)
                        binding?.authErrorCard?.visibility = View.GONE
                    }
                    AuthorizationState.AUTHORIZED_WITH_ERROR -> {
                        enableButtons(false)
                        binding?.authErrorCard?.visibility = View.VISIBLE
                        binding.personAvatarPlaceholder.setImageBitmap(null)
                        binding?.buttonLogOut?.setOnClickListener {
                            netiCore?.logOut()
                        }
                        binding?.buttonTryAgain?.setOnClickListener {
                            netiCore?.checkAuthorization()
                        }
                    }
                    AuthorizationState.UNAUTHORIZED -> {
                        binding.personAvatarPlaceholder.setImageBitmap(null)
                        enableButtons(false)
                        binding.personAvatarPlaceholder.setImageBitmap(null)
                        binding?.authErrorCard?.visibility = View.GONE
                    }
                    AuthorizationState.LOADING -> {
                        binding.personAvatarPlaceholder.setImageBitmap(null)
                        enableButtons(false)
                        binding?.authErrorCard?.visibility = View.GONE
                    }
                }
            }
        }
    }

}