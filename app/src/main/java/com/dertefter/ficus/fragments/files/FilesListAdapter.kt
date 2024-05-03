package com.dertefter.ficus.fragments.files

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.ficus.R

class FilesListAdapter(val fragment: FilesFragment) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var filesList = mutableListOf<String>()
    fun setFileList(newList: List<String>?){
        if (newList != null){
            filesList = newList.toMutableList()
        }else{
            filesList = mutableListOf()
        }
        if (filesList.isEmpty()) {
            fragment.binding.noFiles.visibility = View.VISIBLE
        }else{
            fragment.binding.noFiles.visibility = View.GONE
        }
        notifyDataSetChanged()
    }


    class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val file_name: TextView = itemView.findViewById(R.id.file_name)
        val file_ex : TextView = itemView.findViewById(R.id.file_ex)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val file = filesList[position]
        val fileViewHolder = holder as FileViewHolder
        fileViewHolder.file_name.text = file
        if (file.contains(".")){
            fileViewHolder.file_ex.text = file.substringAfterLast(".")
        }else{
            fileViewHolder.file_ex.text = "?"
        }
        fileViewHolder.itemView.setOnClickListener {
            fragment.openFile(path = file)
        }
        fileViewHolder.itemView.setOnLongClickListener {
            fragment.showMenu(fileViewHolder.itemView, R.menu.file_list_mwnu, file)
            true
        }

    }


    override fun getItemCount(): Int {
        return filesList.size
    }

}