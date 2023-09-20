package me.rerere.k3d.scene.actor

import me.rerere.k3d.scene.light.Light

class Scene : ActorGroup() {
    internal val lights = mutableListOf<Light>()

    override fun addChild(actor: Actor) {
        super.addChild(actor)
        if(actor is Light) {
            lights.add(actor)
        }
    }

    override fun removeChild(actor: Actor) {
        super.removeChild(actor)

        if(actor is Light) {
            lights.remove(actor)
        }
    }

    fun removeFromScene(actor: Actor) {
        traverse {
            if(it is ActorGroup) {
                it.removeChild(actor)
            }
        }
    }
}