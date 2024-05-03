package com.dertefter.ficus.fragments.dispace.di_cources.course_view

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.ficus.ImageGetter
import com.dertefter.ficus.R
import com.dertefter.ficus.fragments.other.ImageDialogFragment
import com.dertefter.neticore.data.dispace.di_cources.DiCourse
import com.dertefter.neticore.data.dispace.di_cources.DiCourseContent
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
import com.squareup.picasso.Picasso

class DiCourseViewAdapter(val fragment: CourseViewFragment) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var dataList = mutableListOf<DiCourseContent>()
    var savedFileList = mutableListOf<String>()

    fun setData(data: List<DiCourseContent>?){
        if (!data.isNullOrEmpty()) {
            dataList = data.toMutableList()
        }else{
            dataList.clear()
        }
        notifyDataSetChanged()
    }


    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
    }

    class ContentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val content: TextView = itemView.findViewById(R.id.content)
    }

    class AttachmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.attachment_name)
        val icon : ImageView = itemView.findViewById(R.id.attachment_icon)
    }

    class ImageChipViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val linearLayout: LinearLayout = itemView.findViewById(R.id.linear_layout)
    }

    class InfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.info_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType){
            0 -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.di_course_view_header, parent, false)
                HeaderViewHolder(view)
            }
            1 -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.di_course_view_content, parent, false)
                ContentViewHolder(view)
            }
            2 -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.di_course_view_attachment, parent, false)
                AttachmentViewHolder(view)
            }
            3 -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.di_course_view_image_chip, parent, false)
                ImageChipViewHolder(view)
            }
            4 -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.di_course_view_info, parent, false)
                InfoViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.di_course_view_content, parent, false)
                ContentViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = dataList[position]
        when (holder){
            is HeaderViewHolder -> {
                holder.title.text = currentItem.htmlContent
            }
            is ContentViewHolder -> {
                displayHtml(currentItem.htmlContent!!, holder.content)
            }
            is AttachmentViewHolder -> {
                holder.name.text = currentItem.attachmentName
                if (currentItem.attachmentName in savedFileList){
                    holder.icon.setImageResource(R.drawable.done)
                    holder.itemView.findViewById<ShimmerFrameLayout>(R.id.shim).visibility = View.GONE
                    holder.itemView.setOnClickListener {
                        fragment.findNavController().navigate(R.id.filesFragment)
                    }
                }else{
                    holder.icon.setImageResource(R.drawable.download)
                    holder.itemView.setOnClickListener {
                        holder.itemView.findViewById<ShimmerFrameLayout>(R.id.shim).visibility = View.VISIBLE
                        Log.e("attachment", "clicked ${currentItem.attachmentLink} ${currentItem.attachmentName}")
                        fragment.downloadFile(currentItem.attachmentLink!!, currentItem.attachmentName!!)
                    }
                }

            }
            is InfoViewHolder -> {
                holder.text.text = currentItem.htmlContent
            }
            is ImageChipViewHolder -> {
                for (i in currentItem.imagesChips!!){
                    val imageView = ImageView(fragment.requireContext())
                    imageView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    holder.linearLayout.addView(imageView)
                    imageView.setOnClickListener {
                        val bundle = Bundle()
                        bundle.putString("image", i)
                        val imageDialogFragment = ImageDialogFragment()
                        imageDialogFragment.arguments = bundle
                        imageDialogFragment.show(fragment.parentFragmentManager, "image")
                    }
                    Picasso.get().load(i).resize(500, 0).into(imageView)

                }
            }
        }

    }


    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun getItemViewType(position: Int): Int {
        val currentItem = dataList[position]
        return when (currentItem.type){
            "header" -> 0
            "typography" -> 1
            "attachment" -> 2
            "image_chip" -> 3
            "info" -> 4
            else -> 1
        }
    }

    private fun displayHtml(html: String, textView: TextView) {

        // Creating object of ImageGetter class you just created
        val imageGetter = ImageGetter(fragment.resources, textView)

        // Using Html framework to parse html
        val styledText= HtmlCompat.fromHtml(html,
            HtmlCompat.FROM_HTML_MODE_COMPACT,
            imageGetter,null)

        // to enable image/link clicking
        textView.movementMethod = LinkMovementMethod.getInstance()

        // setting the text after formatting html and downloading and setting images
        textView.text = styledText
    }

}