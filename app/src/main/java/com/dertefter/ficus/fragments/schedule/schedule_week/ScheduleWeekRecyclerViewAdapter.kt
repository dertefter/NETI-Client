package com.dertefter.ficus.fragments.schedule.schedule_week

import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.ficus.R
import com.dertefter.neticore.data.schedule.Day
import com.dertefter.neticore.data.schedule.Lesson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


const val DAY_TYPE = 0
const val LESSON_TYPE = 1
const val LAB_TYPE = 2
const val SEMINAR_TYPE = 3
const val CUSTOM_TYPE = 4

class ScheduleWeekRecyclerViewAdapter(val fragment: ScheduleWeekFragment): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var listOfAny = mutableListOf<Any>()
    var _dayList: List<Day>? = null
    var isError = false

    var scrolledTo = 0

    fun setDayList(dayList: List<Day>?) {
        CoroutineScope(Dispatchers.IO).launch {
            _dayList = dayList
            listOfAny.clear()
            dayList?.forEach { day ->
                listOfAny.add(day)
                day.lessons?.let { listOfAny.addAll(it) }
            }
            withContext(Dispatchers.Main){
                notifyDataSetChanged()
            }
        }
    }


    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
    }

    class LessonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val timeStart: TextView = itemView.findViewById(R.id.timeStart)
        val timeEnd: TextView = itemView.findViewById(R.id.timeEnd)
        val aud: TextView = itemView.findViewById(R.id.aud)
        val person: TextView = itemView.findViewById(R.id.person)
        val type: TextView = itemView.findViewById(R.id.type)
    }

    class LabViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val timeStart: TextView = itemView.findViewById(R.id.timeStart)
        val timeEnd: TextView = itemView.findViewById(R.id.timeEnd)
        val aud: TextView = itemView.findViewById(R.id.aud)
        val person: TextView = itemView.findViewById(R.id.person)
        val type: TextView = itemView.findViewById(R.id.type)
    }

    class SeminarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val timeStart: TextView = itemView.findViewById(R.id.timeStart)
        val timeEnd: TextView = itemView.findViewById(R.id.timeEnd)
        val aud: TextView = itemView.findViewById(R.id.aud)
        val person: TextView = itemView.findViewById(R.id.person)
        val type: TextView = itemView.findViewById(R.id.type)
    }

    class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val timeStart: TextView = itemView.findViewById(R.id.timeStart)
        val timeEnd: TextView = itemView.findViewById(R.id.timeEnd)
        val aud: TextView = itemView.findViewById(R.id.aud)
        val person: TextView = itemView.findViewById(R.id.person)
        val type: TextView = itemView.findViewById(R.id.type)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == LESSON_TYPE){
            val itemView = LayoutInflater.from(fragment.requireContext()).inflate(R.layout.item_lesson, parent, false)
            LessonViewHolder(itemView)
        }else if (viewType == LAB_TYPE){
            val itemView = LayoutInflater.from(fragment.requireContext()).inflate(R.layout.item_lesson_lab, parent, false)
            LabViewHolder(itemView)
        }else if (viewType == SEMINAR_TYPE){
            val itemView = LayoutInflater.from(fragment.requireContext()).inflate(R.layout.item_lesson_seminar, parent, false)
            SeminarViewHolder(itemView)
        }else if (viewType == CUSTOM_TYPE){
            val itemView = LayoutInflater.from(fragment.requireContext()).inflate(R.layout.item_lesson_custom, parent, false)
            CustomViewHolder(itemView)
        }
        else{
            val itemView = LayoutInflater.from(fragment.requireContext()).inflate(R.layout.item_day, parent, false)
            DayViewHolder(itemView)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DayViewHolder){
            val currentItem = listOfAny[position] as Day
            if (currentItem.today){
                val typedValue = TypedValue()
                val theme = fragment.requireContext().theme
                theme.resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true)
                val color = typedValue.data
                holder.title.setTextColor(color)
                scrolledTo = position
            }else{
                val typedValue = TypedValue()
                val theme = fragment.requireContext().theme
                theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)
                val color = typedValue.data
                holder.title.setTextColor(color)
            }
            holder.title.text = currentItem.title
        }
        else if (holder is LessonViewHolder){
            val currentItem = listOfAny[position] as Lesson
            holder.title.text = currentItem.title
            holder.timeStart.text = currentItem.timeStart
            holder.timeEnd.text = currentItem.timeEnd
            if (currentItem.aud.isNullOrEmpty()){
                (holder.aud).visibility = View.GONE
            }else{
                (holder.aud).visibility = View.VISIBLE
                holder.aud.text = currentItem.aud
            }

            if (currentItem.person.isNullOrEmpty()){
                (holder.person.parent as View).visibility = View.GONE
            }else{
                (holder.person.parent as View).visibility = View.VISIBLE
                holder.person.text = currentItem.person
            }

            if (currentItem.type.isNullOrEmpty()){
                holder.type.visibility = View.GONE
            }else{
                holder.type.text = currentItem.type
                holder.type.visibility = View.VISIBLE
            }
        }
        else if (holder is LabViewHolder) {
            val currentItem = listOfAny[position] as Lesson
            holder.title.text = currentItem.title
            holder.timeStart.text = currentItem.timeStart
            holder.timeEnd.text = currentItem.timeEnd
            if (currentItem.aud.isNullOrEmpty()){
                (holder.aud).visibility = View.GONE
            }else{
                (holder.aud).visibility = View.VISIBLE
                holder.aud.text = currentItem.aud
            }

            if (currentItem.person.isNullOrEmpty()){
                (holder.person.parent as View).visibility = View.GONE
            }else{
                (holder.person.parent as View).visibility = View.VISIBLE
                holder.person.text = currentItem.person
            }

            if (currentItem.type.isNullOrEmpty()){
                holder.type.visibility = View.GONE
            }else{
                holder.type.text = currentItem.type
                holder.type.visibility = View.VISIBLE
            }
        }
        else if (holder is SeminarViewHolder) {
            val currentItem = listOfAny[position] as Lesson
            holder.title.text = currentItem.title
            holder.timeStart.text = currentItem.timeStart
            holder.timeEnd.text = currentItem.timeEnd
            if (currentItem.aud.isNullOrEmpty()){
                (holder.aud).visibility = View.GONE
            }else{
                (holder.aud).visibility = View.VISIBLE
                holder.aud.text = currentItem.aud
            }

            if (currentItem.person.isNullOrEmpty()){
                (holder.person.parent as View).visibility = View.GONE
            }else{
                (holder.person.parent as View).visibility = View.VISIBLE
                holder.person.text = currentItem.person
            }

            if (currentItem.type.isNullOrEmpty()){
                holder.type.visibility = View.GONE
            }else{
                holder.type.text = currentItem.type
                holder.type.visibility = View.VISIBLE
            }
        }
        else if (holder is CustomViewHolder){
            val currentItem = listOfAny[position] as Lesson
            holder.title.text = currentItem.title
            holder.timeStart.text = currentItem.timeStart
            holder.timeEnd.text = currentItem.timeEnd
            if (currentItem.aud.isNullOrEmpty()){
                (holder.aud).visibility = View.GONE
            }else{
                (holder.aud).visibility = View.VISIBLE
                holder.aud.text = currentItem.aud
            }

            if (currentItem.person.isNullOrEmpty()){
                (holder.person.parent as View).visibility = View.GONE
            }else{
                (holder.person.parent as View).visibility = View.VISIBLE
                holder.person.text = currentItem.person
            }

            if (currentItem.type.isNullOrEmpty()){
                holder.type.visibility = View.GONE
            }else{
                holder.type.text = currentItem.type
                holder.type.visibility = View.VISIBLE
            }

            holder.deleteButton.setOnClickListener {
                Log.e("customId", currentItem.customId.toString())
                fragment?.netiCore?.client?.scheduleViewModel?.deleteRule(lesson = currentItem, customId = currentItem.customId!!)
                fragment.netiCore?.client?.updateWeeks()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (listOfAny[position] is Day){
            DAY_TYPE
        }else{
            if ((listOfAny[position] as Lesson).isCustom){
                CUSTOM_TYPE
            }
            else if ((listOfAny[position] as Lesson).type == "Лабораторная"){
                LAB_TYPE
            }else if ((listOfAny[position] as Lesson).type == "Практика"){
                SEMINAR_TYPE
            }else{
                LESSON_TYPE
            }
        }
    }

    override fun getItemCount(): Int {
        return listOfAny.size
    }

}