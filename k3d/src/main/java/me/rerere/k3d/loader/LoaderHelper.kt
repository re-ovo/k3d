package me.rerere.k3d.loader

import com.google.gson.Gson

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

private val GsonInstance by lazy { Gson() }