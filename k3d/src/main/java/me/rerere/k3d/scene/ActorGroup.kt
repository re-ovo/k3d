package me.rerere.k3d.scene

/**
 * ActorGroup is a group of actors, it can be used to group actors together.
 * ActorGroup is also an actor, so it can be added to another ActorGroup.
 * So the entire scene is a tree structure.
 *
 * @see Actor
 * @see Scene
 */
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