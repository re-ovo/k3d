package me.rerere.k3d.renderer

/**
 * A virtual context is an abstraction of the real context
 *
 * It is used to provide a unified interface for different
 */
interface VirtualContext {
    fun clear(mask: Int)

    fun clearColor(r: Float, g: Float, b: Float, a: Float)

    fun enable(cap: Int)

    fun enableVertexAttribArray(index: Int)
}