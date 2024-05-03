package com.dertefter.ficus.fragments.campus_pass

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.GridLayoutManager
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentCampusPassBinding
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.Status
import com.dertefter.neticore.data.campus_pass.CampusPass
import com.dertefter.neticore.data.campus_pass.CampusPassDate
import com.dertefter.neticore.data.campus_pass.CampusPassTime
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.MaterialShapeDrawable

class CampusPassFragment : Fragment(R.layout.fragment_campus_pass) {

    var netiCore: NETICore? = null

    lateinit var binding: FragmentCampusPassBinding



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore
        binding = FragmentCampusPassBinding.bind(view)

        setupToolbar()

        observeCampusPassData()

        netiCore?.client?.campusPassViewModel?.updateCampusPassData()


    }


    fun showPass(campusPass: CampusPass) {
        binding.number.text = campusPass.number
        binding.date.text = campusPass.date
        binding.time.text = campusPass.time
        binding.passView.visibility = View.VISIBLE
        binding.actionTextView.text = "Ваша запись"
        binding.dateTimeSelector.visibility = View.GONE
        binding.cancelButton.setOnClickListener {
            netiCore?.client?.campusPassViewModel?.cancelCampusPass(campusPass.id)
        }
    }

    fun showSelection(campusdates: MutableList<CampusPassDate>){
        binding.passView.visibility = View.GONE
        binding.dateTimeSelector.visibility = View.VISIBLE
        val adapter = DateListAdapter(this)
        adapter.setData(campusdates)
        binding.dayRecyclerView.adapter = adapter
        binding.dayRecyclerView.visibility = View.VISIBLE
        binding.dayRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        observeTimes()
        observeDate()
    }

    fun selectDate(campusPassDate: CampusPassDate){
        netiCore?.client?.campusPassViewModel?.selectDate(campusPassDate)
    }



    fun observeCampusPassData(){
        netiCore?.client?.campusPassViewModel?.campusPassLiveData?.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    binding.actionTextView.text = ""
                    binding.scrollView.visibility = View.GONE
                    binding.loading.visibility = View.VISIBLE
                    ObjectAnimator.ofFloat(binding.loading, "alpha", 0f, 1f).apply {
                        duration = 300
                        start()
                    }
                }

                Status.SUCCESS -> {
                    binding.scrollView.visibility = View.VISIBLE
                    binding.loading.visibility = View.GONE
                    if (it.data != null && it.data!!.isRegistered){
                        showPass(it.data!!.campusPass!!)
                    }else{
                        binding.passView.visibility = View.GONE
                        binding.dateTimeSelector.visibility = View.VISIBLE
                        showSelection(it.data!!.campusPassDates!!)
                    }
                }

                Status.ERROR -> {
                    binding.actionTextView.text = ""
                    binding.scrollView.visibility = View.GONE
                    binding.loading.visibility = View.GONE
                }
            }
        }
    }

    fun observeTimes(){
        netiCore?.client?.campusPassViewModel?.campusPassTimesLiveData?.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    binding.loading.visibility = View.VISIBLE
                    ObjectAnimator.ofFloat(binding.loading, "alpha", 0f, 1f).apply {
                        duration = 300
                        start()
                    }
                }

                Status.SUCCESS -> {
                    binding.scrollView.visibility = View.VISIBLE
                    binding.loading.visibility = View.GONE
                    val adapter = TimesListAdapter(this)
                    adapter.setData(it.data)
                    binding.timeRecyclerView.adapter = adapter
                    binding.timeRecyclerView.visibility = View.VISIBLE
                    binding.timeRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
                }

                Status.ERROR -> {
                    binding.scrollView.visibility = View.GONE
                    binding.loading.visibility = View.GONE
                }
            }
        }
    }

    fun observeDate(){
        netiCore?.client?.campusPassViewModel?.selectedDate?.observe(viewLifecycleOwner) {
            Log.e("observeDate", "observeDate: $it")
            if (it == null){
                binding.dayRecyclerView.visibility = View.VISIBLE
                binding.timeRecyclerView.visibility = View.GONE
                binding.toolbarCollapse.subtitle = ""
                binding.actionTextView.text = "Выберите дату"
            }else{
                binding.dayRecyclerView.visibility = View.GONE
                binding.timeRecyclerView.visibility = View.VISIBLE
                binding.toolbarCollapse.subtitle = it.title
                binding.actionTextView.text = "Выберите время"
            }
        }
    }


    fun setupToolbar(){
        binding?.appBarLayout?.statusBarForeground = MaterialShapeDrawable.createWithElevationOverlay(context)
        binding?.toolbar?.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    fun selectTime(time: CampusPassTime){
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Запись")
            .setMessage("Вы уверены, что хотите записаться на ${time.time_text}?")
            .setNegativeButton("Отмена") { dialog, which ->
                netiCore?.client?.campusPassViewModel?.updateCampusPassData()
                dialog.dismiss()
            }
            .setPositiveButton("Записаться") { dialog, which ->
                netiCore?.client?.campusPassViewModel?.registerCampusPass(time)
            }
            .show()
    }

}