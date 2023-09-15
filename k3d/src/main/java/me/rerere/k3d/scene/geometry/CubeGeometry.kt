package me.rerere.k3d.scene.geometry

import me.rerere.k3d.renderer.resource.Attribute
import me.rerere.k3d.renderer.resource.DataType
import java.nio.FloatBuffer

class CubeGeometry(
    width: Float = 1f,
    height: Float = 1f,
    depth: Float = 1f,
) : BufferGeometry() {
    var width = width
        set(value) {
            field = value
            updateVao()
        }
    var height = height
        set(value) {
            field = value
            updateVao()
        }
    var depth = depth
        set(value) {
            field = value
            updateVao()
        }

    init {
        updateVao()
    }

    private fun updateVao() {
        val attribute = this.getAttribute("a_pos") ?: Attribute(
            "a_pos",
            3,
            DataType.FLOAT,
            false,
            FloatBuffer.allocate(6 * 3 * 4)
        )
        val buffer = attribute.data as FloatBuffer
        buffer.apply {
            clear()

            // Front face
            put(-1.0f * width).put(-1.0f * height).put(1.0f * depth)
            put(1.0f * width).put(-1.0f * height).put(1.0f * depth)
            put(1.0f * width).put(1.0f * height).put(1.0f * depth)
            put(-1.0f * width).put(1.0f * height).put(1.0f * depth)

            // Back face
            put(-1.0f * width).put(-1.0f * height).put(-1.0f * depth)
            put(-1.0f * width).put(1.0f * height).put(-1.0f * depth)
            put(1.0f * width).put(1.0f * height).put(-1.0f * depth)
            put(1.0f * width).put(-1.0f * height).put(-1.0f * depth)

            // Top face
            put(-1.0f * width).put(1.0f * height).put(-1.0f * depth)
            put(-1.0f * width).put(1.0f * height).put(1.0f * depth)
            put(1.0f * width).put(1.0f * height).put(1.0f * depth)
            put(1.0f * width).put(1.0f * height).put(-1.0f * depth)

            // Bottom face
            put(-1.0f * width).put(-1.0f * height).put(-1.0f * depth)
            put(1.0f * width).put(-1.0f * height).put(-1.0f * depth)
            put(1.0f * width).put(-1.0f * height).put(1.0f * depth)
            put(-1.0f * width).put(-1.0f * height).put(1.0f * depth)

            // Right face
            put(1.0f * width).put(-1.0f * height).put(-1.0f * depth)
            put(1.0f * width).put(1.0f * height).put(-1.0f * depth)
            put(1.0f * width).put(1.0f * height).put(1.0f * depth)
            put(1.0f * width).put(-1.0f * height).put(1.0f * depth)

            // Left face
            put(-1.0f * width).put(-1.0f * height).put(-1.0f * depth)
            put(-1.0f * width).put(-1.0f * height).put(1.0f * depth)
            put(-1.0f * width).put(1.0f * height).put(1.0f * depth)
            put(-1.0f * width).put(1.0f * height).put(-1.0f * depth)

            flip()
        }
        attribute.markDirty()
        setIndices(intArrayOf(
            0, 1, 2, 0, 2, 3,    // front
            4, 5, 6, 4, 6, 7,    // back
            8, 9, 10, 8, 10, 11,   // top
            12, 13, 14, 12, 14, 15,   // bottom
            16, 17, 18, 16, 18, 19,   // right
            20, 21, 22, 20, 22, 23,   // left
        ))
        setAttribute(attribute)
    }
}