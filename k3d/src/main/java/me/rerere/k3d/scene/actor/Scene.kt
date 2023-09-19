package me.rerere.k3d.scene.actor

import me.rerere.k3d.scene.light.LightActor

class Scene : ActorGroup() {
    private val lights = mutableListOf<LightActor>()

    override fun addChild(actor: Actor) {
        super.addChild(actor)
        if(actor is LightActor) {
            lights.add(actor)
        }
    }

    override fun removeChild(actor: Actor) {
        super.removeChild(actor)

        if(actor is LightActor) {
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