package me.rerere.k3d.util.system

import com.google.common.collect.MapMaker
import java.util.Collections
import java.util.IdentityHashMap
import java.util.Queue
import java.util.WeakHashMap
import java.util.concurrent.ConcurrentLinkedQueue

internal fun <K, V> weakIdentityHashMapOf(): MutableMap<K, V> = MapMaker().weakKeys().makeMap()

internal fun <T> weakIdentityHashSetOf(): MutableSet<T> = Collections.newSetFromMap(weakIdentityHashMapOf())

internal fun <T> concurrentQueueOf(): Queue<T> = ConcurrentLinkedQueue()