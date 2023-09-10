package me.rerere.k3d.util

internal interface Dirty {
    var dirty: Boolean

    fun markDirty() {
        dirty = true
    }

    fun markClean() {
        dirty = false
    }
}

internal inline fun Dirty.cleanIfDirty(block: () -> Unit) {
    if (dirty) {
        block()
        markClean()
    }
}