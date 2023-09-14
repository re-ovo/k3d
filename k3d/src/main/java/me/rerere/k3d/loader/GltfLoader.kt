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

        var jsonChunk : String? = null
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
                    jsonChunk = String(json)
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

        val gltf = GsonInstance.fromJson(jsonChunk, Gltf::class.java)
        println(gltf)
    }
}

internal data class Gltf(
    val accessors: List<Accessor>,
    val asset: Asset,
    val bufferViews: List<BufferView>,
    val buffers: List<Buffer>,
    val images: List<Image>,
    val materials: List<Material>,
    val meshes: List<Mesh>,
    val nodes: List<Node>,
    val samplers: List<Sampler>,
    val scene: Int,
    val scenes: List<Scene>,
    val textures: List<Texture>,
) {
    data class Accessor(
        val bufferView: Int,
        val byteOffset: Int?,
        val componentType: Int,
        val count: Int,
        val max: List<Float>,
        val min: List<Float>,
        val type: String,
    )

    data class Asset(
        val version: String,
        val generator: String,
        val extras: JsonObject?
    )

    data class BufferView(
        val buffer: Int,
        val byteLength: Int,
        val byteOffset: Int?,
        val byteStride: Int?,
        val target: Int,
        val name: String,
    )

    data class Buffer(
        val byteLength: Int,
        val uri: String?,
    )

    data class Image(
        val uri: String?,
        val mimeType: String?,
        val bufferView: Int?,
    )

    /**
     * [Material](https://registry.khronos.org/glTF/specs/2.0/glTF-2.0.html#materials-overview)
     */
    data class Material(
        val name: String,
        val pbrMetallicRoughness: MaterialPbrMetallicRoughness,
    )

    data class MaterialPbrMetallicRoughness(
        val baseColorFactor: List<Float>,
        val metallicFactor: Float,
        val roughnessFactor: Float,
        val baseColorTexture: Texture?,
        val metallicRoughnessTexture: Texture?,
    )

    data class MaterialTexture(
        val index: Int,
        val texCoord: Int,
        val scale: Float?,
    )

    data class Mesh(
        val name: String,
        val primitives: List<Primitive>,
    )

    data class Primitive(
        val attributes: Map<String, Int>,
        val indices: Int,
        val material: Int,
        val mode: Int,
    )

    data class Node(
        val name: String,
        val matrix: List<Float>?,
        val children: List<Int>,
    )

    data class Sampler(
        val magFilter: Int,
        val minFilter: Int,
        val wrapS: Int,
        val wrapT: Int,
    )

    data class Scene(
        val name: String,
        val nodes: List<Int>,
    )

    data class Texture(
        val name: String?,
        val sampler: Int,
        val source: Int,
    )
}