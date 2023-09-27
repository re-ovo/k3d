package me.rerere.k3d.renderer

import kotlin.time.TimeSource

class Clock {
    private val timeSource = TimeSource.Monotonic
    private var lastTime = timeSource.markNow()
    private var deltaTime = 0.0f

    fun tick() {
        val now = timeSource.markNow()
        deltaTime = (now - lastTime).inWholeMilliseconds / 1000.0f
        lastTime = now
    }

    fun getDelta(): Float = deltaTime
}