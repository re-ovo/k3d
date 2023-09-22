package me.rerere.k3d.util

import kotlin.reflect.KProperty

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
@Deprecated("Stop using this function, because it's buggy")
internal inline fun Dirty.cleanIfDirty(block: () -> Unit) {
    if (dirty) {
        block()
        markClean()
    }
}

/**
 * Create a dirty object, it can be used as a delegated property
 *
 * It will automatically mark the object as dirty when the value is changed
 *
 * Example:
 * ```
 * class Test {
 *    // we don't use `var x by dirtyValue(42)` here because we want to access the dirty state
 *    // in other places
 *    private var _x = dirtyValue(42)
 *    var x by _x
 * }
 * ```
 *
 * @param value The initial value
 * @param dirtyInit The initial dirty state
 */
fun <T> dirtyValue(value: T?, dirtyInit: Boolean = false) = DirtyDelegation(value, dirtyInit)

class DirtyDelegation<T> internal constructor(
    var value: T?,
    var dirty: Boolean
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        this.value = value
        this.dirty = true
    }
}