package com.thermal.monitoring.presentation.eventos

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.thermal.monitoring.data.remote.Deteccion

class DeteccionCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.GREEN
        textSize = 40f
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private var detecciones: List<Deteccion> = emptyList()
    private var imageWidth: Int = 0
    private var imageHeight: Int = 0
    private var viewWidth: Int = 0
    private var viewHeight: Int = 0

    fun setDetecciones(detecciones: List<Deteccion>, imgWidth: Int, imgHeight: Int) {
        this.detecciones = detecciones
        this.imageWidth = imgWidth
        this.imageHeight = imgHeight
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w
        viewHeight = h
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (imageWidth == 0 || imageHeight == 0 || detecciones.isEmpty()) return

        // Calcular el factor de escala para ajustar las coordenadas
        val scaleX = viewWidth.toFloat() / imageWidth.toFloat()
        val scaleY = viewHeight.toFloat() / imageHeight.toFloat()

        // Usar el menor factor de escala para mantener el aspect ratio
        val scale = minOf(scaleX, scaleY)

        // Calcular el offset para centrar la imagen
        val scaledWidth = imageWidth * scale
        val scaledHeight = imageHeight * scale
        val offsetX = (viewWidth - scaledWidth) / 2
        val offsetY = (viewHeight - scaledHeight) / 2

        // Dibujar cada detección
        detecciones.forEachIndexed { index, deteccion ->
            // Escalar y ajustar coordenadas
            val left = deteccion.x1 * scale + offsetX
            val top = deteccion.y1 * scale + offsetY
            val right = deteccion.x2 * scale + offsetX
            val bottom = deteccion.y2 * scale + offsetY

            val rect = RectF(left, top, right, bottom)

            // Dibujar rectángulo
            canvas.drawRect(rect, paint)

            // Dibujar etiqueta con confianza
            val label = "${(deteccion.confianza * 100).toInt()}%"
            canvas.drawText(label, left, top - 10, textPaint)
        }
    }

    fun clear() {
        detecciones = emptyList()
        invalidate()
    }
}