package me.rerere.k3d.util.system

interface Disposable {
    fun dispose()
}

fun Disposable.alsoDispose(disposable: Disposable) {
    AutoDispose.register(this, disposable)
}

object AutoDispose {
    internal val tree = ObjectTree()

    fun register(parent: Disposable, child: Disposable) {
        tree.register(parent, child)
    }

    fun dispose(obj: Disposable) {
        tree.disposeAll(obj)
    }

    fun disposeEntireTree() {
        tree.disposeEntireTree()
    }
}