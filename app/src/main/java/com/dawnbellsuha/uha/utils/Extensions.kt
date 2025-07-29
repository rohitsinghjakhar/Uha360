package com.dawnbellsuha.uha.utils


import android.content.Context
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.Toast

fun View.animateFadeIn(duration: Long = 300) {
    val animation = AlphaAnimation(0f, 1f)
    animation.duration = duration
    this.startAnimation(animation)
    this.visibility = View.VISIBLE
}


fun View.animateSlideInRight(duration: Long = 300) {
    this.alpha = 0f
    this.translationX = 100f
    this.animate()
        .translationX(0f)
        .alpha(1f)
        .setDuration(duration)
        .start()
}
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}
fun Context.dpToPx(dp: Int): Int {
    return (dp * resources.displayMetrics.density).toInt()
}

