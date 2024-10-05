package com.dertefter.ficus.fragments.e_library

import android.graphics.Color
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.ficus.ImageGetter
import com.dertefter.ficus.R
import com.dertefter.neticore.data.Person
import com.dertefter.neticore.data.e_library.ELibrarySearchItem
import com.dertefter.neticore.data.schedule.Group
import com.google.android.material.color.MaterialColors
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso

class ELibrarySearchListAdapter(val fragment: ELibrarySearchFragment) : RecyclerView.Adapter<ELibrarySearchListAdapter.ItemViewHolder>() {
    private var searchItemList = mutableListOf<ELibrarySearchItem>()

    fun setData(newList: List<ELibrarySearchItem>?){
        if (newList.isNullOrEmpty()){
            searchItemList.clear()
        }else{
            searchItemList = newList.toMutableList()
        }
        notifyDataSetChanged()
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_elibrary_search, parent, false)
        return ItemViewHolder(itemView)

    }

    override fun getItemCount(): Int {
        return searchItemList.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = searchItemList[position]
        displayHtml(currentItem.title!!, holder.title)

    }


    private fun displayHtml(html: String, tv: TextView) {

        // Creating object of ImageGetter class you just created
        val imageGetter = ImageGetter(fragment.resources, tv)

        // Using Html framework to parse html
        val styledText= HtmlCompat.fromHtml(html,
            HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH,
            imageGetter,null)

        // to enable image/link clicking
        tv.movementMethod = LinkMovementMethod.getInstance()

        // setting the text after formatting html and downloading and setting images
        tv.text = styledText



    }

}