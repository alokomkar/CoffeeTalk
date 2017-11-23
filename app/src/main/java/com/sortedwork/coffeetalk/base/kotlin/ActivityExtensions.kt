package com.sortedwork.coffeetalk.base.kotlin

/**
 * Created by Alok on 31/10/17.
 */
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.support.v4.content.ContextCompat
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowManager


@SuppressLint("InlinedApi")
fun Activity.setStatusBarColor(colorResId: Int) {
    supportsLollipop {
        with(window) {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            statusBarColor = ContextCompat.getColor(context, colorResId)
        }
    }

}

fun Activity.getDPFromPixel(pixels: Float): Float {
    val displayMetrics = DisplayMetrics()
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixels, displayMetrics)
}

fun Application.getString( stringId : Int ) : String {
    return applicationContext.getString(stringId)
}