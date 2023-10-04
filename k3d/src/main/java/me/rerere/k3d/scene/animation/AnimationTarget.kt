package me.rerere.k3d.scene.animation

import me.rerere.k3d.scene.actor.Actor

sealed interface AnimationTarget {
    val node: Actor

    fun update(data: FloatArray)

    class Position(override val node: Actor) : AnimationTarget {
        override fun update(data: FloatArray) {
            node.position.set(data[0], data[1], data[2])
        }
    }

    class Rotation(override val node: Actor) : AnimationTarget {
        override fun update(data: FloatArray) {
            node.rotation.set(data[0], data[1], data[2], data[3])
        }
    }

    class Scale(override val node: Actor) : AnimationTarget {
        override fun update(data: FloatArray) {
            node.scale.set(data[0], data[1], data[2])
        }
    }
}