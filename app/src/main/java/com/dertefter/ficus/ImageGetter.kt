package com.dertefter.ficus

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.util.Base64
import android.widget.TextView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream

class ImageGetter(
    private val res: Resources,
    private val htmlTextView: TextView
) : Html.ImageGetter {

    override fun getDrawable(url: String): Drawable {
        val holder = BitmapDrawablePlaceHolder(res, null)

        GlobalScope.launch(Dispatchers.IO) {
            runCatching {
                val drawable: Drawable = if (url.startsWith("data:image/png;base64,")) {
                    val base64Image = url.substring("data:image/png;base64,".length)
                    val decodedString = Base64.decode(base64Image, Base64.DEFAULT)
                    val decodedByte = BitmapFactory.decodeStream(ByteArrayInputStream(decodedString))
                    BitmapDrawable(res, decodedByte)
                } else {
                    val bitmap = Picasso.get().load(url).get()
                    BitmapDrawable(res, bitmap)
                }

                val width = htmlTextView.width - htmlTextView.paddingLeft - htmlTextView.paddingRight
                val aspectRatio: Float = drawable.intrinsicWidth.toFloat() / drawable.intrinsicHeight.toFloat()
                val height = width / aspectRatio
                drawable.setBounds(10, 20, width, height.toInt())
                holder.setDrawable(drawable)
                holder.setBounds(10, 20, width, height.toInt())
                withContext(Dispatchers.Main) {
                    htmlTextView.text = htmlTextView.text
                }
            }
        }
        return holder
    }

    internal class BitmapDrawablePlaceHolder(res: Resources, bitmap: Bitmap?) :
        BitmapDrawable(res, bitmap) {
        private var drawable: Drawable? = null

        override fun draw(canvas: Canvas) {
            drawable?.run { draw(canvas) }
        }

        fun setDrawable(drawable: Drawable) {
            this.drawable = drawable
        }
    }

    fun getScreenWidth() = Resources.getSystem().displayMetrics.widthPixels
}