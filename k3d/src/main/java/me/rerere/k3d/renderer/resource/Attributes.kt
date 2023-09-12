package me.rerere.k3d.renderer.resource

import me.rerere.k3d.util.Dirty
import java.nio.Buffer

class Attribute(
    val name: String,
    val itemSize: Int,
    val type: DataType,
    val normalized: Boolean,
    val data: Buffer
): Dirty {
    override var dirty: Boolean = false
}

class VertexArray {
    private val attributes = hashMapOf<String, Attribute>()
    private var indices: MutableList<Int>? = null

    fun setAttribute(attribute: Attribute) {
        attributes[attribute.name] = attribute
    }

    fun getAttribute(name: String): Attribute? {
        return attributes[name]
    }

    fun getAttributes(): Collection<Attribute> {
        return attributes.values
    }

    fun setIndices(indices: List<Int>) {
        this.indices = indices.toMutableList()
    }

    fun getIndices(): List<Int>? {
        return indices
    }
}