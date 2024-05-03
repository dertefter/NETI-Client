package com.dertefter.ficus.fragments.files

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
import com.dertefter.neticore.NETICore
import com.google.android.material.color.MaterialColors
import java.io.File
import java.util.Locale


class FilesFragment : Fragment(R.layout.fragment_files) {

    lateinit var binding: FragmentFilesBinding

    var netiCore: NETICore? = null
    lateinit var adapter: FilesListAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore
        binding = FragmentFilesBinding.bind(view)
        netiCore?.diSpaceClient?.fileManagerViewModel?.updateSavedFilesList()
        adapter = FilesListAdapter(this)
        adapter.setFileList(listOf())
        setupRecyclerView()

        observeFilesList()
        setupAppBar()

    }

    fun showMenu(v: View, @MenuRes menuRes: Int, file_name: String) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            if (menuItem.itemId == R.id.delete) {

                netiCore?.diSpaceClient?.fileManagerViewModel?.deleteFile(file_name)
                true
            }
            false
        }
        popup.setOnDismissListener {
            popup.dismiss()
        }
        // Show the popup menu.
        popup.show()
    }

    fun getMimeType(uri: Uri): String? {
        var mimeType: String? = null
        mimeType = if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            val cr: ContentResolver = requireActivity().applicationContext.contentResolver
            cr.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(
                uri
                    .toString()
            )
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                fileExtension.lowercase(Locale.getDefault())
            )
        }
        return mimeType
    }


    fun openFile(path: String){
        val uri = FileProvider.getUriForFile(requireContext(), activity?.application?.packageName  + ".provider", File(requireActivity().filesDir.absolutePath+"/down/" + path))
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setDataAndType(uri, getMimeType(uri))
        startActivity(intent)
    }

    fun setupAppBar(){
        binding.appBarLayout.setStatusBarForegroundColor(
            MaterialColors.getColor(binding.appBarLayout, com.google.android.material.R.attr.colorSurface))
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

    }

    fun setupRecyclerView(){
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    fun observeFilesList(){
        netiCore?.diSpaceClient?.fileManagerViewModel?.savedFilesListLiveData?.observe(viewLifecycleOwner) {
            adapter.setFileList(it)
        }
    }



}