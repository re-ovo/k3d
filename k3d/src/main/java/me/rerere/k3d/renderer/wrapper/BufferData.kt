package me.rerere.k3d.renderer.wrapper

import me.rerere.k3d.util.Dirty
import java.nio.Buffer
import java.nio.IntBuffer

class BufferData<T: Buffer>(
    private val data: T,
): Dirty {
    override var dirty: Boolean = true
}