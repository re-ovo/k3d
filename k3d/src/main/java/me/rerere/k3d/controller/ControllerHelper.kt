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

sealed class ControllerEvent {
    data class Rotate(val deltaX: Float, val deltaY: Float) : ControllerEvent()
    data class Zoom(val delta: Float) : ControllerEvent()
    data class Pan(val deltaX: Float, val deltaY: Float) : ControllerEvent()
}

internal class MotionHandler(private val handler: (ControllerEvent) -> Unit) {
    private var lastX = 0f
    private var lastY = 0f
    private var lastDistance = -1f

    fun handle(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
            }

            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount == 1) {
                    // Single touch: Rotate
                    val deltaX = event.x - lastX
                    val deltaY = event.y - lastY
                    handler(ControllerEvent.Rotate(deltaX, deltaY))
                } else if (event.pointerCount == 2) {
                    // Two fingers touch: Zoom
                    val distance = event.getPointerDistance(0, 1)
                    if (lastDistance != -1f) {
                        val delta = distance - lastDistance
                        handler(ControllerEvent.Zoom(delta))
                    }
                    lastDistance = distance
                } else if (event.pointerCount == 3) {
                    // Three fingers touch: Pan
                    val x0 = event.getX(0)
                    val y0 = event.getY(0)
                    val x1 = event.getX(1)
                    val y1 = event.getY(1)
                    val x2 = event.getX(2)
                    val y2 = event.getY(2)

                    val deltaX = x0.coerceAtLeast(x1).coerceAtLeast(x2) - x0.coerceAtMost(x1)
                        .coerceAtMost(x2)
                    val deltaY = y0.coerceAtLeast(y1).coerceAtLeast(y2) - y0.coerceAtMost(y1)
                        .coerceAtMost(y2)

                    handler(ControllerEvent.Pan(deltaX, deltaY))
                }

                lastX = event.x
                lastY = event.y
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                lastDistance = -1f
            }
        }
    }
}