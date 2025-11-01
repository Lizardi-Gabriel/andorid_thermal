package com.thermal.monitoring.presentation.eventos

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
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
    private var imageView: ImageView? = null
    private val rectF = RectF()

    fun setImageView(imageView: ImageView) {
        this.imageView = imageView
    }

    fun setDetecciones(detecciones: List<Deteccion>) {
        this.detecciones = detecciones
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (detecciones.isEmpty() || imageView == null) return

        val drawable = imageView?.drawable ?: return

        // Dimensiones intrinsecas de la imagen mostrada
        val imageWidth = drawable.intrinsicWidth.toFloat()
        val imageHeight = drawable.intrinsicHeight.toFloat()

        if (imageWidth <= 0 || imageHeight <= 0) return

        // Dimensiones del ImageView
        val viewWidth = imageView?.width?.toFloat() ?: 0f
        val viewHeight = imageView?.height?.toFloat() ?: 0f

        if (viewWidth <= 0 || viewHeight <= 0) return

        // Encontrar dimensiones maximas en las coordenadas de deteccion
        val maxX = detecciones.maxOfOrNull { maxOf(it.x1.toFloat(), it.x2.toFloat()) } ?: imageWidth
        val maxY = detecciones.maxOfOrNull { maxOf(it.y1.toFloat(), it.y2.toFloat()) } ?: imageHeight

        // Calcular factor de escala de coordenadas de deteccion a imagen mostrada
        val coordScaleX = if (maxX > imageWidth) imageWidth / maxX else 1f
        val coordScaleY = if (maxY > imageHeight) imageHeight / maxY else 1f
        val coordScale = minOf(coordScaleX, coordScaleY)

        // Calcular el escalado y offset que aplica fitCenter para mostrar en el view
        val viewScale = minOf(viewWidth / imageWidth, viewHeight / imageHeight)
        val scaledWidth = imageWidth * viewScale
        val scaledHeight = imageHeight * viewScale

        // Offset para centrar la imagen en el view
        val offsetX = (viewWidth - scaledWidth) / 2f
        val offsetY = (viewHeight - scaledHeight) / 2f

        // Factor de escala total
        val totalScale = coordScale * viewScale

        detecciones.forEach { deteccion ->
            // Aplicar escala de coordenadas + escala de vista + offset
            val x1 = deteccion.x1 * totalScale + offsetX
            val y1 = deteccion.y1 * totalScale + offsetY
            val x2 = deteccion.x2 * totalScale + offsetX
            val y2 = deteccion.y2 * totalScale + offsetY

            rectF.set(x1, y1, x2, y2)
            canvas.drawRect(rectF, paint)

            // Dibujar texto con porcentaje de confianza
            val label = "${(deteccion.confianza * 100).toInt()}%"
            canvas.drawText(label, rectF.left, maxOf(rectF.top - 10, textPaint.textSize), textPaint)
        }
    }

    fun clear() {
        detecciones = emptyList()
        imageView = null
        invalidate()
    }

}