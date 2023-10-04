package me.rerere.k3d.scene.animation

data class AnimationClip(
    val name: String,
    val duration: Float,
    val tracks: List<KeyframeTrack>,
)

data class KeyframeTrack(
    val target: AnimationTarget,
    val keyframes: List<Pair<Float, FloatArray>>,
    val interpolation: Interpolation,
) {
    fun getValue(time: Float): FloatArray {
        val index = keyframes.binarySearch { it.first.compareTo(time) }
        if (index < 0) {
            when (val nextIndex = -index - 1) {
                0 -> {
                    return keyframes[0].second
                }
                keyframes.size -> {
                    return keyframes.last().second
                }
                else -> {
                    val prev = keyframes[nextIndex - 1]
                    val next = keyframes[nextIndex]
                    return interpolation.interpolate(
                        t = time,
                        t0 = prev.first,
                        t1 = next.first,
                        v0 = prev.second,
                        v1 = next.second,
                    )
                }
            }
        } else {
            return keyframes[index].second
        }
    }
}

fun AnimationClip.apply(time: Float) {
    tracks.forEach { track ->
        val value = track.getValue(time)
        track.target.update(value)
    }
}