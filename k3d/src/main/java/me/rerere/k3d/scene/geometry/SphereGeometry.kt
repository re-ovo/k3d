package me.rerere.k3d.scene.geometry

import me.rerere.k3d.renderer.resource.Attribute
import me.rerere.k3d.renderer.resource.DataType
import me.rerere.k3d.renderer.shader.BuiltInAttributeName
import me.rerere.k3d.util.toByteBuffer
import kotlin.math.cos
import kotlin.math.sin

class SphereGeometry(
    radius: Float = 1f,
    widthSegments: Int = 32,
    heightSegments: Int = 16,
) : BufferGeometry() {
    var radius = radius
        set(value) {
            field = value
            computeVertex()
        }

    var widthSegments = widthSegments
        set(value) {
            field = value
            computeVertex()
        }

    var heightSegments = heightSegments
        set(value) {
            field = value
            computeVertex()
        }

    init {
        computeVertex()
    }

    private fun computeVertex() {
        val vertexCount = (widthSegments + 1) * (heightSegments + 1)
        val grid = arrayListOf<ArrayList<Int>>()
        val positions = FloatArray(vertexCount * 3)
        val normals = FloatArray(vertexCount * 3)

        var index = 0
        for (y in 0..heightSegments) {
            val v = y.toFloat() / heightSegments
            val row = arrayListOf<Int>()
            for (x in 0..widthSegments) {
                val u = x.toFloat() / widthSegments

                val x = -radius * cos(u * 2 * Math.PI) * sin(v * Math.PI)
                val y = radius * cos(v * Math.PI)
                val z = radius * sin(u * 2 * Math.PI) * sin(v * Math.PI)

                positions[index * 3] = x.toFloat()
                positions[index * 3 + 1] = y.toFloat()
                positions[index * 3 + 2] = z.toFloat()

                normals[index * 3] = (x / radius).toFloat()
                normals[index * 3 + 1] = (y / radius).toFloat()
                normals[index * 3 + 2] = (z / radius).toFloat()

                row.add(index)
                index++
            }

            grid.add(row)
        }

        // TODO: 别创建新的attribute，因为内存地址变了，导致没法更新buffer
        this.setAttribute(BuiltInAttributeName.POSITION, Attribute(
            itemSize = 3,
            type = DataType.FLOAT,
            normalized = false,
            data = positions.toByteBuffer(),
            count = vertexCount
        ).apply {
            markDirtyNew()
        })

        this.setAttribute(BuiltInAttributeName.NORMAL, Attribute(
            itemSize = 3,
            type = DataType.FLOAT,
            normalized = false,
            data = normals.toByteBuffer(),
            count = vertexCount
        ).apply {
            markDirtyNew()
        })

        val indices = arrayListOf<Int>()
        for (iy in 0 until heightSegments) {
            for (ix in 0 until widthSegments) {
                val a = grid[iy][ix + 1]
                val b = grid[iy][ix]
                val c = grid[iy + 1][ix]
                val d = grid[iy + 1][ix + 1]

                if (iy != 0) {
                    indices.add(a)
                    indices.add(b)
                    indices.add(d)
                }

                if (iy != heightSegments - 1) {
                    indices.add(b)
                    indices.add(c)
                    indices.add(d)
                }
            }
        }
        this.setIndices(indices.toIntArray(), markDirty = true)
    }
}