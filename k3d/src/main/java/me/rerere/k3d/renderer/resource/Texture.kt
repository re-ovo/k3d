package me.rerere.k3d.renderer.resource

import android.graphics.Bitmap
import java.nio.ByteBuffer

sealed class Texture(
    val data: Bitmap,
    val width: Int,
    val height: Int,
    val wrapS: TextureWrap,
    val wrapT: TextureWrap,
    val minFilter: TextureFilter,
    val magFilter: TextureFilter,
) {
    class Texture2D(
        data: Bitmap,
        width: Int,
        height: Int,
        wrapS: TextureWrap = TextureWrap.REPEAT,
        wrapT: TextureWrap = TextureWrap.REPEAT,
        minFilter: TextureFilter = TextureFilter.LINEAR,
        magFilter: TextureFilter = TextureFilter.LINEAR,
    ) : Texture(data, width, height, wrapS, wrapT, minFilter, magFilter)

    class TextureCube(
        data: Bitmap,
        width: Int,
        height: Int,
        wrapS: TextureWrap = TextureWrap.REPEAT,
        wrapT: TextureWrap = TextureWrap.REPEAT,
        minFilter: TextureFilter = TextureFilter.LINEAR,
        magFilter: TextureFilter = TextureFilter.LINEAR,
    ) : Texture(data, width, height, wrapS, wrapT, minFilter, magFilter)

    override fun toString(): String {
        return "Texture(data=$data, width=$width, height=$height, wrapS=$wrapS, wrapT=$wrapT, minFilter=$minFilter, magFilter=$magFilter)"
    }
}