package com.dertefter.ficus.fragments.search_group

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.ficus.R
import com.dertefter.neticore.data.schedule.Group

class GroupListAdapter(val fragment: SearchGroupFragment) : RecyclerView.Adapter<GroupListAdapter.GroupViewHolder>() {
    private var groupList = mutableListOf<Group>()
    var individualGroup: Group? = null

    fun setGroupList(newList: List<Group>?){
        if (newList.isNullOrEmpty()){
            groupList.clear()
        }else{
            groupList = newList.toMutableList()
        }
        if (individualGroup != null){
            groupList.add(0, individualGroup!!)
        }
        notifyDataSetChanged()
    }

    class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title_group)
    }

    class IndividualGroupViewGolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title_group)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        if (viewType == 1){
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_group_individual, parent, false)
            return GroupViewHolder(itemView)
        }else{
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_group, parent, false)
            return GroupViewHolder(itemView)
        }

    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val currentItem = groupList[position]
        if (!currentItem.isIndividual){
            holder.title.text = currentItem.title
        }else{
            holder.title.text = "Индивидуальное"
        }

        holder.itemView.setOnClickListener {
            fragment.setGroup(currentItem)

        }

    }

    override fun getItemCount(): Int {
        return groupList.size
    }

    fun addIndividual(group: Group) {
        individualGroup = group
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        val currentItem = groupList[position]
        if (currentItem.isIndividual){
            return 1
        }else{
            return 0
        }
    }

}