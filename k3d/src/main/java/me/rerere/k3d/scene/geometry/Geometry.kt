package me.rerere.k3d.scene.geometry

import me.rerere.k3d.renderer.resource.Attribute
import me.rerere.k3d.renderer.resource.VertexArray
import java.nio.Buffer

open class BufferGeometry {
    internal val vao = VertexArray()

    fun setAttribute(name: String, attribute: Attribute) {
        vao.setAttribute(name, attribute)
    }

    fun getAttribute(name: String): Attribute? {
        return vao.getAttribute(name)
    }

    fun getIndices(): Buffer? {
        return vao.getIndices()
    }

    fun setIndices(indices: Buffer) {
        vao.setIndices(indices)
    }
}