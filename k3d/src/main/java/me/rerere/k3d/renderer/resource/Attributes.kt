package me.rerere.k3d.renderer.resource

import me.rerere.k3d.util.Dirty
import java.nio.Buffer

/**
 * This class represents a single attribute(such as position, normal, uv, etc.)
 *
 * @param itemSize The size of each item in this attribute
 * @param type The data type of this attribute
 * @param normalized Whether the data should be normalized
 * @param stride The stride of this attribute
 * @param offset The offset of this attribute
 * @param data The data of this attribute
 */
class Attribute(
    val itemSize: Int,
    val type: DataType,
    val normalized: Boolean,
    val stride: Int = 0,
    val offset: Int = 0,
    val data: Buffer
): Dirty {
    override var dirty: Boolean = false

    override fun toString(): String {
        return "Attribute(itemSize=$itemSize, type=$type, normalized=$normalized, stride=$stride, offset=$offset, data=$data)"
    }
}

internal class VertexArray {
    private val attributes = hashMapOf<String, Attribute>()
    private var indices: Buffer? = null

    fun setAttribute(name: String, attribute: Attribute) {
        attributes[name] = attribute
    }

    fun getAttribute(name: String): Attribute? {
        return attributes[name]
    }

    fun getAttributes(): Set<Map.Entry<String, Attribute>> = attributes.entries

    fun setIndices(indices: Buffer) {
        this.indices = indices
    }

    fun getIndices(): Buffer? {
        return indices
    }
}