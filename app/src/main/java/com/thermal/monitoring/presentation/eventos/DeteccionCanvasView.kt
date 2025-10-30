package com.thermal.monitoring.presentation.eventos

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.thermal.monitoring.R
import com.thermal.monitoring.data.remote.Deteccion

class DeteccionCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = context.getColor(R.color.deteccion_box)
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = context.getColor(R.color.deteccion_box)
        textSize = 40f
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private var detecciones: List<Deteccion> = emptyList()
    private var imageWidth: Int = 0
    private var imageHeight: Int = 0
    private var imageViewWidth: Int = 0
    private var imageViewHeight: Int = 0

    fun setDetecciones(
        detecciones: List<Deteccion>,
        imgWidth: Int,
        imgHeight: Int,
        ivWidth: Int,
        ivHeight: Int
    ) {
        this.detecciones = detecciones
        this.imageWidth = imgWidth
        this.imageHeight = imgHeight
        this.imageViewWidth = ivWidth
        this.imageViewHeight = ivHeight
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (imageWidth == 0 || imageHeight == 0 || detecciones.isEmpty() ||
            imageViewWidth == 0 || imageViewHeight == 0) return

        // Calcular escala manteniendo aspect ratio (fitCenter)
        val scaleX = imageViewWidth.toFloat() / imageWidth.toFloat()
        val scaleY = imageViewHeight.toFloat() / imageHeight.toFloat()
        val scale = minOf(scaleX, scaleY)

        // Calcular dimensiones reales de la imagen renderizada
        val scaledWidth = imageWidth * scale
        val scaledHeight = imageHeight * scale

        // Calcular offset para centrar
        val offsetX = (imageViewWidth - scaledWidth) / 2f
        val offsetY = (imageViewHeight - scaledHeight) / 2f

        // Dibujar cada detección
        detecciones.forEach { deteccion ->
            val left = deteccion.x1 * scale + offsetX
            val top = deteccion.y1 * scale + offsetY
            val right = deteccion.x2 * scale + offsetX
            val bottom = deteccion.y2 * scale + offsetY

            val rect = RectF(left, top, right, bottom)
            canvas.drawRect(rect, paint)

            val label = "${(deteccion.confianza * 100).toInt()}%"
            canvas.drawText(label, left, maxOf(top - 10, textPaint.textSize), textPaint)
        }
    }

    fun clear() {
        detecciones = emptyList()
        invalidate()
    }
}