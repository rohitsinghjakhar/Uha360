package com.dawnbellsuha.uha.adapters

import com.dawnbellsuha.uha.R

data class OnboardingItem(
    val imageResId: Int,
    val title: String,
    val description: String,
    val backgroundColorResId: Int = R.color.light_blue
)