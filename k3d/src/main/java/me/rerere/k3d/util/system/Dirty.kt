package me.rerere.k3d.util.system

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KProperty

interface Dirty

interface DirtyUpdate : Dirty {
    fun updateDirty()
}

/**
 * Make this dirty object depends on another dirty object
 *
 * When the [dependency] object is marked dirty, this object will be marked dirty too
 *
 * @param dependency the dependency object
 */
fun Dirty.dependsOn(dependency: Dirty) {
    DirtyQueue.dependsOn(this, dependency)
}

/**
 * Remove the dependency between this object and another dirty object
 *
 * @param dependency the dependency object
 */
fun Dirty.dependsRemove(dependency: Dirty) {
    DirtyQueue.dependsRemove(this, dependency)
}

/**
 * Mark this object dirty
 *
 * It will be added to the dirty queue and will be updated in the next frame. The dependents of this
 * object will be marked dirty too.
 */
fun Dirty.markDirty() {
    DirtyQueue.markDirty(this)
}

/**
 * Update this [Dirty] object without mark it dirty
 *
 * @see [Dirty.markDirty]
 */
inline fun <T : Dirty> T.withoutMarkDirty(block: T.() -> Unit) {
    try {
        DirtyQueue.setShouldUpdate(this, false)
        block()
    } finally {
        DirtyQueue.setShouldUpdate(this, true)
    }
}

val Dirty.currentFrameDirty: Boolean
    get() = DirtyQueue.isCurrentFrameDirty(this)

object DirtyQueue {
    private val dependencyGraph = DependencyGraph<Dirty>()
    private val lock = ReentrantLock()

    private val queue = concurrentQueueOf<Dirty>()
    private val currentFrameDirty = weakIdentityHashSetOf<Dirty>()
    private val nonUpdateDirty = weakIdentityHashSetOf<Dirty>()

    fun isCurrentFrameDirty(dirty: Dirty): Boolean {
        return currentFrameDirty.contains(dirty)
    }

    fun markDirty(dirty: Dirty) {
        if (nonUpdateDirty.contains(dirty)) return
        queue.add(dirty)

        // Mark all dependents dirty
        lock.withLock {
            dependencyGraph.getDependentsRecursive(dirty).fastForeach {
                if (nonUpdateDirty.contains(it)) return@fastForeach
                queue.add(it)
            }
        }
    }

    fun dependsOn(dependent: Dirty, dependency: Dirty) {
        dependencyGraph.addDependency(dependent, dependency)
    }

    fun dependsRemove(dependent: Dirty, dependency: Dirty) {
        dependencyGraph.removeDependency(dependent, dependency)
    }

    fun setShouldUpdate(dirty: Dirty, shouldUpdate: Boolean) {
        if (shouldUpdate) {
            nonUpdateDirty.remove(dirty)
        } else {
            nonUpdateDirty.add(dirty)
        }
    }

    /**
     * Update all dirty objects
     *
     * This method should be called before rendering
     */
    fun frameStart() {
        var element = queue.poll()
        while (element != null) {
            this.currentFrameDirty += element

            if (element is DirtyUpdate) {
                element.withoutMarkDirty {// Anti-recursive update
                    updateDirty()
                }
            }

            element = queue.poll()
        }
    }

    fun frameEnd() {
        currentFrameDirty.clear()
    }
}

/**
 * Create a dirty property delegate
 *
 * It will automatically mark the owner object dirty when the property is changed
 *
 * Note this is a generic function, so it will box the primitive type, use dirtyXXXValue instead
 *
 * @param initialValue the initial value of the property
 * @param getter the getter of the property
 * @param setter the setter of the property
 * @receiver the owner object
 */
fun <T> Dirty.dirtyValue(
    initialValue: T,
    getter: (T) -> T = { it },
    setter: (oldValue: T, newValue: T) -> T = { _, newValue -> newValue }
): DirtyValue<T> {
    require(initialValue !is Float) {
        "Use `dirtyFloatValue()` instead to avoid boxing"
    }

    return DirtyValue(this, initialValue, getter, setter)
}

/**
 * Create a nullable dirty property delegate
 *
 * It will automatically mark the owner object dirty when the property is changed
 *
 * @param initialValue the initial value of the property
 * @param getter the getter of the property
 * @param setter the setter of the property
 * @receiver the owner object
 */
fun <T> Dirty.dirtyValueNullable(
    initialValue: T?,
    getter: (T?) -> T? = { it },
    setter: (oldValue: T?, newValue: T?) -> T? = { _, newValue -> newValue }
): DirtyValue<T?> {
    return DirtyValue(this, initialValue, getter, setter)
}

/**
 * Create a dirty [Float] property delegate
 *
 * It will automatically mark the owner object dirty when the property is changed
 *
 * @param initialValue the initial value of the property
 * @param getter the getter of the property
 * @param setter the setter of the property
 * @receiver the owner object
 */
fun Dirty.dirtyFloatValue(
    initialValue: Float,
    getter: FloatUnaryMapper = FloatUnaryMapper { it },
    setter: FloatBinaryMapper = FloatBinaryMapper { _, newValue -> newValue }
): DirtyFloatValue {
    return DirtyFloatValue(this, initialValue, getter, setter)
}

class DirtyValue<T> internal constructor(
    private val dirty: Dirty,
    initialValue: T,
    private val getter: (T) -> T,
    private val setter: (oldValue: T, newValue: T) -> T
) {
    private var _value = initialValue

    var value: T
        get() = getter(_value)
        set(value) {
            _value = setter(_value, value)
            dirty.markDirty()
        }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}

class DirtyFloatValue(
    private val dirty: Dirty,
    initialValue: Float,
    private val getter: FloatUnaryMapper,
    private val setter: FloatBinaryMapper,
) {
    private var _value = initialValue

    var value: Float
        get() = getter.map(_value)

        set(value) {
            _value = setter.map(_value, value)
            dirty.markDirty()
        }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Float {
        return value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
        this.value = value
    }
}