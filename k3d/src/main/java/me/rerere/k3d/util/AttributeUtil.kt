package me.rerere.k3d.util

import me.rerere.k3d.renderer.resource.Attribute
import me.rerere.k3d.renderer.shader.BuiltInAttributeName
import me.rerere.k3d.scene.geometry.BufferGeometry
import java.nio.ByteBuffer

internal fun Attribute.sequenceFloatData(count: Int): Sequence<FloatArray> {
    val floatBuffer = ((data as ByteBuffer).position(offset) as ByteBuffer).slice().asFloatBuffer()
    val itemSize = itemSize
    return sequence {
        repeat(count) {
            val floatArray = FloatArray(itemSize)
            floatBuffer.get(floatArray)
            yield(floatArray)
        }
    }
}

internal fun BufferGeometry.computeTangent(count: Int) {
    val positions = getAttribute(BuiltInAttributeName.POSITION.attributeName)?.sequenceFloatData(count)?.toList() ?: return
    val normals = getAttribute(BuiltInAttributeName.NORMAL.attributeName)?.sequenceFloatData(count)?.toList() ?: return
    val uvs = getAttribute(BuiltInAttributeName.TEXCOORD_NORMAL.attributeName)?.sequenceFloatData(count)?.toList() ?: return

    val tangentData = FloatArray(count * 3)

    // TODO: 需要考虑index
}