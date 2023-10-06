package me.rerere.k3d.scene.animation

import me.rerere.k3d.scene.actor.Actor

sealed interface AnimationTarget {
    val node: Actor

    fun update(
        a: Pair<Float, FloatArray>,
        b: Pair<Float, FloatArray>,
        time: Float,
        interpolation: Interpolation,
    )

    class Position(override val node: Actor) : AnimationTarget {
        override fun update(
            a: Pair<Float, FloatArray>,
            b: Pair<Float, FloatArray>,
            time: Float,
            interpolation: Interpolation
        ) {
            val x = interpolation.interpolate(time, a.first, b.first, a.second[0], b.second[0])
            val y = interpolation.interpolate(time, a.first, b.first, a.second[1], b.second[1])
            val z = interpolation.interpolate(time, a.first, b.first, a.second[2], b.second[2])

            node.position.set(x, y, z)
        }
    }

    class Rotation(override val node: Actor) : AnimationTarget {
        override fun update(
            a: Pair<Float, FloatArray>,
            b: Pair<Float, FloatArray>,
            time: Float,
            interpolation: Interpolation
        ) {
            val x = interpolation.interpolate(time, a.first, b.first, a.second[0], b.second[0])
            val y = interpolation.interpolate(time, a.first, b.first, a.second[1], b.second[1])
            val z = interpolation.interpolate(time, a.first, b.first, a.second[2], b.second[2])
            val w = interpolation.interpolate(time, a.first, b.first, a.second[3], b.second[3])

            node.rotation.set(x, y, z, w)
        }
    }

    class Scale(override val node: Actor) : AnimationTarget {
        override fun update(
            a: Pair<Float, FloatArray>,
            b: Pair<Float, FloatArray>,
            time: Float,
            interpolation: Interpolation
        ) {
            val x = interpolation.interpolate(time, a.first, b.first, a.second[0], b.second[0])
            val y = interpolation.interpolate(time, a.first, b.first, a.second[1], b.second[1])
            val z = interpolation.interpolate(time, a.first, b.first, a.second[2], b.second[2])

            node.scale.set(x, y, z)
        }
    }

    class Weight(override val node: Actor) : AnimationTarget {
        override fun update(
            a: Pair<Float, FloatArray>,
            b: Pair<Float, FloatArray>,
            time: Float,
            interpolation: Interpolation
        ) {
            // TODO: Support weight animation
        }
    }
}