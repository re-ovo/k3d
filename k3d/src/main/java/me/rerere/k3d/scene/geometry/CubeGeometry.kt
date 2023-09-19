package me.rerere.k3d.scene.geometry

import me.rerere.k3d.renderer.resource.Attribute
import me.rerere.k3d.renderer.resource.DataType
import me.rerere.k3d.renderer.shader.BuiltInAttributeName
import java.nio.FloatBuffer
import java.nio.IntBuffer

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
            IntBuffer.wrap(
                intArrayOf(
                    0, 1, 2, 0, 2, 3,    // front
                    4, 5, 6, 4, 6, 7,    // back
                    8, 9, 10, 8, 10, 11,   // top
                    12, 13, 14, 12, 14, 15,   // bottom
                    16, 17, 18, 16, 18, 19,   // right
                    20, 21, 22, 20, 22, 23,   // left
                )
            )
        )
        updateVao()
    }

    private fun updateVao() {
        updatePositionBuffer()
        updateNormalBuffer()
    }

    private fun updatePositionBuffer() {
        val attribute = this.getAttribute(BuiltInAttributeName.POSITION.attributeName) ?: Attribute(
            itemSize = 3,
            type = DataType.FLOAT,
            normalized = false,
            data = FloatBuffer.allocate(6 * 3 * 4)
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

        setAttribute(BuiltInAttributeName.POSITION.attributeName, attribute)
        attribute.markDirty()
    }

    private fun updateNormalBuffer() {
        val attribute = this.getAttribute(BuiltInAttributeName.NORMAL.attributeName) ?: Attribute(
            itemSize = 3,
            type = DataType.FLOAT,
            normalized = false,
            data = FloatBuffer.allocate(6 * 3 * 4)
        )
        val buffer = attribute.data as FloatBuffer
        buffer.apply {
            clear()

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

            flip()
        }

        setAttribute(BuiltInAttributeName.NORMAL.attributeName, attribute)
        // attribute.markDirty()
    }
}