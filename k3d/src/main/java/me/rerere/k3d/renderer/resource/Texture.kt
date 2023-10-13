package me.rerere.k3d.renderer.resource

import android.graphics.Bitmap
import me.rerere.k3d.util.system.Dirty
import me.rerere.k3d.util.system.Disposable
import java.nio.Buffer

sealed class Texture(
    val width: Int,
    val height: Int,
    val wrapS: TextureWrap,
    val wrapT: TextureWrap,
    val minFilter: TextureFilter,
    val magFilter: TextureFilter,
): Dirty, Disposable {
    private var _dirty = false

    override fun dispose() {}

    override fun isDirty(): Boolean {
        return _dirty
    }

    override fun markDirtyNew() {
        _dirty = true
    }

    override fun clearDirty() {
        _dirty = false
    }

    override fun updateDirty() {}

    class Texture2D(
        val data: Bitmap,
        width: Int,
        height: Int,
        wrapS: TextureWrap = TextureWrap.REPEAT,
        wrapT: TextureWrap = TextureWrap.REPEAT,
        minFilter: TextureFilter = TextureFilter.LINEAR,
        magFilter: TextureFilter = TextureFilter.LINEAR,
    ) : Texture(width, height, wrapS, wrapT, minFilter, magFilter)

    class TextureCube(
        val data: Bitmap,
        width: Int,
        height: Int,
        wrapS: TextureWrap = TextureWrap.REPEAT,
        wrapT: TextureWrap = TextureWrap.REPEAT,
        minFilter: TextureFilter = TextureFilter.LINEAR,
        magFilter: TextureFilter = TextureFilter.LINEAR,
    ) : Texture(width, height, wrapS, wrapT, minFilter, magFilter)

    class DataTexture(
        val data: Buffer,
        width: Int,
        height: Int,
        wrapS: TextureWrap = TextureWrap.REPEAT,
        wrapT: TextureWrap = TextureWrap.REPEAT,
        minFilter: TextureFilter = TextureFilter.LINEAR,
        magFilter: TextureFilter = TextureFilter.LINEAR,
    ) : Texture(width, height, wrapS, wrapT, minFilter, magFilter)

    override fun toString(): String {
        return "Texture(width=$width, height=$height, wrapS=$wrapS, wrapT=$wrapT, minFilter=$minFilter, magFilter=$magFilter)"
    }
}