package me.rerere.k3d.util

import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.FloatBuffer

fun Buffer.toFloatBuffer(): FloatBuffer {
    return when(this) {
        is FloatBuffer -> this
        is ByteBuffer -> asFloatBuffer()
        else -> throw IllegalArgumentException("Cannot convert buffer to float buffer")
    }
}