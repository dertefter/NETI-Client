package com.dertefter.ficus.fragments.schedule

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.ficus.R
import com.dertefter.neticore.data.schedule.Day
import com.dertefter.neticore.data.schedule.Group
import com.dertefter.neticore.data.schedule.Lesson


const val DAY_TYPE = 0
const val LESSON_TYPE = 1
const val LAB_TYPE = 2
const val SEMINAR_TYPE = 3

class SavedGroupsAdapter(val fragment: ScheduleFragment): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var data = mutableListOf<Group>()

    fun setData(newData: List<Group>?) {
        if (newData!= null) {
            data = newData.toMutableList()
            notifyDataSetChanged()
        }
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title_group)
        val remove: ImageView = itemView.findViewById(R.id.remove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(fragment.requireContext()).inflate(R.layout.item_saved_group, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = data[position]
        (holder as ViewHolder).title.text = currentItem.title
        if (currentItem.isIndividual){
            (holder as ViewHolder).title.text = "${currentItem.title} • Индивидуальное"
        }
        (holder as ViewHolder).remove.setOnClickListener {
            fragment.netiCore?.client?.scheduleViewModel?.deleteGroup(currentItem)
        }
        (holder as ViewHolder).itemView.setOnClickListener {
            fragment.netiCore?.client?.scheduleViewModel?.setGroup(currentItem)
        }
    }


    override fun getItemCount(): Int {
        return data.size
    }

}