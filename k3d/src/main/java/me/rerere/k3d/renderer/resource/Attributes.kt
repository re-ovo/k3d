package me.rerere.k3d.renderer.resource

import me.rerere.k3d.util.system.Dirty
import java.nio.ByteBuffer

/**
 * This class represents a single attribute(such as position, normal, uv, etc.)
 *
 * @property itemSize The size of each item in this attribute
 * @property type The data type of this attribute
 * @property normalized Whether the data should be normalized
 * @property count The count of elements in this attribute, not to be confused with the number of
 *                 bytes or number of components. For example, if itemSize is 3 (triangle), then count will be
 *                 the number of triangles.
 * @param data The data of this attribute
 */
class Attribute(
    val itemSize: Int,
    val type: DataType,
    val normalized: Boolean,
    val count: Int,
    val data: ByteBuffer
) : Dirty {
    override var dirty: Boolean = false

    override fun toString(): String {
        return "Attribute(itemSize=$itemSize, type=$type, normalized=$normalized, count=$count, data=$data)"
    }
}

internal class VertexArray {
    private val attributes = hashMapOf<String, Attribute>()
    private var indices: Attribute? = null

    fun setAttribute(name: String, attribute: Attribute) {
        attributes[name] = attribute
    }

    fun getAttribute(name: String): Attribute? {
        return attributes[name]
    }

    fun getAttributes(): Set<Map.Entry<String, Attribute>> = attributes.entries

    fun setIndices(indices: Attribute) {
        this.indices = indices
    }

    fun getIndices(): Attribute? {
        return indices
    }
}