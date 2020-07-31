package io.github.davoleo.barcodestock.scanner

import android.graphics.*
import androidx.core.content.ContextCompat
import io.github.davoleo.barcodestock.R

class BarcodeGraphic(overlay: GraphicOverlay, mColor: Int) : GraphicOverlay.Graphic(overlay) {

    private val boxPaint: Paint = Paint().apply {
        color = mColor
        style = Paint.Style.STROKE
        strokeWidth = context.resources.getDimension(R.dimen.reticle_stroke_width)
    }

    private val eraserPaint: Paint = Paint().apply {
        strokeWidth = boxPaint.strokeWidth
        //Xfermode is a way to customize Paint objects
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private val translucentPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.actionBarOpacity)
    }

    val cornerRadius: Float = context.resources.getDimensionPixelOffset(R.dimen.reticle_corner_radius).toFloat();

    val rect = getReticleBox(overlay)

    override fun draw(canvas: Canvas) {
        //Draw the translucent background
        canvas.drawRect(0F, 0F, canvas.width.toFloat(), canvas.height.toFloat(), translucentPaint)

        //Erase the rectangle area of the barcode
        eraserPaint.style = Paint.Style.FILL
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, eraserPaint)
        eraserPaint.style = Paint.Style.STROKE
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, eraserPaint)

        //Draw the actual box
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, boxPaint)

        //canvas.drawPoint(canvas.width / 2F, canvas.height / 2F, boxPaint)
    }


    companion object {
        fun getReticleBox(overlay: GraphicOverlay): RectF {
            val context = overlay.context
            val overlayWidth = overlay.width.toFloat()
            val overlayHeight = overlay.height.toFloat()
            val boxWidth = overlayWidth * 80 / 100
            val boxHeight = overlayHeight * 30 / 100
            val centerX = overlayWidth / 2
            val centerY = overlayHeight / 2

            return RectF(centerX - boxWidth / 2, centerY - boxHeight / 2, centerX + boxWidth / 2, centerY + boxHeight / 2)
        }
    }
}