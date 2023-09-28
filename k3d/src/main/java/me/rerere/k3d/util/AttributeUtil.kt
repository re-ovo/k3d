package me.rerere.k3d.util

import me.rerere.k3d.renderer.resource.Attribute
import me.rerere.k3d.renderer.resource.DataType
import me.rerere.k3d.renderer.shader.BuiltInAttributeName
import me.rerere.k3d.scene.geometry.BufferGeometry
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import kotlin.math.sqrt

internal fun Attribute.readFloatData(count: Int): List<FloatArray> {
    val floatBuffer =
        ((data as ByteBuffer).position(offset) as ByteBuffer)
            .slice()
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
    val itemSize = itemSize
    return buildList {
        for (i in 0 until count) {
            val floatArray = FloatArray(itemSize)
            floatBuffer.get(floatArray)
            add(floatArray)
        }
    }
}

internal fun BufferGeometry.readIndices(count: Int, indiceType: DataType): IntArray {
    val indices = getIndices()
    return if (indices != null) {
        (indices as ByteBuffer).apply {
            rewind()
            order(ByteOrder.nativeOrder())
        }.let { buffer ->
            when(indiceType) {
                DataType.UNSIGNED_INT -> {
                    val result = IntArray(count)
                    buffer.asIntBuffer().get(result)
                    result
                }

                DataType.UNSIGNED_SHORT -> {
                    val result = ShortArray(count)
                    buffer.asShortBuffer().get(result)
                    result.map { it.toInt() }.toIntArray()
                }

                else -> error("Unsupported indice type: $indiceType")
            }
        }

    } else {
        IntArray(count) { it }
    }
}

internal fun BufferGeometry.computeTangent(vertCount: Int, indicesCount: Int) {
    val positions =
        getAttribute(BuiltInAttributeName.POSITION.attributeName)?.readFloatData(vertCount)
            ?: return
    val uvs =
        getAttribute(BuiltInAttributeName.TEXCOORD_NORMAL.attributeName)?.readFloatData(vertCount)
            ?: return
    val indices = readIndices(indicesCount, vao.getIndiceType())

    require(indices.size % 3 == 0) { "Indices size must be a multiple of 3" }
    require(indices.all { it < vertCount }) { "Indices must be less than vertex count" }

    val tangentData = FloatArray(vertCount * 3)

    for (i in indices.indices step 3) {
        val i0: Int = indices[i]
        val i1: Int = indices[i + 1]
        val i2: Int = indices[i + 2]

        val v0: FloatArray = positions[i0]
        val v1: FloatArray = positions[i1]
        val v2: FloatArray = positions[i2]

        val uv0: FloatArray = uvs[i0]
        val uv1: FloatArray = uvs[i1]
        val uv2: FloatArray = uvs[i2]

        val edge1 = v1 - v0
        val edge2 = v2 - v0

        val deltaUV1 = uv1 - uv0
        val deltaUV2 = uv2 - uv0

        val f = 1.0f / (deltaUV1[0] * deltaUV2[1] - deltaUV2[0] * deltaUV1[1])

        val tangent = FloatArray(3)
        tangent[0] = f * (deltaUV2[1] * edge1[0] - deltaUV1[1] * edge2[0])
        tangent[1] = f * (deltaUV2[1] * edge1[1] - deltaUV1[1] * edge2[1])
        tangent[2] = f * (deltaUV2[1] * edge1[2] - deltaUV1[1] * edge2[2])

        tangentData[i0 * 3 + 0] = tangent[0]
        tangentData[i0 * 3 + 1] = tangent[1]
        tangentData[i0 * 3 + 2] = tangent[2]

        tangentData[i1 * 3 + 0] = tangent[0]
        tangentData[i1 * 3 + 1] = tangent[1]
        tangentData[i1 * 3 + 2] = tangent[2]

        tangentData[i2 * 3 + 0] = tangent[0]
        tangentData[i2 * 3 + 1] = tangent[1]
        tangentData[i2 * 3 + 2] = tangent[2]
    }
    setAttribute(
        BuiltInAttributeName.TANGENT.attributeName, Attribute(
            itemSize = 3,
            data = FloatBuffer.wrap(tangentData),
            offset = 0,
            normalized = false,
            stride = 0,
            type = DataType.FLOAT,
            count = vertCount
        )
    )
}

private operator fun FloatArray.minus(other: FloatArray): FloatArray {
    return FloatArray(size) { this[it] - other[it] }
}

private fun FloatArray.length(): Double {
    val sum = sumOf { it * it.toDouble() }
    return sqrt(sum)
}