/* -----------------------------------
 * Author: Davoleo
 * Date / Hour: 17/07/2020 / 20:01
 * Class: GraphicOverlay
 * Project: BarcodeStock
 * Copyright - © - Davoleo - 2020
 * ----------------------------------- */

package io.github.davoleo.barcodestock.scanner

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.google.android.gms.vision.CameraSource
import java.util.*

class GraphicOverlay(context: Context?, attributes: AttributeSet?) : View(context, attributes) {

    private val lock = Any()
    private val graphics = ArrayList<Graphic>()
    // Matrix to transform from image coordinates to overlay view coordinates.
    private val transformationMatrix = Matrix()
    private var previewWidth = 0
    private var previewHeight = 0
    private var widthScaleFactor = 1F
    private var heightScaleFactor = 1F

    fun translateX(x: Float): Float {
        return x * widthScaleFactor
    }

    fun translateY(y: Float): Float {
        return y * heightScaleFactor
    }

    fun translateRect(rect: Rect): RectF {
        return RectF(
                translateX(rect.left.toFloat()),
                translateY(rect.top.toFloat()),
                translateX(rect.right.toFloat()),
                translateY(rect.bottom.toFloat())
        )
    }

    fun clear() {
        synchronized(lock) {
            graphics.clear()
        }
        postInvalidate()
    }

    fun add(graphic: Graphic) {
        synchronized(lock) {
            graphics.add(graphic)
        }
    }

    fun setCameraInfo(cameraSource: CameraSource) {
        val previewSize = cameraSource.previewSize ?: return

        if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            previewWidth = previewSize.height
            previewHeight = previewSize.width
        }
        else {
            previewHeight = previewSize.height
            previewWidth = previewSize.width
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (previewWidth > 0 && previewHeight > 0) {
            widthScaleFactor = width.toFloat() / previewWidth
            heightScaleFactor = height.toFloat() / previewHeight
        }

        synchronized(lock) {
            graphics.forEach { it.draw(canvas) }
        }
    }

    abstract class Graphic(protected val overlay: GraphicOverlay) {
        protected val context: Context = overlay.context;

        abstract fun draw(canvas: Canvas)

    }
}