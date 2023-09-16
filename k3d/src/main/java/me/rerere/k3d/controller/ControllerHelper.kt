package me.rerere.k3d.controller

import android.view.MotionEvent
import kotlin.math.sqrt

internal fun MotionEvent.getPointerDistance(index1: Int, index2: Int): Float {
    val x1 = getX(index1)
    val y1 = getY(index1)
    val x2 = getX(index2)
    val y2 = getY(index2)
    return sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2).toDouble()).toFloat()
}