package me.rerere.k3d.scene

class Scene : ActorGroup() {
    // private val lights = mutableListOf<Light>()

    override fun addChild(actor: Actor) {
        super.addChild(actor)
//        if(actor is Light) {
//            if(camera != null) {
//                throw IllegalStateException("Scene can only have one camera")
//            }
//            camera = actor
//        }
    }

    override fun removeChild(actor: Actor) {
        super.removeChild(actor)

        // TODO: Remove light etc
    }

    fun removeFromScene(actor: Actor) {
        traverse {
            if(it is ActorGroup) {
                it.removeChild(actor)
            }
        }
    }
}