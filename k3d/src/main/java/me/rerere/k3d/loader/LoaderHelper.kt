package me.rerere.k3d.loader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.gson.Gson
import java.nio.ByteBuffer

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

internal val GsonInstance by lazy { Gson() }

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
internal fun Bitmap.toByteBuffer(): ByteBuffer {
    val buffer = ByteBuffer.allocateDirect(this.byteCount)
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