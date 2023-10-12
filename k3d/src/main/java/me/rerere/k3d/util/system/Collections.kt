@file:Suppress("ReplaceManualRangeWithIndicesCalls")

package me.rerere.k3d.util.system

import org.plumelib.util.WeakIdentityHashMap
import java.util.Collections
import java.util.IdentityHashMap
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

internal fun <E> identitySetOf(): MutableSet<E> = Collections.newSetFromMap(IdentityHashMap())

internal fun <K, V> identityMapOf(): MutableMap<K, V> = IdentityHashMap()

internal fun <T> concurrentQueueOf(): Queue<T> = ConcurrentLinkedQueue()

inline fun <T> List<T>.fastForeach(action: (T) -> Unit) {
    for (i in 0 until this.size) { // avoid iterator creation
        action(this[i])
    }
}

inline fun <T> List<T>.fastForEachIndexed(action: (index: Int, T) -> Unit) {
    for (i in 0 until this.size) { // avoid iterator creation
        action(i, this[i])
    }
}

inline fun <reified R, C : MutableList<in R>> List<*>.fastFilterIsInstanceTo(destination: C): C {
    for (i in 0 until this.size) { // avoid iterator creation
        val element = this[i]
        if (element is R) {
            destination.add(element)
        }
    }
    return destination
}

fun <K, V> weakIdentityHashMap(): MutableMap<K, V> = WeakIdentityHashMap()

fun <E> weakIdentityHashSet(): MutableSet<E> = Collections.newSetFromMap(WeakIdentityHashMap())