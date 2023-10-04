package me.rerere.k3d.scene.animation

class AnimationPlayer(
    private val animation: AnimationClip,
    private val loop: Boolean = true,
    private val speed: Float = 1f,
) {
    private var time = 0f
    private var playing = false

    fun play() {
        playing = true
    }

    fun pause() {
        playing = false
    }

    fun update(deltaTime: Float) {
        if (playing) {
            time += deltaTime * speed
            if (time > animation.duration) {
                if (loop) {
                    time %= animation.duration
                } else {
                    time = animation.duration
                    playing = false
                }
            }
            animation.apply(time)
        }
    }
}