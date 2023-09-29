package me.rerere.k3d.util

import me.rerere.k3d.renderer.resource.DataType
import java.nio.ByteBuffer

fun FloatArray.toByteBuffer(): ByteBuffer {
    val buffer = ByteBuffer.allocate(this.size * 4)
    buffer.asFloatBuffer().put(this)
    return buffer
}

fun IntArray.toByteBuffer(): ByteBuffer {
    val buffer = ByteBuffer.allocate(this.size * 4)
    buffer.asIntBuffer().put(this)
    return buffer
}

fun ShortArray.toByteBuffer(): ByteBuffer {
    val buffer = ByteBuffer.allocate(this.size * 2)
    buffer.asShortBuffer().put(this)
    return buffer
}

fun ByteArray.toByteBuffer(): ByteBuffer {
    val buffer = ByteBuffer.allocate(this.size)
    buffer.put(this)
    return buffer
}

fun newByteBuffer(dataType: DataType, size: Int): ByteBuffer {
    return ByteBuffer.allocate(dataType.size * size)
}