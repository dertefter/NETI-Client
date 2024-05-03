package com.dertefter.ficus.fragments.news

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.ficus.R
import com.dertefter.neticore.data.news.News
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class NewsListAdapter(val fragment: NewsFragment) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var newsList = mutableListOf<News>()
    var isError = false
    fun setNewsList(newNewsList: List<News>?){
        if (newNewsList != null){
            newsList = newNewsList.toMutableList()
        }
        notifyDataSetChanged()
    }

    fun getLastPosition(): Int{
        return newsList.size
    }

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title_news)
        val tag: TextView = itemView.findViewById(R.id.tag_news)
        val date: TextView = itemView.findViewById(R.id.date_news)
        val image: ImageView = itemView.findViewById(R.id.image_news)
        val type: TextView = itemView.findViewById(R.id.type_news)
        var newsid: String = ""
        var color = Color.GRAY
    }

    class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    class ErrorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val retryButton: TextView = itemView.findViewById(R.id.retry_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0){
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
            NewsViewHolder(itemView)
        } else if (viewType == 2){
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_error, parent, false)
            ErrorViewHolder(itemView)
        } else{
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_loading_news, parent, false)
            LoadingViewHolder(itemView)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is NewsViewHolder){
            val currentItem = newsList[position]
            holder.title.text = currentItem.title
            holder.tag.text = currentItem.tag
            holder.date.text = currentItem.date
            holder.newsid = currentItem.newsid
            holder.type.text = currentItem.type
            if (currentItem.imageUrl != null){
                Picasso.get().load(currentItem.imageUrl).into(holder.image)
                Picasso.get()
                    .load(currentItem.imageUrl)
                    .resize(600,400)
                    .centerCrop()
                    .into(holder.image, object : Callback {
                        override fun onSuccess() {
                            holder.color = fragment.getDominantColor(holder.image.drawable.toBitmap())
                        }

                        override fun onError(ex: Exception) {

                        }
                    })

                holder.image.visibility = View.VISIBLE
                holder.type.visibility = View.VISIBLE
            }else{
                holder.image.visibility = View.GONE
                holder.type.visibility = View.GONE
                holder.color = Color.GRAY
            }
            ViewCompat.setTransitionName(holder.itemView, "image$position")
            holder.itemView.setOnClickListener {
                fragment.readNews(holder.itemView,
                    currentItem.title,
                    holder.newsid,
                    currentItem.date,
                    currentItem.imageUrl,
                    holder.color)
            }
        } else if (holder is LoadingViewHolder){
            if (isError){
                holder.itemView.visibility = View.GONE
                holder.itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
            }else{
                holder.itemView.visibility = View.VISIBLE
                holder.itemView.layoutParams =
                    RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
            }
        }else if (holder is ErrorViewHolder){
            holder.retryButton.setOnClickListener {
                fragment.getNews()
            }
        }


    }

    override fun getItemViewType(position: Int): Int {
        if (position == newsList.size || position== newsList.size+1){
            return 1
        }else{
            if (newsList[position].error != null && newsList[position].error != ""){
                isError = true
                return 2
            }else{
                isError = false
            }
            return 0
        }
    }

    override fun getItemCount(): Int {
        return newsList.size + 2
    }

}