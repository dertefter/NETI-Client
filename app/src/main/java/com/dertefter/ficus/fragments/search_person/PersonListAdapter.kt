package com.dertefter.ficus.fragments.search_person

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.ficus.R
import com.dertefter.neticore.data.Person
import com.dertefter.neticore.data.schedule.Group
import com.google.android.material.color.MaterialColors
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso

class PersonListAdapter(val fragment: SearchPersonFragment) : RecyclerView.Adapter<PersonListAdapter.PersonViewHolder>() {
    private var personList = mutableListOf<Person>()

    fun setPersonList(newList: List<Person>?){
        if (newList.isNullOrEmpty()){
            personList.clear()
        }else{
            personList = newList.toMutableList()
        }
        notifyDataSetChanged()
    }

    class PersonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.person_name)
        val mail : TextView = itemView.findViewById(R.id.person_mail)
        val avatar: ShapeableImageView = itemView.findViewById(R.id.person_avatar_placeholder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_person, parent, false)
        return PersonViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        val currentItem = personList[position]
        holder.name.text = currentItem.name
        holder.mail.text = currentItem.mail
        if (!currentItem.pic.isNullOrEmpty()){
            Picasso.get().load(currentItem.pic).resize(300,300).centerCrop().into(holder.avatar)
            holder.avatar.visibility = View.VISIBLE
        }else{
            holder.avatar.visibility = View.GONE
        }
        holder.itemView.setOnClickListener {
            fragment.openPerson(currentItem)
        }
    }

    override fun getItemCount(): Int {
        return personList.size
    }

    fun getLastPosition(): Int {
        return personList.size - 1
    }

}