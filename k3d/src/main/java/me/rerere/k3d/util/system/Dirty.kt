package me.rerere.k3d.util.system

interface Dirty {
    var dirty: Boolean

    fun markDirty() {
        dirty = true
    }

    fun markClean() {
        dirty = false
    }
}

object DirtyQueue {

}

@Deprecated("Stop using this function, because it's buggy")
internal inline fun Dirty.cleanIfDirty(block: () -> Unit) {
    if (dirty) {
        block()
        markClean()
    }
}