package me.rerere.k3d.renderer.wrapper

// Represents a set of attributes(VAO)
class Attributes {
    private val attributes = mutableListOf<Attribute>()
    private var index: IntArray? = null

    fun addAttribute(
        name: String,
        type: GLDataType,
        size: Int,
        normalized: Boolean = false,
        stride: Int = 0,
        offset: Int = 0
    ) {
        attributes.add(
            Attribute(
                name,
                type,
                size,
                normalized,
                stride,
                offset
            )
        )
    }

    fun getAttributes(): List<Attribute> = attributes

    fun getIndex(): IntArray? = index

    fun setIndex(index: IntArray) {
        this.index = index
    }
}

class Attribute(
    val name: String,
    val type: GLDataType,
    val size: Int,
    val normalized: Boolean,
    val stride: Int,
    val offset: Int,
)