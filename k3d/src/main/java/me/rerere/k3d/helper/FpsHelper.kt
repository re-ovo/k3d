package me.rerere.k3d.helper

import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@OptIn(ExperimentalTime::class)
class FpsHelper(
    private val collectDuration: Duration
) {
    private var lastTime = TimeSource.Monotonic.markNow()
    private var fps = 0
    private var frameCount = 0

    fun render() {
        frameCount++
        val now = TimeSource.Monotonic.markNow()
        if (now - lastTime >= collectDuration) {
            fps = (frameCount / collectDuration.toDouble(DurationUnit.SECONDS)).roundToInt()
            frameCount = 0
            lastTime = now
            println("FPS: $fps")
        }
    }
}