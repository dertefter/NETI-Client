package com.dertefter.ficus.fragments.schedule.schedule_rules_dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.children
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentImageDialogBinding
import com.dertefter.ficus.databinding.FragmentProfileDialogBinding
import com.dertefter.ficus.databinding.FragmentScheduleRulesDialogBinding
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.AuthorizationState
import com.dertefter.neticore.data.Status
import com.dertefter.neticore.data.schedule.Lesson
import com.google.android.material.chip.Chip
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch


class ScheduleRulesDialogFragment : DialogFragment(R.layout.fragment_schedule_rules_dialog) {

    lateinit var binding: FragmentScheduleRulesDialogBinding
    var netiCore: NETICore? = null
    val weeksList = mutableListOf<Int>()
    val daysList = mutableListOf<Int>()
    var isNote = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore
        binding = FragmentScheduleRulesDialogBinding.bind(view)
        binding.cancel.setOnClickListener { dismiss() }
        binding.buttonTypeLesson.setOnClickListener { setIsNote(false) }
        binding.buttonTypeNote.setOnClickListener { setIsNote(true)  }
        binding.titleInput.doOnTextChanged { text, start, before, count -> validateTextInput() }
        for (i in binding.days.children){
            (i as Chip).setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked){
                    daysList.add(binding.days.children.indexOf(i))
                }else{
                    daysList.remove(binding.days.children.indexOf(i))
                }
            }
        }
        for (i in binding.days.children){
            (i as Chip).setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked){
                    daysList.add(binding.days.children.indexOf(i))
                }else{
                    daysList.remove(binding.days.children.indexOf(i))
                }
            }
        }

        for (i in binding.weeks.children){
            (i as Chip).setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked){
                    weeksList.add(binding.weeks.children.indexOf(i) + 1)
                }else{
                    weeksList.remove(binding.weeks.children.indexOf(i) + 1)
                }
            }
        }

        binding.save.setOnClickListener {
            if (weeksList.isEmpty()){
                Toast.makeText(context, "Выберите недели", Toast.LENGTH_SHORT).show()
            } else if (daysList.isEmpty()){
                Toast.makeText(context, "Выберите дни", Toast.LENGTH_SHORT).show()
            } else {
                for (w in weeksList){
                    for (d in daysList){
                        val customId = "scheduleRules${w}$d"
                        val lesson = Lesson(
                            title = binding.titleInput.text.toString(),
                            aud = binding.audInput.text.toString(),
                            person = binding.personInput.text.toString(),
                            timeStart = binding.timeStartInput.text.toString(),
                            timeEnd = binding.timeEndInput.text.toString(),
                            isCustom = true,
                            isNote = isNote,
                            customId = customId,
                        )
                        netiCore?.client?.scheduleViewModel?.createRule(lesson, customId)
                        netiCore?.client?.updateWeeks()
                    }
                }
                Toast.makeText(context, "Сохранено", Toast.LENGTH_SHORT).show()
                dismiss()
            }

        }
    }

    fun validateTextInput(){
        if (binding.titleInput.text.isNullOrEmpty()){
            binding.save.isEnabled = false
        }else{
            binding.save.isEnabled = true
        }
    }
    fun setIsNote(b: Boolean){
        isNote = b
        if (b){
            binding.titleInputLayout.hint = "Текст заметки"
            binding.audInputLayout.visibility = View.GONE
            binding.personInputLayout.visibility = View.GONE
            binding.timeStartInputLayout.visibility = View.GONE
            binding.timeEndInputLayout.visibility = View.GONE
        }else{
            binding.titleInputLayout.hint = "Название пары"
            binding.audInputLayout.visibility = View.VISIBLE
            binding.personInputLayout.visibility = View.VISIBLE
            binding.timeStartInputLayout.visibility = View.VISIBLE
            binding.timeEndInputLayout.visibility = View.VISIBLE
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        return dialog
    }

}