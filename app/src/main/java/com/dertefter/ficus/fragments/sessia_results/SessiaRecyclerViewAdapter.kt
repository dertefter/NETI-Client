package com.dertefter.ficus.fragments.sessia_results

import android.content.res.Configuration
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.ficus.R
import com.dertefter.neticore.data.sessia_results.SessiaItem
import com.google.android.material.progressindicator.CircularProgressIndicator


class SessiaRecyclerViewAdapter(val fragment: SessiaItemFragment): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var listOfLesson: List<SessiaItem>? = null

    fun setItems(list: List<SessiaItem>?){
        if (list == null){
            listOfLesson = listOf()
        }else{
            listOfLesson = list
        }
        notifyDataSetChanged()
    }

    class viewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val value: TextView = itemView.findViewById(R.id.value)
        val valueFivePoint: TextView = itemView.findViewById(R.id.valueFivePoint)
        val valueECTS: TextView = itemView.findViewById(R.id.valueECTS)
        val progressBar: CircularProgressIndicator = itemView.findViewById(R.id.progressBar)
        var progress: Int = 0

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sessia_result, parent, false)
        return viewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = listOfLesson?.get(position)
        val viewHolder = holder as viewHolder
        viewHolder.title.text = item?.title
        viewHolder.value.text = item?.value
        if (item?.valueECTS?.isEmpty() == true){
            (viewHolder.valueECTS.parent as View).visibility = View.GONE
        }else{
            (viewHolder.valueECTS.parent as View).visibility = View.INVISIBLE
            viewHolder.valueECTS.text = item?.valueECTS
        }
        if (item?.valueFivePoint?.isEmpty() == true){
            (viewHolder.valueFivePoint.parent as View).visibility = View.GONE
        }else{
            (viewHolder.valueFivePoint.parent as View).visibility = View.VISIBLE
            viewHolder.valueFivePoint.text = item?.valueFivePoint
        }

        val progress: Int = if (item?.value?.contains("Н") == true){
            0
        }else{
            item?.value?.toInt() ?: 0
        }
        var colorStart: Int = Color.parseColor("#9c2a21")
        var colorEnd: Int = Color.parseColor("#269143")
        when (viewHolder.itemView.context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                colorStart = Color.parseColor("#ff5e33")
                colorEnd = Color.parseColor("#98f196")
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                colorStart = Color.parseColor("#992820")
                colorEnd = Color.parseColor("#177336")
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }
        viewHolder.progress = progress
        val backgroundColor = (interpolateColor(colorStart, colorEnd,
            (progress) / 100f - 0.2f))
        viewHolder.progressBar.setIndicatorColor(backgroundColor)
        viewHolder.value.setTextColor(backgroundColor)
        viewHolder.valueFivePoint.setTextColor(backgroundColor)
        viewHolder.valueECTS.setTextColor(backgroundColor)
        viewHolder.progressBar.setProgressCompat(progress, true)
    }

    private fun interpolate(a: Float, b: Float, proportion: Float): Float {
        return a + ((b - a) * proportion)
    }

    private fun getNegativeColor(color: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[0] = (hsv[0] + 180) % 360
        return Color.HSVToColor(hsv)
    }

    private fun interpolateColor(a: Int, b: Int, proportion: Float): Int {
        val hsva = FloatArray(3)
        val hsvb = FloatArray(3)
        Color.colorToHSV(a, hsva)
        Color.colorToHSV(b, hsvb)
        for (i in 0..2) {
            hsvb[i] = interpolate(hsva[i], hsvb[i], proportion)
        }
        return Color.HSVToColor(hsvb)
    }

    override fun getItemCount(): Int {
        return listOfLesson?.size ?: 0
    }

}