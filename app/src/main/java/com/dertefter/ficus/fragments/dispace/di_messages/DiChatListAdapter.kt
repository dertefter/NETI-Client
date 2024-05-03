package com.dertefter.ficus.fragments.dispace.di_messages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.ficus.R
import com.dertefter.neticore.data.dispace.di_messages.DiSenderPerson
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso

class DiChatListAdapter(val fragment: DiSpaceMessages) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var chatList = mutableListOf<DiSenderPerson>()
    var isError = false

    fun setChatList(chatList_: List<DiSenderPerson>?){
        if (!chatList_.isNullOrEmpty()) {
            chatList = chatList_.toMutableList()
            notifyDataSetChanged()
        }else{
            chatList.clear()
            notifyDataSetChanged()
        }
    }

    class ChatItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.name)
        val last_message_text: TextView = itemView.findViewById(R.id.last_message_text)
        val avatar: ShapeableImageView = itemView.findViewById(R.id.person_avatar_placeholder)
        val isNew: View = itemView.findViewById(R.id.isNew)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ChatItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_di_messages_chat_item, parent, false))

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chatItem = chatList[position]
        val chatItemViewHolder = holder as ChatItemViewHolder
        chatItemViewHolder.name.text = "${chatItem.surname} ${chatItem.name} ${chatItem.patronymic}"
        chatItemViewHolder.last_message_text.text = chatItem.last_msg
        if (!chatItem.photo.isNullOrEmpty()){
                Picasso.get().load("https://dispace.edu.nstu.ru/files/images/photos/b_IMG_"+chatItem.photo).resize(200,200).centerCrop().into(holder.avatar)
            holder.avatar.visibility = View.VISIBLE
        }else{
            holder.avatar.visibility = View.GONE
        }
        val isNew = chatItem.is_new == "1"
        if (isNew){
            chatItemViewHolder.isNew.visibility = View.VISIBLE
        }else{
            chatItemViewHolder.isNew.visibility = View.GONE
        }
        holder.itemView.setOnClickListener {
            fragment.openChat(chatItem)
        }
    }


    override fun getItemCount(): Int {
        return chatList.size
    }

}