package me.rerere.k3d.renderer.resource

import me.rerere.k3d.util.Dirty
import java.nio.Buffer

/**
 * This class represents a single attribute(such as position, normal, uv, etc.)
 *
 * @param name The name of this attribute
 * @param itemSize The size of each item in this attribute
 * @param type The data type of this attribute
 * @param normalized Whether the data should be normalized
 * @param data The data of this attribute
 */
class Attribute(
    val name: String,
    val itemSize: Int,
    val type: DataType,
    val normalized: Boolean,
    val data: Buffer
): Dirty {
    override var dirty: Boolean = false
}

internal class VertexArray {
    private val attributes = hashMapOf<String, Attribute>()
    private var indices: IntArray? = null

    fun setAttribute(attribute: Attribute) {
        attributes[attribute.name] = attribute
    }

    fun getAttribute(name: String): Attribute? {
        return attributes[name]
    }

    fun getAttributes(): Collection<Attribute> {
        return attributes.values
    }

    fun setIndices(indices: IntArray) {
        this.indices = indices
    }

    fun getIndices(): IntArray? {
        return indices
    }
}