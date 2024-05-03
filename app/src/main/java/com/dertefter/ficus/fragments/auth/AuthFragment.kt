package com.dertefter.ficus.fragments.auth

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentAuthBinding
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.AuthorizationState
import kotlinx.coroutines.launch

class AuthFragment : Fragment(R.layout.fragment_auth) {

    var netiCore: NETICore? = null

    lateinit var binding: FragmentAuthBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore
        binding = FragmentAuthBinding.bind(view)
        observeAuthState()
        setupAuthButton()
    }

    fun checkTextFields(){
        if (binding.loginTextField.editText?.text.toString().isEmpty() || binding.passwordTextField.editText?.text.toString().isEmpty()){
            binding.authButton.isEnabled = false
        }else{
            binding.authButton.isEnabled = true
        }
    }

    fun setupAuthButton(){
        binding.loginTextField.editText?.doOnTextChanged { text, start, before, count ->
            checkTextFields()
        }
        binding.passwordTextField.editText?.doOnTextChanged { text, start, before, count ->
            checkTextFields()
        }
        binding.authButton.setOnClickListener {
            Log.e("AuthFragment", "setupAuthButton: ${binding.loginTextField.editText?.text.toString()}")
            netiCore?.login(binding.loginTextField.editText?.text.toString(),
                            binding.passwordTextField.editText?.text.toString())
        }
    }

    fun observeAuthState(){
        lifecycleScope.launch {
            netiCore?.client?.authorizationStateViewModel?.uiState?.collect {
                if (it == AuthorizationState.LOADING){
                    binding.progressBar.visibility = View.VISIBLE
                    binding.authButton.isEnabled = false
                }else{
                    binding.progressBar.visibility = View.GONE
                    binding.authButton.isEnabled = true
                }
                when(it){
                    AuthorizationState.AUTHORIZED -> {
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                    AuthorizationState.AUTHORIZED_WITH_ERROR -> {
                        Log.e("AuthFragment", "observeAuthState: AUTHORIZED_WITH_ERROR")
                    }
                    AuthorizationState.UNAUTHORIZED -> {
                        Log.e("AuthFragment", "observeAuthState: UNAUTHORIZED")
                    }
                    AuthorizationState.LOADING -> {
                        Log.e("AuthFragment", "observeAuthState: LOADING")
                    }
                }
            }
        }
    }

}