package me.rerere.k3d.scene.actor

import me.rerere.k3d.util.system.fastForeach

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

    /**
     * Add a child actor to this group
     *
     * @param actor the actor to be added
     */
    open fun addChild(actor: Actor) {
        actor.parent = this
        children.add(actor)
    }

    /**
     * Remove a child actor from this group
     *
     * @param actor the actor to be removed
     */
    open fun removeChild(actor: Actor) {
        actor.parent = null
        children.remove(actor)
    }

    /**
     * Get all children of this group
     */
    fun getChildren(): List<Actor> {
        return children
    }
}

fun ActorGroup.traverse(action: (Actor) -> Unit) {
    action(this)
    val children = this.getChildren()
    children.fastForeach { child: Actor ->
        action(child)
        if (child is ActorGroup) {
            child.traverse(action)
        }
    }
}