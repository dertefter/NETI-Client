package com.dertefter.ficus.fragments.sessia_results.share_sessia

import android.content.ClipboardManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.DiCourseViewBottomSheetBinding
import com.dertefter.ficus.databinding.ShareSessiaBottomSheetBinding
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.Status
import com.dertefter.neticore.data.dispace.di_cources.DiCourseView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ShareSessiaBottomSheet : BottomSheetDialogFragment() {

    lateinit var binding: ShareSessiaBottomSheetBinding
    var netiCore: NETICore? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.share_sessia_bottom_sheet, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ShareSessiaBottomSheetBinding.bind(view)
        netiCore = (activity?.application as Ficus).netiCore

        binding.refrashScoreLink.setOnClickListener {
            netiCore?.client?.sessiaViewModel?.updateLink()
        }

        observeSessiaLink()

        if (netiCore?.client?.sessiaViewModel?.sessiaLinkLiveData?.value?.status != Status.SUCCESS){
            netiCore?.client?.sessiaViewModel?.getLink()
        }

    }

    fun observeSessiaLink(){
        netiCore?.client?.sessiaViewModel?.sessiaLinkLiveData?.observe(viewLifecycleOwner){
            when(it.status){
                Status.SUCCESS -> {
                    val link = it.data
                    binding.scoreLink.text = link
                    binding.refrashScoreLink.isEnabled = true
                    binding.copyLink.isEnabled = true
                    binding.copyLink.setOnClickListener {
                        //copy to clipboard
                        val clipboard = context?.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Ссылка из Ficus", link)
                        clipboard.setPrimaryClip(clip)


                    }

                }
                Status.ERROR -> {
                    binding.scoreLink.text = "Ошибка загрузки..."
                    binding.refrashScoreLink.isEnabled = true
                    binding.copyLink.isEnabled = true
                }
                Status.LOADING -> {
                    binding.scoreLink.text = ""
                    binding.refrashScoreLink.isEnabled = false
                    binding.copyLink.isEnabled = false
                }
            }
        }
    }


    companion object {
        const val TAG = "ShareScore"
    }
}
