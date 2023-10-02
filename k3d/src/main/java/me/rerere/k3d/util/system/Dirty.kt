package me.rerere.k3d.util.system

import kotlin.reflect.KProperty

interface Dirty

interface DirtyUpdate {
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
 * Mark this object dirty
 */
fun Dirty.markDirty() {
    DirtyQueue.markDirty(this)
}

val Dirty.currentFrameDirty: Boolean
    get() = DirtyQueue.isCurrentFrameDirty(this)

// TODO: Actor加载后没有更新
// TODO: 支持/检测循环更新? (updateDirty里面再次调用markDirty)
object DirtyQueue {
    private val queue = concurrentQueueOf<Dirty>()
    private val dependencyGraph = DependencyGraph<Dirty>()
    private val currentFrameDirty = mutableSetOf<Dirty>()

    fun isCurrentFrameDirty(dirty: Dirty): Boolean {
        return currentFrameDirty.contains(dirty)
    }

    /**
     * @see [Dirty.dependsOn]
     */
    fun dependsOn(dependent: Dirty, dependency: Dirty) {
        dependencyGraph.addDependency(dependent, dependency)
    }

    /**
     * @see [Dirty.markDirty]
     */
    fun markDirty(dirty: Dirty) {
        queue.add(dirty)

        // Mark all dependents dirty
        dependencyGraph.getDependentsRecursive(dirty).forEach {
            queue.add(it)
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

            if(element is DirtyUpdate) {
                element.updateDirty()
            }

            element = queue.poll()
        }
    }

    fun frameEnd() {
        currentFrameDirty.clear()
    }
}

fun <T> Dirty.dirtyValue(
    initialValue: T,
    getter: (T) -> T = { it },
    setter: (T) -> T = { it }
): DirtyValue<T> {
    return DirtyValue(this, initialValue, getter, setter)
}

class DirtyValue<T>(
    private val dirty: Dirty,
    initialValue: T,
    private val getter: (T) -> T,
    private val setter: (T) -> T
) {
    private var _value = initialValue

    var value: T
        get() = getter(_value)
        set(value) {
            _value = setter(value)
            dirty.markDirty()
        }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}