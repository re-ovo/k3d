package me.rerere.k3d.renderer

class Clock {
    private var lastTime = System.nanoTime()
    private var deltaTime = 0.0f

    fun tick() {
        val now = System.nanoTime()
        deltaTime = (now - lastTime) / 1000000000.0f
        lastTime = now
    }

    fun getDelta(): Float = deltaTime
}