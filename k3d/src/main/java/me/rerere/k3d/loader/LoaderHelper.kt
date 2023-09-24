package me.rerere.k3d.loader

import android.graphics.Bitmap
import android.opengl.GLUtils
import com.google.gson.GsonBuilder
import me.rerere.k3d.scene.actor.Actor
import me.rerere.k3d.scene.actor.ActorGroup
import me.rerere.k3d.scene.actor.Scene
import me.rerere.k3d.util.Color4f
import me.rerere.k3d.util.math.Vec3
import java.nio.ByteBuffer
import java.util.Stack

/**
 * Reverse bytes
 *
 * Usually convert from little endian to big endian
 */
internal fun Int.reverseBytes(): Int {
    return (this and 0xFF shl 24) or
            (this and 0xFF00 shl 8) or
            (this and 0xFF0000 shr 8) or
            (this and 0xFF000000.toInt() ushr 24)
}

internal val GsonInstance by lazy {
    GsonBuilder()
        .registerTypeAdapter(Color4f::class.java, Color4fAdapter)
        .registerTypeAdapter(Vec3::class.java, Vec3fAdapter)
        .create()
}

/**
 * Slice the byte buffer safely
 *
 * @param start start position
 * @param end end position
 */
internal fun ByteBuffer.sliceSafely(start: Int, end: Int): ByteBuffer = (
        this.clear()
            .position(start)
            .limit(end) as ByteBuffer
        )
    .slice()

/**
 * Create a empty(dumb) bitmap and fill it with [eraseColor]
 *
 * Sometimes we need a bitmap to create a texture, but we don't have it, so we create a empty bitmap
 * and fill it with [eraseColor], usually it's white(0xFFFFFFFF).
 *
 * @param width bitmap width
 * @param height bitmap height
 * @param eraseColor erase color
 *
 * @return a empty bitmap
 */
internal fun createEmptyBitmap(
    width: Int = 1,
    height: Int = 1,
    eraseColor: Int = 0xFFFFFFFF.toInt()
): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    bitmap.eraseColor(eraseColor)
    return bitmap
}

/**
 * Convert a bitmap to byte buffer
 *
 * @receiver bitmap
 * @return byte buffer
 */
@Deprecated("use GLUtils.texImage2D instead to avoid memory copy", ReplaceWith("GLUtils.texImage2D"))
internal fun Bitmap.toByteBuffer(): ByteBuffer {
    val buffer = ByteBuffer.allocate(this.byteCount)
    this.copyPixelsToBuffer(buffer)
    buffer.flip()
    return buffer
}

internal inline fun <R> Bitmap.use(block: (Bitmap) -> R): R {
    try {
        return block(this)
    } finally {
        this.recycle()
    }
}

/**
 * Dump the scene
 *
 * @receiver scene
 */
internal fun Scene.dump() {
    var currentDepth = 0
    val stack = Stack<Pair<Actor, Int>>()
    stack.push(Pair(this, 0))
    while (stack.isNotEmpty()) {
        val pair = stack.pop()
        val actor = pair.first
        currentDepth = pair.second
        if (actor is ActorGroup) {
            actor.getChildren().forEach {
                stack.push(Pair(it, currentDepth + 1))
            }
        }
        println("  ".repeat(currentDepth) + actor)
    }
}