package com.dawnbellsuha.uha.views

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.dawnbellsuha.uha.R

class GradientView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    init {
        // Apply default gradient on initialization
        val defaultColors = intArrayOf(
            ContextCompat.getColor(context, R.color.gradient_start),
            ContextCompat.getColor(context, R.color.gradient_end)
        )
        setGradientColors(defaultColors)
    }

    fun setGradientColors(colors: IntArray) {
        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            colors
        )
        gradientDrawable.cornerRadius = 0f
        background = gradientDrawable
    }
}
