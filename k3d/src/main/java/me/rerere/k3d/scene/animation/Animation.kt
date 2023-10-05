package me.rerere.k3d.scene.animation

import me.rerere.k3d.util.system.fastForeach

data class AnimationClip(
    val name: String,
    val duration: Float,
    val tracks: List<KeyframeTrack>,
)

data class KeyframeTrack(
    val target: AnimationTarget,
    val keyframes: List<Pair<Float, FloatArray>>,
    val interpolation: Interpolation,
)

fun AnimationClip.apply(time: Float) {
    tracks.fastForeach { track ->
        val minTime = track.keyframes.first().first
        val maxTime = track.keyframes.last().first

        if(time < minTime) {
            track.target.update(
                a = track.keyframes.first(),
                b = track.keyframes.first(),
                time = time,
                interpolation = track.interpolation,
            )
            return@fastForeach
        }

        if(time > maxTime) {
            track.target.update(
                a = track.keyframes.last(),
                b = track.keyframes.last(),
                time = time,
                interpolation = track.interpolation,
            )
            return@fastForeach
        }

        var a: Pair<Float, FloatArray>? = null
        var b: Pair<Float, FloatArray>? = null
        for (i in 0 until track.keyframes.size) {
            val keyframe = track.keyframes[i]
            if (keyframe.first <= time) {
                a = keyframe
            }
            if (keyframe.first >= time) {
                b = keyframe
                break
            }
        }

        if (a != null && b != null) {
            track.target.update(
                a = a,
                b = b,
                time = time,
                interpolation = track.interpolation,
            )
        } else error("KeyframeTrack error")
    }
}