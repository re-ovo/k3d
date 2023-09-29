package me.rerere.k3d.util

import me.rerere.k3d.renderer.resource.DataType
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Convert [FloatArray] to [ByteBuffer]
 *
 * It will auto allocate a [ByteBuffer] with size of [FloatArray.size] * 4, and set the order to [ByteOrder.nativeOrder]
 *
 * @receiver [FloatArray]
 * @return [ByteBuffer]
 */
fun FloatArray.toByteBuffer(): ByteBuffer {
    val buffer = ByteBuffer.allocate(this.size * 4).order(ByteOrder.nativeOrder())
    buffer.asFloatBuffer().put(this)
    return buffer
}

/**
 * Convert [IntArray] to [ByteBuffer]
 *
 * It will auto allocate a [ByteBuffer] with size of [IntArray.size] * 4, and set the order to [ByteOrder.nativeOrder]
 *
 * @receiver [IntArray]
 * @return [ByteBuffer]
 */
fun IntArray.toByteBuffer(): ByteBuffer {
    val buffer = ByteBuffer.allocate(this.size * 4).order(ByteOrder.nativeOrder())
    buffer.asIntBuffer().put(this)
    return buffer
}

/**
 * Convert [ShortArray] to [ByteBuffer]
 *
 * It will auto allocate a [ByteBuffer] with size of [ShortArray.size] * 2, and set the order to [ByteOrder.nativeOrder]
 *
 * @receiver [ShortArray]
 * @return [ByteBuffer]
 */
fun ShortArray.toByteBuffer(): ByteBuffer {
    val buffer = ByteBuffer.allocate(this.size * 2).order(ByteOrder.nativeOrder())
    buffer.asShortBuffer().put(this)
    return buffer
}

/**
 * Convert [ByteArray] to [ByteBuffer]
 *
 * It will auto allocate a [ByteBuffer] with size of [ByteArray.size], and set the order to [ByteOrder.nativeOrder]
 *
 * @receiver [ByteArray]
 * @return [ByteBuffer]
 */
fun ByteArray.toByteBuffer(): ByteBuffer {
    val buffer = ByteBuffer.allocate(this.size).order(ByteOrder.nativeOrder())
    buffer.put(this)
    return buffer
}

/**
 * Create a new [ByteBuffer] with size of [DataType.size] * [size], and set the order to [ByteOrder.nativeOrder]
 *
 * @param dataType The [DataType] of the elements that will be stored in the [ByteBuffer]
 * @param size The max size of the elements
 * @return [ByteBuffer]
 */
fun newByteBuffer(dataType: DataType, size: Int): ByteBuffer {
    return ByteBuffer.allocate(dataType.size * size).order(ByteOrder.nativeOrder())
}