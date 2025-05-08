package com.example.petto.ui.notification

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan

class CustomTypefaceSpan(private val typeface: Typeface) : MetricAffectingSpan() {
    override fun updateDrawState(tp: TextPaint) {
        applyCustomTypeFace(tp)
    }

    override fun updateMeasureState(tp: TextPaint) {
        applyCustomTypeFace(tp)
    }

    private fun applyCustomTypeFace(paint: Paint) {
        paint.typeface = typeface
    }
}
