package com.dertefter.ficus.fragments.dispace.di_cources.course_view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentCourseViewBinding
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.Status
import com.dertefter.neticore.data.dispace.di_cources.DiCourse
import com.dertefter.neticore.data.dispace.di_cources.DiCourseView
import com.google.android.material.color.MaterialColors

class CourseViewFragment : Fragment(R.layout.fragment_course_view) {

    lateinit var binding : FragmentCourseViewBinding

    var netiCore: NETICore? = null

    lateinit var course: DiCourse

    lateinit var adapter: DiCourseViewAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCourseViewBinding.bind(view)
        netiCore = (activity?.application as Ficus).netiCore
        adapter = DiCourseViewAdapter(this)
        course = CourseViewFragmentArgs.fromBundle(requireArguments()).course
        binding.buttonMenu.setOnClickListener {
            val modalBottomSheet = ModalBottomSheet()
            modalBottomSheet.show(parentFragmentManager, ModalBottomSheet.TAG)
        }

        binding.appBarLayout.setStatusBarForegroundColor(
            MaterialColors.getColor(binding.appBarLayout, com.google.android.material.R.attr.colorSurface))
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        setupRecyclerView()
        observeCourseView()


        netiCore?.diSpaceClient?.fileManagerViewModel?.updateSavedFilesList()
        observeFilesList()

        netiCore?.diSpaceClient?.diCourcesViewModel?.updateCourseView(course.id.toString())
    }

    fun observeFilesList(){
        netiCore?.diSpaceClient?.fileManagerViewModel?.savedFilesListLiveData?.observe(viewLifecycleOwner) {
            adapter.savedFileList = it.toMutableList()
            adapter.notifyDataSetChanged()
        }
    }

    fun downloadFile(link: String, name: String){
        netiCore?.diSpaceClient?.fileManagerViewModel?.downloadFile(link, name)
    }


    fun displayCourseView(courseView: DiCourseView){
        val contentItems = courseView.contentItems
        val title = courseView.title
        binding.toolbar.title = title
        adapter.setData(contentItems)
    }

    fun setupRecyclerView(){
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    fun observeCourseView(){
        netiCore?.diSpaceClient?.diCourcesViewModel?.courseViewLiveData?.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                Status.SUCCESS -> {
                    binding.progressBar.visibility = View.GONE
                    val courseView = it.data
                    displayCourseView(courseView!!)

                }

                Status.ERROR -> {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

}