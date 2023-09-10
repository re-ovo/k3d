package me.rerere.k3d.scene

class Scene : ActorGroup() {
    override fun addChild(actor: Actor) {
        super.addChild(actor)

//        if(actor is Light) {
//            if(camera != null) {
//                throw IllegalStateException("Scene can only have one camera")
//            }
//            camera = actor
//        }
    }
}