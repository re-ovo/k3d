@file:Suppress("ReplaceManualRangeWithIndicesCalls")

package me.rerere.k3d.util.system

import com.google.common.collect.MapMaker
import com.google.common.collect.Queues
import java.util.Collections
import java.util.IdentityHashMap
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

internal fun <E> identitySetOf(): MutableSet<E> = Collections.newSetFromMap(IdentityHashMap())

internal fun <K, V> weakIdentityHashMapOf(): MutableMap<K, V> = MapMaker().weakKeys().makeMap()

internal fun <T> weakIdentityHashSetOf(): MutableSet<T> = Collections.newSetFromMap(weakIdentityHashMapOf())

internal fun <T> concurrentQueueOf(): Queue<T> = ConcurrentLinkedQueue()

inline fun <T> List<T>.fastForeach(action: (T) -> Unit) {
    for (i in 0 until this.size) { // avoid iterator creation
        action(this[i])
    }
}