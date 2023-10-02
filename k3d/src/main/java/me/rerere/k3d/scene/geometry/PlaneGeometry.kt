package me.rerere.k3d.scene.geometry

import me.rerere.k3d.renderer.resource.Attribute
import me.rerere.k3d.renderer.resource.DataType
import me.rerere.k3d.renderer.shader.BuiltInAttributeName
import me.rerere.k3d.util.newByteBuffer
import me.rerere.k3d.util.toByteBuffer

class PlaneGeometry(
    val width: Float = 1f,
    val height: Float = 1f,
) : BufferGeometry() {
    init {
        updatePositionBuffer()
        updateNormalBuffer()

        setIndices(intArrayOf(
            0, 1, 2,
            0, 2, 3
        ))
    }

    private fun updatePositionBuffer() {
        val attribute = this.getAttribute(BuiltInAttributeName.POSITION) ?: Attribute(
            itemSize = 3,
            type = DataType.FLOAT,
            normalized = false,
            data = newByteBuffer(DataType.FLOAT, 3 * 4),
            count = 4
        )

        attribute.data.asFloatBuffer().apply {
            rewind()
            put(
                floatArrayOf(
                    -width / 2, 0f, -height / 2,
                    -width / 2, 0f, height / 2,
                    width / 2, 0f, height / 2,
                    width / 2, 0f, -height / 2,
                )
            )
        }

        this.setAttribute(BuiltInAttributeName.POSITION, attribute)
    }

    private fun updateNormalBuffer() {
        val attribute = this.getAttribute(BuiltInAttributeName.NORMAL) ?: Attribute(
            itemSize = 3,
            type = DataType.FLOAT,
            normalized = false,
            data = newByteBuffer(DataType.FLOAT, 3 * 4),
            count = 4
        )

        attribute.data.asFloatBuffer().apply {
            rewind()
            put(
                floatArrayOf(
                    0f, 1f, 0f,
                    0f, 1f, 0f,
                    0f, 1f, 0f,
                    0f, 1f, 0f,
                )
            )
        }

        this.setAttribute(BuiltInAttributeName.NORMAL, attribute)
    }
}