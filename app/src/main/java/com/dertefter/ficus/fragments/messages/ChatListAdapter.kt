package com.dertefter.ficus.fragments.messages

import android.animation.ObjectAnimator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.ficus.MainActivity
import com.dertefter.ficus.R
import com.dertefter.neticore.data.Person
import com.dertefter.neticore.data.messages.SenderPerson
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso

class ChatListAdapter(val fragment: MessagesTabFragment) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var chatList = mutableListOf<SenderPerson>()
    var isError = false

    fun setChatList(chatList_: List<SenderPerson>?){
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
        val mail: TextView = itemView.findViewById(R.id.mail)
        val last_message_theme: TextView = itemView.findViewById(R.id.last_message_theme)
        val last_message_text: TextView = itemView.findViewById(R.id.last_message_text)
        val avatar: ShapeableImageView = itemView.findViewById(R.id.person_avatar_placeholder)
        val isNew: View = itemView.findViewById(R.id.isNew)
        val liveData: MutableLiveData<Person?> = MutableLiveData()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ChatItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_messages_chat_item, parent, false))

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chatItem = chatList[position]
        val chatItemViewHolder = holder as ChatItemViewHolder
        chatItemViewHolder.name.text = chatItem.name
        chatItemViewHolder.last_message_theme.text = chatItem.last_message_theme
        chatItemViewHolder.last_message_text.text = chatItem.last_message_text
        val isNew = chatItem.messages.any { it.isNew }
        if (isNew){
            chatItemViewHolder.isNew.visibility = View.VISIBLE
        }else{
            chatItemViewHolder.isNew.visibility = View.GONE
        }
        ViewCompat.setTransitionName(holder.itemView, chatItem.name)
        holder.itemView.setOnClickListener {
            fragment.openChat(holder.itemView, chatItem)
        }
        (fragment.activity as MainActivity).netiCore!!.personHelper!!.retrivePersonByName(chatItem.name, chatItemViewHolder.liveData)
        holder.liveData.observeForever {
            if (it != null){
                if (!it.pic.isNullOrEmpty()){
                    Picasso.get().load(it.pic).resize(200,200).centerCrop().into(chatItemViewHolder.avatar)
                    ObjectAnimator.ofFloat(chatItemViewHolder.avatar, "alpha", 0f, 1f).setDuration(100).start()
                }
                chatItemViewHolder.mail.text = it.mail
                ObjectAnimator.ofFloat(chatItemViewHolder.mail, "alpha", 0f, 1f).setDuration(100).start()

            }
        }
    }


    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

}