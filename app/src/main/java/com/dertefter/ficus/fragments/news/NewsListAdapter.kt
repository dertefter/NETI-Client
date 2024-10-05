package com.dertefter.ficus.fragments.news

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.ficus.R
import com.dertefter.neticore.data.news.News
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.google.android.material.color.MaterialColors
import com.google.android.material.imageview.ShapeableImageView
import com.smarttoolfactory.extendedcolors.util.RGBUtil.toArgbString
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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
        val image: ShapeableImageView = itemView.findViewById(R.id.image_news)
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
        return if (viewType == 1000){
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_loading_news, parent, false)
            LoadingViewHolder(itemView)
        } else if (viewType == 2000){
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_error, parent, false)
            ErrorViewHolder(itemView)
        } else{
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
            NewsViewHolder(itemView)
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
                            CoroutineScope(Dispatchers.IO).launch {
                                val bitmap = holder.image.drawable.toBitmap()
                                val compressed = Bitmap.createScaledBitmap(bitmap, 10, 6, false)
                                holder.color = fragment.getDominantColor(compressed)
                                val c: Context = DynamicColors.wrapContextIfAvailable(
                                    holder.itemView.context,
                                    DynamicColorsOptions.Builder()
                                        .setContentBasedSource(holder.color)
                                        .build()
                                )
                                withContext(Dispatchers.Main){
                                    (holder.itemView as MaterialCardView).setCardBackgroundColor(MaterialColors.getColor(c, com.google.android.material.R.attr.colorSurfaceContainerHigh, Color.GRAY))
                                    holder.title.setTextColor(MaterialColors.getColor(c, com.google.android.material.R.attr.colorOnSurface, Color.GRAY))
                                    holder.tag.setTextColor(MaterialColors.getColor(c, com.google.android.material.R.attr.colorOnSurfaceVariant, Color.GRAY))
                                    holder.type.setTextColor(MaterialColors.getColor(c, com.google.android.material.R.attr.colorOnPrimary, Color.GRAY))
                                    (holder.type.parent as MaterialCardView).backgroundTintList = ColorStateList.valueOf(MaterialColors.getColor(c, com.google.android.material.R.attr.colorPrimary, Color.GRAY))
                                }
                            }
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

    override fun getItemId(position: Int): Long {
        return newsList[position].newsid.hashCode().toLong()
    }

    override fun getItemViewType(position: Int): Int {
        if (position == newsList.size || position == newsList.size+1){
            return 1000
        }else{
            if (newsList[position].error != null && newsList[position].error != ""){
                isError = true
                return 2000
            }else{
                isError = false
            }
            return position
        }
    }

    override fun getItemCount(): Int {
        return newsList.size + 2
    }

}