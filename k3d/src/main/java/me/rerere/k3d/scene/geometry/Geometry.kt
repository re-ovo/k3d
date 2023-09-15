package me.rerere.k3d.scene.geometry

import me.rerere.k3d.renderer.resource.Attribute
import me.rerere.k3d.renderer.resource.VertexArray

open class BufferGeometry {
    internal val vao = VertexArray()

    fun setIndices(indices: IntArray) {
        vao.setIndices(indices)
    }

    fun setAttribute(attribute: Attribute) {
        vao.setAttribute(attribute)
    }

    fun getIndices(): IntArray? {
        return vao.getIndices()
    }

    fun getAttribute(name: String): Attribute? {
        return vao.getAttribute(name)
    }
}