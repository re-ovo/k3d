package me.rerere.k3d.scene

class Scene {
    private val actors = mutableListOf<Actor>()

    fun addActor(actor: Actor){
        actors.add(actor)
    }

    fun removeActor(actor: Actor){
        actors.remove(actor)
    }

//    fun render(){
//        actors.forEach {
//            it.render()
//        }
//    }
}