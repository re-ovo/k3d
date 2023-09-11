package me.rerere.k3d.util

/**
 * A dirty object is an object that can be marked as dirty
 *
 * Some objects may correspond to some resources of OpenGL, or serve as dependencies on other
 * computed properties. Therefore, in order to update on demand, we introduce a dirty tag to
 * indicate whether the object needs to be updated.
 *
 * For example, a actor has a position and a rotation. And the position and rotation are used to
 * calculate the model matrix. If the position or rotation changes, the model matrix needs to be
 * recalculated. Therefore, the position and rotation are dirty objects, and the model matrix is
 * a computed property that depends on the position and rotation.
 */
internal interface Dirty {
    /**
     * Is this object dirty?
     *
     * Dirty objects should be recalculated
     */
    var dirty: Boolean

    /**
     * Mark this object as dirty
     */
    fun markDirty() {
        dirty = true
    }

    /**
     * Mark this object as clean
     */
    fun markClean() {
        dirty = false
    }
}

/**
 * If this object is dirty, run the block and mark this object as clean
 *
 * @param block The block to run
 */
internal inline fun Dirty.cleanIfDirty(block: () -> Unit) {
    if (dirty) {
        block()
        markClean()
    }
}