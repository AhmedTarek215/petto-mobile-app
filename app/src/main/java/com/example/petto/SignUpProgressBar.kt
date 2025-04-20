package com.example.petto

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class SignUpProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val totalSteps = 4
    private var currentStep = 1
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.progress_active)
        style = Paint.Style.FILL
    }
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.progress_inactive)
        style = Paint.Style.FILL
    }

    fun setProgress(step: Int) {
        currentStep = step.coerceIn(1, totalSteps)
        invalidate()
        requestLayout()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val segmentWidth = width / totalSteps.toFloat()
        val segmentHeight = height.toFloat()

        for (i in 0 until totalSteps) {
            val paint = if (i < currentStep) progressPaint else backgroundPaint
            canvas.drawRect(
                i * segmentWidth, 0f,
                (i + 1) * segmentWidth, segmentHeight,
                paint
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = 16 // Set your preferred height
        val height = resolveSize(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height)
    }

}
