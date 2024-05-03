package com.dertefter.ficus.fragments.campus_pass

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.ficus.R
import com.dertefter.neticore.data.campus_pass.CampusPassDate
import com.dertefter.neticore.data.schedule.Group

class DateListAdapter(val fragment: CampusPassFragment) : RecyclerView.Adapter<DateListAdapter.DayViewHolder>() {
    private var data = mutableListOf<CampusPassDate>()

    fun setData(newList: List<CampusPassDate>?){
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
        holder.title.text = currentItem.title
        holder.itemView.setOnClickListener {
            fragment.selectDate(currentItem)
        }

    }

    override fun getItemCount(): Int {
        return data.size
    }


}