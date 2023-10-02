package me.rerere.k3d.util.system

import java.util.Collections
import java.util.WeakHashMap

internal class DependencyGraph<T> {
    private val graph = WeakHashMap<T, MutableSet<T>>()

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

        graph.getOrPut(dependent) { weakSetOf() }.add(dependency)
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
    fun getDependencies(dependent: T): Set<T> {
        return graph[dependent] ?: emptySet()
    }

    /**
     * Get all dependents of the dependency
     *
     * @param dependency The object that the dependent depends on
     * @return The dependents of the dependency
     */
    fun getDependents(dependency: T): Set<T> {
        return graph.filterValues { it.contains(dependency) }.keys
    }

    fun getDependentsRecursive(dependency: T): Set<T> {
        val dependents = mutableSetOf<T>()
        val queue = ArrayDeque<T>()
        queue.add(dependency)
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            dependents.add(current)
            queue.addAll(getDependents(current))
        }
        return dependents
    }

    fun getDependenciesRecursive(dependent: T): Set<T> {
        val dependencies = mutableSetOf<T>()
        val queue = ArrayDeque<T>()
        queue.add(dependent)
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            dependencies.add(current)
            queue.addAll(getDependencies(current))
        }
        return dependencies
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

// Create a weak set
private fun <T> weakSetOf(): MutableSet<T> {
    return Collections.newSetFromMap(WeakHashMap())
}