package com.dertefter.ficus.fragments.schedule.schedule_sessia

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.ScheduleSessiaBottomSheetBinding
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.schedule.SessiaScheduleItem
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.Locale

class ScheduleSessiaBottomSheet() : BottomSheetDialogFragment() {
    private lateinit var item: SessiaScheduleItem
    lateinit var binding: ScheduleSessiaBottomSheetBinding
    var netiCore: NETICore? = null

    companion object {
        val TAG: String = "sessiaSchedule"
        private const val ITEM = "item"

        fun newInstance(
            item: SessiaScheduleItem
        ): ScheduleSessiaBottomSheet = ScheduleSessiaBottomSheet().apply {
            arguments = Bundle().apply {
                putParcelable(ITEM, item)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.schedule_sessia_bottom_sheet, container, false)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU){
            item = arguments?.getParcelable(ITEM) ?: throw IllegalStateException("No args provided")
        }else{
            item = arguments?.getParcelable(ITEM, SessiaScheduleItem::class.java) ?: throw IllegalStateException("No args provided")
        }

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ScheduleSessiaBottomSheetBinding.bind(view)
        netiCore = (activity?.application as Ficus).netiCore
        binding.title.text = item.title
        binding.time.text = item.time
        if (item.time.isNullOrEmpty()){
            binding.time.text = "--:--"
        }
        if (item.aud.isNullOrEmpty()){
            binding.aud.text = "Где-то"
        }
        binding.date.text = formatDate(item.date!!)
        binding.aud.text = item.aud
        binding.type.text = item.type
        netiCore!!.personHelper!!.retrivePerson(item.personLink, binding.name, binding.personAvatarPlaceholder)
    }

    fun formatDate(dateString: String): String {
        val locale = Locale("ru", "RU")
        val inputFormat = SimpleDateFormat("dd.MM", locale)
        val date = inputFormat.parse(dateString)
        val outputFormat = SimpleDateFormat("dd MMMM", locale)
        return outputFormat.format(date)
    }
}
