package com.dertefter.ficus.fragments.dispace.di_cources.di_cource_search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.ficus.R
import com.dertefter.neticore.data.dispace.di_cources.DiCourse
import com.google.android.material.button.MaterialButton

class DiCourseListAdapter(val fragment: DiCourceSearchFragment) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var chatList = mutableListOf<DiCourse>()
    var isError = false

    fun setCourseList(chatList_: List<DiCourse>?){
        if (!chatList_.isNullOrEmpty()) {
            chatList = chatList_.toMutableList()
            notifyDataSetChanged()
        }else{
            chatList.clear()
            notifyDataSetChanged()
        }
    }

    class CourseItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val person: TextView = itemView.findViewById(R.id.person)
        val courceId = itemView.findViewById<TextView>(R.id.course_id)
        val fav: MaterialButton = itemView.findViewById(R.id.favButton)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CourseItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_course, parent, false))

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = chatList[position]
        val holderCourse = holder as CourseItemViewHolder
        holderCourse.title.text = currentItem.name
        holderCourse.person.text = currentItem.colleague
        holderCourse.courceId.text = currentItem.id.toString()
        val isFav = currentItem.infav == 1
        if (isFav){
            holderCourse.fav.setIconResource(R.drawable.favorite_filled)
            holderCourse.fav.setOnClickListener {
                fragment.removeFav(currentItem)
            }
        }else{
            holderCourse.fav.setIconResource(R.drawable.favorite)
            holderCourse.fav.setOnClickListener {
                fragment.addFav(currentItem)
            }
        }
        holderCourse.itemView.setOnClickListener {
            fragment.openCourse(currentItem)
        }
    }


    override fun getItemCount(): Int {
        return chatList.size
    }

}