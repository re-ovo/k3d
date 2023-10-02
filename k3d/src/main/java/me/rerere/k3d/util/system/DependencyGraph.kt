package me.rerere.k3d.util.system

import com.google.common.collect.MapMaker
import java.nio.channels.FileChannel.MapMode
import java.util.Stack
import java.util.WeakHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

internal class DependencyGraph<T> {
    private val graph = MapMaker().weakKeys().makeMap<T, MutableSet<T>>()
    private val lock = ReentrantReadWriteLock()

    /**
     * Add a dependency
     *
     * The "dependent" depends on the "dependency"
     *
     * @param dependent The object that depends on the dependency
     * @param dependency The object that the dependent depends on
     */
    fun addDependency(dependent: T, dependency: T) {
        checkCyclicDependency(dependent, dependency)

        lock.write {
            graph.getOrPut(dependent) { weakIdentityHashSetOf() }.add(dependency)
        }
    }

    /**
     * Remove a dependency
     *
     * @param dependent The object that depends on the dependency
     * @param dependency The object that the dependent depends on
     */
    fun removeDependency(dependent: T, dependency: T) {
        graph[dependent]?.remove(dependency)
    }

    /**
     * Get all dependencies of the dependent
     *
     * @param dependent The object that depends on the dependency
     * @return The dependencies of the dependent
     */
    private fun getDependencies(dependent: T): Set<T> {
        return graph[dependent] ?: emptySet()
    }

    /**
     * Get all dependents of the dependency
     *
     * @param dependency The object that the dependent depends on
     * @return The dependents of the dependency
     */
    fun getDependents(dependency: T): Set<T> {
        lock.read {
            return graph.filterValues { it.contains(dependency) }.keys
        }
    }

    fun getDependentsRecursive(dependency: T): List<T> {
        val result = arrayListOf<T>()

        val queue = Stack<T>()
        queue.push(dependency)

        while (queue.isNotEmpty()) {
            val current = queue.pop()
            if(current !== dependency) {
                result.add(current)
            }
            queue.addAll(getDependents(current))
        }

        return result
    }


    /**
     * Clear the graph
     */
    fun clear() {
        graph.clear()
    }

    // Check if the dependency is cyclic
    private fun checkCyclicDependency(dependent: T, dependency: T) {
        if (dependent == dependency) {
            throw IllegalArgumentException("Cyclic dependency detected: $dependent -> $dependency")
        }
        if (dependency in getDependencies(dependent)) {
            throw IllegalArgumentException("Cyclic dependency detected: $dependent -> $dependency")
        }
    }
}

