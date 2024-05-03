package com.dertefter.ficus.fragments.dispace.di_messages.chat_view

import android.animation.ObjectAnimator
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.ficus.ImageGetter
import com.dertefter.ficus.R
import com.dertefter.neticore.data.dispace.di_messages.DiMessage
import com.dertefter.neticore.data.dispace.di_messages.DiSenderPerson
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso

const val VIEW_TYPE_MY_MESSAGE = 0
const val VIEW_TYPE_NOT_MY_MESSAGE = 1
const val VIEW_TYPE_LOADING = 2

class DiMessageListAdapter(val fragment: DiChatViewFragment, val senderId: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var messageList = mutableListOf<DiMessage>()
    var remain = 0

    fun setMessageList(chatList_: List<DiMessage>?){
        if (!chatList_.isNullOrEmpty()) {
            messageList = chatList_.toMutableList()
        }else{
            messageList.clear()
        }
        if (remain > 0){
            messageList.add(0, DiMessage("","", "", "", "", "", "", "", "", "","",""))

        }
        notifyDataSetChanged()
    }


    fun getItem(position: Int): DiMessage? {
        return messageList[position]
    }

    fun addMessageList(chatList_: List<DiMessage>?, recyclerView: RecyclerView){
        if (!chatList_.isNullOrEmpty()) {
            for (item in chatList_.reversed()){
                messageList.add(0, item)
                notifyItemInserted(0)
            }
            messageList.find { it.sender_id == "" }?.let {
                messageList.remove(it)}
            if (remain > 0){
                messageList.add(0, DiMessage("","", "", "", "", "", "", "", "", "","",""))
                notifyItemInserted(0)
            }

        }
    }

    class MyMessageItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val content : TextView = itemView.findViewById(R.id.message_content)
        val date : TextView = itemView.findViewById(R.id.message_date)
    }
    class NotMyMessageItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val content : TextView = itemView.findViewById(R.id.message_content)
        val date : TextView = itemView.findViewById(R.id.message_date)
    }
    class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_LOADING -> LoadingViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_message_loading, parent, false))
            VIEW_TYPE_MY_MESSAGE -> MyMessageItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_message_mine, parent, false))
            else -> NotMyMessageItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_message_not_mine, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MyMessageItemViewHolder){
            val chatItem = messageList[position]
            displayHtml(chatItem.message!!, holder.itemView.findViewById(R.id.message_content))
            holder.date.text = chatItem.date
        }else if (holder is NotMyMessageItemViewHolder){
            val chatItem = messageList[position]
            displayHtml(chatItem.message!!, holder.itemView.findViewById(R.id.message_content))
            holder.date.text = chatItem.date
        }
        ObjectAnimator.ofFloat(holder.itemView, "alpha", 0f, 1f).apply {
            duration = 169
            start()
        }
    }


    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        val currentItem = messageList[position]
        if (currentItem.sender_id == ""){
            return VIEW_TYPE_LOADING
        }else if (currentItem.sender_id == senderId){
            return VIEW_TYPE_NOT_MY_MESSAGE
        }else{
            return VIEW_TYPE_MY_MESSAGE
        }

    }

    private fun displayHtml(html: String, textView: TextView) {

        val imageGetter = ImageGetter(fragment.resources, textView)
        val styledText= HtmlCompat.fromHtml(html,
            HtmlCompat.FROM_HTML_MODE_COMPACT,
            imageGetter,null)
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.text = styledText
    }

}