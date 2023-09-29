package me.rerere.k3d.scene.geometry

import me.rerere.k3d.renderer.resource.Attribute
import me.rerere.k3d.renderer.resource.DataType
import me.rerere.k3d.renderer.shader.BuiltInAttributeName
import me.rerere.k3d.util.newByteBuffer
import me.rerere.k3d.util.toByteBuffer

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
        setIndices(
            intArrayOf(
                0, 1, 2, 0, 2, 3,    // front
                4, 5, 6, 4, 6, 7,    // back
                8, 9, 10, 8, 10, 11,   // top
                12, 13, 14, 12, 14, 15,   // bottom
                16, 17, 18, 16, 18, 19,   // right
                20, 21, 22, 20, 22, 23,   // left
            ).toByteBuffer()
        )
        updateVao()
    }

    private fun updateVao() {
        updatePositionBuffer()
        updateNormalBuffer()
    }

    private fun updatePositionBuffer() {
        val attribute = this.getAttribute(BuiltInAttributeName.POSITION) ?: Attribute(
            itemSize = 3,
            type = DataType.FLOAT,
            normalized = false,
            data = newByteBuffer(DataType.FLOAT, 6 * 3 * 4),
            count = 6 * 4
        )

        attribute.data.asFloatBuffer().apply {
            rewind()

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
        }

        setAttribute(BuiltInAttributeName.POSITION, attribute)
    }

    private fun updateNormalBuffer() {
        val attribute = this.getAttribute(BuiltInAttributeName.NORMAL) ?: Attribute(
            itemSize = 3,
            type = DataType.FLOAT,
            normalized = false,
            data = newByteBuffer(DataType.FLOAT, 6 * 3 * 4),
            count = 6 * 4
        )

        attribute.data.asFloatBuffer().apply {
            rewind()

            // Front face
            repeat(4) {
                put(0.0f).put(0.0f).put(1.0f)
            }

            // Back face
            repeat(4) {
                put(0.0f).put(0.0f).put(-1.0f)
            }

            // Top face
            repeat(4) {
                put(0.0f).put(1.0f).put(0.0f)
            }

            // Bottom face
            repeat(4) {
                put(0.0f).put(-1.0f).put(0.0f)
            }

            // Right face
            repeat(4) {
                put(1.0f).put(0.0f).put(0.0f)
            }

            // Left face
            repeat(4) {
                put(-1.0f).put(0.0f).put(0.0f)
            }
        }

        setAttribute(BuiltInAttributeName.NORMAL, attribute)
    }
}