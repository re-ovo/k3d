package me.rerere.k3d.util.system

interface Dirty {
    fun isDirty(): Boolean

    fun updateDirty()

    fun markDirtyNew()

    fun clearDirty()
}

class DirtyQueue {
    private val dirtyList = mutableListOf<Dirty>()
    private val dirtySet = identitySetOf<Dirty>()

    fun ensureDirtyUpdated(dirty: Dirty) {
        if(dirty.isDirty()) {
            if(dirtySet.contains(dirty)) return

            dirty.updateDirty()
            addToClearQueue(dirty)
        }
    }

    inline fun whenDirty(dirty: Dirty, block: () -> Unit) {
        if(dirty.isDirty()) {
            block()
            addToClearQueue(dirty)
        }
    }

    fun addToClearQueue(dirty: Dirty) {
        if(dirtySet.contains(dirty)) return

        dirtyList.add(dirty)
        dirtySet.add(dirty)
    }

    fun clean() {
        dirtyList.fastForeach {
            it.clearDirty()
        }
        dirtyList.clear()
        dirtySet.clear()
    }
}

fun <T> Dirty.dirtyValue(
    initialValue: T,
    getter: (T) -> T = { it },
    setter: (old: T, new: T) -> T = { _, new -> new }
): DirtyValueDelegate<T> {
    return DirtyValueDelegate(this, initialValue, getter, setter)
}

fun Dirty.dirtyFloatValue(
    initialValue: Float,
    getter: FloatUnaryMapper = FloatUnaryMapper { it },
    setter: FloatBinaryMapper = FloatBinaryMapper { _, new -> new }
): DirtyFloatValueDelegate {
    return DirtyFloatValueDelegate(this, initialValue, getter, setter)
}

class DirtyValueDelegate<T>(
    private val dirty: Dirty,
    private var value: T,
    private val getter: (T) -> T = { it },
    private val setter: (old: T, new: T) -> T = { _, new -> new }
) {
    operator fun getValue(thisRef: Any?, property: Any?): T {
        return getter(value)
    }

    operator fun setValue(thisRef: Any?, property: Any?, value: T) {
        this.value = setter(this.value, value)
        dirty.markDirtyNew()
    }
}

class DirtyFloatValueDelegate(
    private val dirty: Dirty,
    private var value: Float,
    private val getter: FloatUnaryMapper = FloatUnaryMapper { it },
    private val setter: FloatBinaryMapper = FloatBinaryMapper { _, new -> new }
) {
    operator fun getValue(thisRef: Any?, property: Any?): Float {
        return getter.map(value)
    }

    operator fun setValue(thisRef: Any?, property: Any?, value: Float) {
        this.value = setter.map(this.value, value)
        dirty.markDirtyNew()
    }
}