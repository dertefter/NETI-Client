package com.dertefter.ficus.fragments.other

import android.app.Dialog
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
import com.dertefter.ficus.databinding.FragmentImageDialogBinding
import com.dertefter.ficus.databinding.FragmentProfileDialogBinding
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.AuthorizationState
import com.dertefter.neticore.data.Status
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch


class ImageDialogFragment : DialogFragment(R.layout.fragment_image_dialog) {

    lateinit var binding: FragmentImageDialogBinding

    var image: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        binding = FragmentImageDialogBinding.bind(view)

        Picasso.get().load(image).resize(800, 0).into(binding.pic)

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val arg = arguments
        image = arg?.getString("image")
        return dialog
    }

}