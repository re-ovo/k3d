package me.rerere.k3d.loader

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.DataInputStream
import java.io.InputStream

private const val GLB_MAGIC = 0x676c5446 // "glTF"
private const val GLB_VERSION = 0x02000000

/**
 * glTF Loader
 *
 * It only supports glTF 2.0
 *
 * [glTF 2.0 Specification](https://registry.khronos.org/glTF/specs/2.0/glTF-2.0.html)
 */
@OptIn(ExperimentalStdlibApi::class)
object GltfLoader {
    fun load(inputStream: InputStream) {
        DataInputStream(inputStream).use {
            readGlb(it)
        }
    }

    private fun readGlb(buffered: DataInputStream) {
        val magic = buffered.readInt()
        val version = buffered.readInt()
        val length = buffered.readInt()

        // Check magic
        if (magic != GLB_MAGIC) {
            throw Exception("Not a glb file")
        }

        // Check version
        if (version != GLB_VERSION) {
            throw Exception("Unsupported glb version: ${version.toHexString()}")
        }

        var jsonChunk : JsonObject? = null
        var binChunk : ByteArray? = null

        // Read chunks
        while (buffered.available() > 0) {
            val chunkLength = buffered.readInt().reverseBytes()
            val chunkType = buffered.readInt()

            println("chunk type: ${chunkType.toHexString()}")
            println("chunk length: $chunkLength")

            when (chunkType) {
                // JSON
                0x4a534f4e -> {
                    val json = ByteArray(chunkLength)
                    buffered.read(json)
                    jsonChunk = JsonParser.parseString(json.decodeToString()).asJsonObject
                }
                // BIN
                0x42494E00 -> {
                    val bin = ByteArray(chunkLength)
                    buffered.read(bin)
                    binChunk = bin
                }
                else -> {
                    error("Unsupported chunk type: ${chunkType.toHexString()}")
                }
            }
        }

        println(jsonChunk)
        println(binChunk)
    }
}