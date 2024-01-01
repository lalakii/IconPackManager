package cn.lalaki.tinydesk

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.widget.ImageView

class ImageViewEx(context: Context?) : ImageView(context) {
    private var labelText = ""
    private var paint: Paint? = null

    fun setLabel(text: String) {
        labelText = text
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (paint == null) {
            paint = Paint()
            paint!!.color = Color.BLACK
            paint!!.textSize = 38f
        }
        val textWidth = paint!!.measureText(labelText)
        val yPos = height - 30f
        val xPos: Float
        if (textWidth > width) {
            val endIndex = paint!!.breakText(
                labelText, 0, labelText.length, true, width.toFloat(), null
            )
            labelText = labelText.substring(0, endIndex - 2) + "..."
            xPos = (width - paint!!.measureText(labelText)) / 2
        } else {
            xPos = (width - textWidth) / 2
        }
        canvas.drawText(labelText, xPos, yPos, paint!!)
    }
}