package me.rerere.k3d.util.system

/**
 * Mark a class as disposable
 *
 * A disposable class should be disposed when it's no longer needed, the dispose() method should clean
 * up all the resources used by this class, such as OpenGL resources.
 *
 * Also, the Disposable support a tree structure, which means a Disposable can have a parent Disposable,
 * when the parent Disposable is disposed, the child Disposable will be disposed too.
 *
 * Note: Do not call dispose() manually, use [AutoDispose.dispose] instead.
 *
 * The idea comes from [Intellij SDK](https://plugins.jetbrains.com/docs/intellij/disposers.html#ending-a-disposable-lifecycle)
 *
 * @see AutoDispose
 */
interface Disposable {
    fun dispose()
}

/**
 * Register a Disposable as a child of this Disposable
 *
 * If this Disposable is disposed, the child Disposable will be disposed too.
 *
 * Equivalent to [AutoDispose.register]
 *
 * @param disposable the child Disposable
 * @receiver the parent Disposable
 */
fun Disposable.bindChildDisposable(disposable: Disposable) {
    AutoDispose.register(this, disposable)
}

/**
 * Register a dispose block as a child of this Disposable
 */
fun Disposable.externalDispose(block: () -> Unit) {
    bindChildDisposable(object : Disposable {
        override fun dispose() {
            block()
        }
    })
}

/**
 * Check if a Disposable is disposed
 */
fun Disposable.isDisposed(): Boolean {
    return AutoDispose.isDisposed(this)
}

/**
 * Dispose a Disposable
 *
 * Equivalent to [AutoDispose.dispose]
 */
fun Disposable.disposeAll() {
    AutoDispose.dispose(this)
}

/**
 * Disposable Manager
 */
object AutoDispose {
    internal val tree = ObjectTree()

    /**
     * Register a Disposable as a child of another Disposable
     *
     * If the parent Disposable is disposed, the child Disposable will be disposed too.
     */
    fun register(parent: Disposable, child: Disposable) {
        tree.register(parent, child)
    }

    /**
     * Check if a Disposable is disposed
     *
     * @param obj the Disposable to check
     * @return true if the Disposable is disposed
     */
    fun isDisposed(obj: Disposable): Boolean {
        return tree.isDisposed(obj)
    }

    /**
     * Dispose a Disposable
     *
     * It will dispose the Disposable itself and all its children(including children's children...)
     *
     * @param obj the Disposable to dispose
     */
    fun dispose(obj: Disposable) {
        tree.disposeAll(obj)
    }

    /**
     * Dispose the entire tree
     *
     * It will dispose all the Disposable registered in the tree and clear the tree container, if you
     * no longer need to use K3D, you can call this method to avoid memory leak, because the Disposable
     * Tree will hold a strong reference to the Disposable object.
     */
    fun disposeEntireTree() {
        tree.disposeEntireTree()
    }
}