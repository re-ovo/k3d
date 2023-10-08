package me.rerere.k3d.scene.geometry

import me.rerere.k3d.renderer.resource.Attribute
import me.rerere.k3d.renderer.resource.DataType
import me.rerere.k3d.renderer.resource.VertexArray
import me.rerere.k3d.renderer.shader.BuiltInAttributeName
import me.rerere.k3d.util.math.Vec3
import me.rerere.k3d.util.toByteBuffer
import java.nio.Buffer
import java.nio.ByteBuffer

open class BufferGeometry {
    internal val vao = VertexArray()

    val drawCount: Int
        get() {
            return getIndices()?.count ?: getAttribute(BuiltInAttributeName.POSITION)?.count
            ?: error("Invalid geometry: no indices or position attribute found")
        }

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

    fun getIndices(): Attribute? {
        return vao.getIndices()
    }

    fun setIndices(indices: Attribute) {
        vao.setIndices(indices)
    }

    fun setIndices(indices: IntArray, markDirty: Boolean = false) {
        setIndices(
            Attribute(
                itemSize = 1,
                normalized = false,
                count = indices.size,
                data = indices.toByteBuffer(),
                type = DataType.UNSIGNED_INT
            ).apply {
                if (markDirty) markDirtyNew()
            }
        )
    }

    companion object {
        @JvmStatic
        fun fromPoints(points: List<Vec3>): BufferGeometry {
            val data = points
                .flatMap { sequenceOf(it.x, it.y, it.z) }
                .toFloatArray()
                .toByteBuffer()

            val geometry = BufferGeometry()
            geometry.setAttribute(
                BuiltInAttributeName.POSITION,
                Attribute(
                    itemSize = 3,
                    normalized = false,
                    count = points.size,
                    data = data,
                    type = DataType.FLOAT
                )
            )
            return geometry
        }
    }
}