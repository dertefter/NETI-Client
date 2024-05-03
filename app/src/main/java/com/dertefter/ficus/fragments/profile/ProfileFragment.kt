package com.dertefter.ficus.fragments.profile

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
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
import kotlinx.coroutines.launch


class ProfileFragment : Fragment(R.layout.fragment_profile) {

    lateinit var binding: FragmentProfileBinding

    var netiCore: NETICore? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore
        binding = FragmentProfileBinding.bind(view)

        binding.profileSettings.setOnClickListener {
            val dialogFragment = ProfileDialogFragment()
            dialogFragment.show(childFragmentManager, "My  Fragment")
        }

        binding.tg.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/nstumobile_dev/"))
            startActivity(browserIntent)
        }

        binding.searchPerson.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToSearchPersonFragment()
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

        observeAuthState()
        observePhoto()
    }


    fun enableButtons(v: Boolean){
        binding.money.isEnabled = v
        binding.downloads.isEnabled = v
        binding.campusPass.isEnabled = v
        binding.score.isEnabled = v
        binding.docs.isEnabled = v
    }

    fun navigateToAuthFragment(){
        findNavController().navigate(R.id.action_profileFragment_to_authFragment)
    }
    fun observePhoto(){

        val random_int = (0..4).random()
        when(random_int){
            0 -> binding.backgroundAnim.setImageResource(R.drawable.anim_avatar_1)
            1 -> binding.backgroundAnim.setImageResource(R.drawable.anim_avatar_2)
            2 -> binding.backgroundAnim.setImageResource(R.drawable.anim_avatar_3)
            3 -> binding.backgroundAnim.setImageResource(R.drawable.anim_avatar_4)
            4 -> binding.backgroundAnim.setImageResource(R.drawable.anim_avatar_5)
        }

        ObjectAnimator.ofFloat(binding.backgroundAnim, "rotation", 0f, 360f).apply {
            val random_int2 = (2..4).random()
            repeatCount = ObjectAnimator.INFINITE
            duration = (3000 * random_int2).toLong()
            interpolator = android.view.animation.LinearInterpolator()
            start()
        }

        netiCore?.client?.userInfoViewModel?.userPhotoLiveData?.observe(viewLifecycleOwner){
            when (it.status){
                Status.LOADING -> {
                    binding.personAvatarPlaceholder.visibility = View.GONE
                    binding.personAvatarBackground.visibility = View.VISIBLE
                    binding.backgroundAnim.visibility = View.GONE
                }
                Status.ERROR -> {
                    binding.personAvatarPlaceholder.visibility = View.GONE
                    binding.personAvatarBackground.visibility = View.VISIBLE
                    binding.backgroundAnim.visibility = View.GONE
                }
                Status.SUCCESS -> {
                    val path = it.data
                    if (path != null){
                        binding.personAvatarBackground.visibility = View.GONE
                        binding.personAvatarPlaceholder.visibility = View.VISIBLE
                        binding.backgroundAnim.visibility = View.VISIBLE
                        ObjectAnimator.ofFloat(binding.backgroundAnim, "scaleX", 1f).apply {
                            duration = 300
                            start()
                        }
                        ObjectAnimator.ofFloat(binding.backgroundAnim, "scaleY", 1f).apply {
                            duration = 300
                            start()
                        }
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
                        binding?.buttonLogOut?.setOnClickListener {
                            netiCore?.logOut()
                        }
                        binding?.buttonTryAgain?.setOnClickListener {
                            netiCore?.checkAuthorization()
                        }
                    }
                    AuthorizationState.UNAUTHORIZED -> {
                        enableButtons(false)
                        binding?.authErrorCard?.visibility = View.GONE
                    }
                    AuthorizationState.LOADING -> {
                        enableButtons(false)
                        binding?.authErrorCard?.visibility = View.GONE
                    }
                }
            }
        }
    }

}