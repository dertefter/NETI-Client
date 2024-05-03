package com.dertefter.ficus.fragments.messages.chat_view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.ficus.R
import com.dertefter.neticore.data.messages.Message

class MessagesListAdapter(val fragment: MessagesChatViewFragment) : RecyclerView.Adapter<MessagesListAdapter.MessageViewHolder>() {
    private var messagesList = mutableListOf<Message>()

    fun setMessageList(newList: List<Message>?){
        if (newList.isNullOrEmpty()){
            messagesList.clear()
        }else{
            messagesList = newList.toMutableList()
        }
        notifyDataSetChanged()
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title : TextView = itemView.findViewById(R.id.title)
        val text : TextView = itemView.findViewById(R.id.text)
        val isNew : ImageView = itemView.findViewById(R.id.isNew)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val currentItem = messagesList[position]
        holder.title.text = currentItem.title
        holder.text.text = currentItem.text
        if (currentItem.isNew){
            holder.isNew.visibility = View.VISIBLE
        } else {
            holder.isNew.visibility = View.GONE
        }
        holder.itemView.setOnClickListener {
            fragment.openMessage(currentItem)
        }
    }

    override fun getItemCount(): Int {
        return messagesList.size
    }

}