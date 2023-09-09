package me.rerere.k3d.scene

open class ActorGroup : Actor() {
    private val children = mutableListOf<Actor>()

    fun addChild(actor: Actor) {
        children.add(actor)
    }

    fun removeChild(actor: Actor) {
        children.remove(actor)
    }

    fun getChildren(): List<Actor> {
        return children
    }

    override fun updateWorldMatrix() {
        super.updateWorldMatrix()
        children.forEach {
            it.updateWorldMatrix()
        }
    }
}