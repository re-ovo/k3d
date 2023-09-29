package me.rerere.k3d.scene.geometry

import me.rerere.k3d.renderer.resource.Attribute
import me.rerere.k3d.renderer.resource.DataType
import me.rerere.k3d.renderer.resource.VertexArray
import me.rerere.k3d.renderer.shader.BuiltInAttributeName
import java.nio.Buffer
import java.nio.ByteBuffer

open class BufferGeometry {
    internal val vao = VertexArray()

    fun setAttribute(name: String, attribute: Attribute) {
        vao.setAttribute(name, attribute)
    }

    fun setAttribute(attr: BuiltInAttributeName, attribute: Attribute) {
        vao.setAttribute(attr.attributeName, attribute)
    }

    fun getAttribute(name: String): Attribute? {
        return vao.getAttribute(name)
    }

    fun getAttribute(attr: BuiltInAttributeName): Attribute? {
        return vao.getAttribute(attr.attributeName)
    }

    fun getIndices(): Buffer? {
        return vao.getIndices()
    }

    fun setIndices(indices: ByteBuffer) {
        vao.setIndices(indices)
    }

    fun setIndiceType(type: DataType) {
        vao.setIndiceType(type)
    }
}