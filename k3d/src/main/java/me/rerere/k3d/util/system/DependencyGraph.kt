package me.rerere.k3d.util.system

import com.google.common.collect.MapMaker
import java.nio.channels.FileChannel.MapMode
import java.util.Stack
import java.util.WeakHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Object dependency graph
 *
 * Note: This class is not thread-safe
 *
 * @param T The type of the object
 */
internal class DependencyGraph<T : Any> {
    private val dependents2dependency = MapMaker().weakKeys().makeMap<T, MutableSet<T>>()
    private val dependency2dependents = MapMaker().weakKeys().makeMap<T, MutableSet<T>>()

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
        dependents2dependency.getOrPut(dependent) { weakIdentityHashSetOf() }.add(dependency)
        dependency2dependents.getOrPut(dependency) { weakIdentityHashSetOf() }.add(dependent)
    }

    /**
     * Remove a dependency
     *
     * @param dependent The object that depends on the dependency
     * @param dependency The object that the dependent depends on
     */
    fun removeDependency(dependent: T, dependency: T) {
        dependents2dependency[dependent]?.remove(dependency)
        dependency2dependents[dependency]?.remove(dependent)
    }

    /**
     * Get all dependencies of the dependent
     *
     * @param dependent The object that depends on the dependency
     * @return The dependencies of the dependent
     */
    private fun getDependencies(dependent: T): Set<T> {
        return dependents2dependency[dependent] ?: emptySet()
    }

    /**
     * Get all dependents of the dependency
     *
     * @param dependency The object that the dependent depends on
     * @return The dependents of the dependency
     */
    fun getDependents(dependency: T): Set<T> {
        return dependency2dependents[dependency] ?: emptySet()
    }

    private val _dependentsRecursiveCache = arrayListOf<T>() // avoid allocation

    /**
     * Get all dependents of the dependency recursively
     *
     * Note that this method is not thread-safe, because it reuse a list to avoid object allocation
     *
     * @param dependency The object that the dependent depends on
     * @return The dependents of the dependency recursively
     */
    fun getDependentsRecursive(dependency: T): List<T> {
        _dependentsRecursiveCache.clear()
        fun dfs(d: T) {
            if(d !== dependency) _dependentsRecursiveCache += d
            getDependents(d).forEach {
                if (it !in _dependentsRecursiveCache) {
                    dfs(it)
                }
            }
        }
        dfs(dependency)
        return _dependentsRecursiveCache
    }

    /**
     * Clear the graph
     */
    fun clear() {
        dependents2dependency.clear()
        dependency2dependents.clear()
    }

    // Check if the dependency is cyclic
    private fun checkCyclicDependency(dependent: T, dependency: T) {
        if (dependency in getDependentsRecursive(dependent)) {
            throw CyclicDependencyException(dependent, dependency)
        }
    }
}

/**
 * Exception thrown when a cyclic dependency is detected
 */
class CyclicDependencyException(dependent: Any, dependency: Any) : RuntimeException("Cyclic dependency detected: $dependent -> $dependency")

