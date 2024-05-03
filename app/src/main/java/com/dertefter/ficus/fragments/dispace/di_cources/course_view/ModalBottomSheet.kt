package com.dertefter.ficus.fragments.dispace.di_cources.course_view

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
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.Status
import com.dertefter.neticore.data.dispace.di_cources.DiCourseView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ModalBottomSheet : BottomSheetDialogFragment() {

    lateinit var binding: DiCourseViewBottomSheetBinding
    var netiCore: NETICore? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.di_course_view_bottom_sheet, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DiCourseViewBottomSheetBinding.bind(view)
        netiCore = (activity?.application as Ficus).netiCore
        observeCourseView()

    }

    fun displayCourseMenu(courseView: DiCourseView){
        val navigationItems = courseView.navigationItems
        binding.navigationView.menu.clear()
        for (item in navigationItems){
            binding.navigationView.menu.add(item)
        }
        binding.navigationView.setNavigationItemSelectedListener{
            var page = "0"
            for (i in navigationItems){
                if (it.title == i){
                    page = (navigationItems.indexOf(i) - 1).toString()
                }
            }
            netiCore?.diSpaceClient?.diCourcesViewModel?.updateCourseView(courseView.id, page.toString())
            Log.e("order is", page)
            dismiss()
            true
        }

    }

    fun observeCourseView(){
        netiCore?.diSpaceClient?.diCourcesViewModel?.courseViewLiveData?.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                }

                Status.SUCCESS -> {
                    val courseView = it.data
                    displayCourseMenu(courseView!!)

                }

                Status.ERROR -> {
                }
            }
        }
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }
}
