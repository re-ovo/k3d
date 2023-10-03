package me.rerere.k3d.util.system

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
 */
fun Dirty.markDirty() {
    DirtyQueue.markDirty(this)
}

fun Dirty.markCurrentFrameDirty() {
    DirtyQueue.markCurrentFrameDirty(this)
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

// TODO: 支持/检测循环更新? (updateDirty里面再次调用markDirty)
// TODO: 多Renderer支持, 现在只支持一个Renderer，每次渲染都会清空DirtyQueue
object DirtyQueue {
    private val dependencyGraph = DependencyGraph<Dirty>()

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
        dependencyGraph.getDependentsRecursive(dirty).forEach {
            if (nonUpdateDirty.contains(it)) return@forEach
            queue.add(it)
        }
    }

    fun markCurrentFrameDirty(dirty: Dirty) {
        if (nonUpdateDirty.contains(dirty)) return
        currentFrameDirty += dirty

        // Mark all dependents dirty
        dependencyGraph.getDependentsRecursive(dirty).forEach {
            if (nonUpdateDirty.contains(it)) return@forEach
            currentFrameDirty += it
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

            // println("dirty: ${element.javaClass.simpleName}")

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
    setter: (oldValue: T, newValue: T) -> T = { _, newValue -> newValue }
): DirtyValue<T> {
    return DirtyValue(this, initialValue, getter, setter)
}

fun <T> Dirty.dirtyValueNullable(
    initialValue: T?,
    getter: (T?) -> T? = { it },
    setter: (oldValue: T?, newValue: T?) -> T? = { _, newValue -> newValue }
): DirtyValue<T?> {
    return DirtyValue(this, initialValue, getter, setter)
}

class DirtyValue<T>(
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