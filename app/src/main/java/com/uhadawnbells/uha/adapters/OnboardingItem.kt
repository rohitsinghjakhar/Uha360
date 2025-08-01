package com.uhadawnbells.uha.adapters

import com.uhadawnbells.uha.R

data class OnboardingItem(
    val imageResId: Int,
    val title: String,
    val description: String,
    val backgroundColorResId: Int = R.color.light_blue
)