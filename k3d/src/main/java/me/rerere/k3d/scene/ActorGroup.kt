package me.rerere.k3d.scene

open class ActorGroup : Actor() {
    private val children = mutableListOf<Actor>()

    open fun addChild(actor: Actor) {
        children.add(actor)
    }

    open fun removeChild(actor: Actor) {
        children.remove(actor)
    }

    fun getChildren(): List<Actor> {
        return children
    }
}