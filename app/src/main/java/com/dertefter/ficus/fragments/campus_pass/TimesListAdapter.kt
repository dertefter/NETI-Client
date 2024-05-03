package com.dertefter.ficus.fragments.campus_pass

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.ficus.R
import com.dertefter.neticore.data.campus_pass.CampusPassDate
import com.dertefter.neticore.data.campus_pass.CampusPassTime
import com.dertefter.neticore.data.schedule.Group

class TimesListAdapter(val fragment: CampusPassFragment) : RecyclerView.Adapter<TimesListAdapter.DayViewHolder>() {
    private var data = mutableListOf<CampusPassTime>()

    fun setData(newList: List<CampusPassTime>?){
        if (newList.isNullOrEmpty()){
            data.clear()
        }else{
            data = newList.toMutableList()
        }
        notifyDataSetChanged()
    }

    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_campus_pass_day_time, parent, false)
        return DayViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val currentItem = data[position]
        holder.title.text = currentItem.time_text
        holder.itemView.setOnClickListener {
            fragment.selectTime(currentItem)
        }

    }

    override fun getItemCount(): Int {
        return data.size
    }


}