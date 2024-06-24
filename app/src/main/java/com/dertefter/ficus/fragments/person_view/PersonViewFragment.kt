package com.dertefter.ficus.fragments.person_view

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentFilesBinding
import com.dertefter.ficus.databinding.FragmentPersonViewBinding
import com.dertefter.neticore.NETICore
import com.google.android.material.color.MaterialColors
import java.io.File
import java.util.Locale


class PersonViewFragment : Fragment(R.layout.fragment_person_view) {

    lateinit var binding: FragmentPersonViewBinding

    var netiCore: NETICore? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore
        binding = FragmentPersonViewBinding.bind(view)
    }

}