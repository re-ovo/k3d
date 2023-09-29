package me.rerere.k3d.util

import me.rerere.k3d.renderer.resource.DataType
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun FloatArray.toByteBuffer(): ByteBuffer {
    val buffer = ByteBuffer
        .allocate(this.size * 4)
        .order(ByteOrder.nativeOrder())
    buffer.asFloatBuffer().put(this)
    return buffer
}

fun IntArray.toByteBuffer(): ByteBuffer {
    val buffer = ByteBuffer
        .allocate(this.size * 4)
        .order(ByteOrder.nativeOrder())
    buffer.asIntBuffer().put(this)
    return buffer
}

fun ShortArray.toByteBuffer(): ByteBuffer {
    val buffer = ByteBuffer
        .allocate(this.size * 2)
        .order(ByteOrder.nativeOrder())
    buffer.asShortBuffer().put(this)
    return buffer
}

fun ByteArray.toByteBuffer(): ByteBuffer {
    val buffer = ByteBuffer
        .allocate(this.size)
        .order(ByteOrder.nativeOrder())
    buffer.put(this)
    return buffer
}

fun newByteBuffer(dataType: DataType, size: Int): ByteBuffer {
    return ByteBuffer
        .allocate(dataType.size * size)
        .order(ByteOrder.nativeOrder())
}