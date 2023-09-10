package me.rerere.k3d.util

/**
 * A dirty object is an object that can be marked as dirty
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