package com.dertefter.ficus.fragments.schedule.schedule_sessia

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.ficus.MainActivity
import com.dertefter.ficus.R
import com.dertefter.neticore.data.Person
import com.dertefter.neticore.data.schedule.SessiaScheduleItem
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso

class ScheduleSessiaListAdapter(val fragment: ScheduleSessiaFragment) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var list = mutableListOf<SessiaScheduleItem>()
    var isError = false


    fun setList(newList: List<SessiaScheduleItem>?){
        if (newList != null){
            list = newList.toMutableList()
        }
        notifyDataSetChanged()
    }


    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val time: TextView = itemView.findViewById(R.id.time)
        val date: TextView = itemView.findViewById(R.id.date)
        val type: TextView = itemView.findViewById(R.id.type)
        val aud: TextView  = itemView.findViewById(R.id.aud)
        val dayName: TextView  = itemView.findViewById(R.id.dayName)
        val avatar: ShapeableImageView = itemView.findViewById(R.id.person_avatar_placeholder)
        val card: MaterialCardView = itemView.findViewById(R.id.card_view)
        val liveData: MutableLiveData<Person?> = MutableLiveData()
    }
    class ExamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val time: TextView = itemView.findViewById(R.id.time)
        val date: TextView = itemView.findViewById(R.id.date)
        val type: TextView = itemView.findViewById(R.id.type)
        val aud: TextView  = itemView.findViewById(R.id.aud)
        val dayName: TextView  = itemView.findViewById(R.id.dayName)
        val avatar: ShapeableImageView = itemView.findViewById(R.id.person_avatar_placeholder)
        val card: MaterialCardView = itemView.findViewById(R.id.card_view)
        val liveData: MutableLiveData<Person?> = MutableLiveData()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0){
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_sessia_schedule, parent, false)
            ItemViewHolder(itemView)
        } else{
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_sessia_schedule_exam, parent, false)
            ExamViewHolder(itemView)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = list[position]
        if (holder is ItemViewHolder){
            holder.title.text = currentItem.title
            holder.time.text  = if(currentItem.time.isNullOrEmpty()){"--:--"}else{currentItem.time}
            holder.date.text   = currentItem.date
            holder.type.text  = currentItem.type
            holder.aud.text    = currentItem.aud
            holder.dayName.text= currentItem.dayName
            holder.card.setOnClickListener {
                (fragment as ScheduleSessiaFragment).openDialog(currentItem)
            }
            (fragment.activity as MainActivity).netiCore!!.personHelper!!.retrivePerson(currentItem.personLink, holder.liveData)
            holder.liveData.observeForever {
                if (it != null){
                    if (!it.pic.isNullOrEmpty()){
                        Picasso.get().load(it.pic).resize(200,200).centerCrop().into(holder.avatar)
                    }
                }
            }
        }else if  (holder is ExamViewHolder){
            holder.title.text = currentItem.title
            holder.time.text  = if(currentItem.time.isNullOrEmpty()){"--:--"}else{currentItem.time}
            holder.date.text   = currentItem.date
            holder.type.text  = currentItem.type
            holder.aud.text     = currentItem.aud
            holder.dayName.text= currentItem.dayName
            holder.card.setOnClickListener {
                (fragment as ScheduleSessiaFragment).openDialog(currentItem)
            }
            (fragment.activity as MainActivity).netiCore!!.personHelper!!.retrivePerson(currentItem.personLink, holder.liveData)
            holder.liveData.observeForever {
                if (it != null){
                    if (!it.pic.isNullOrEmpty()){
                        Picasso.get().load(it.pic).resize(200,200).centerCrop().into(holder.avatar)
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentItem = list[position]
        if (currentItem.type == "Экзамен"){
            return 1
        }else{
            return 0
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}