package me.rerere.k3d.loader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.gson.JsonObject
import me.rerere.k3d.renderer.resource.Attribute
import me.rerere.k3d.renderer.resource.DataType
import me.rerere.k3d.renderer.resource.DrawMode
import me.rerere.k3d.renderer.resource.TextureFilter
import me.rerere.k3d.renderer.resource.TextureWrap
import me.rerere.k3d.renderer.shader.BuiltInAttributeName
import me.rerere.k3d.scene.actor.Actor
import me.rerere.k3d.scene.actor.ActorGroup
import me.rerere.k3d.scene.actor.Primitive
import me.rerere.k3d.scene.actor.Scene
import me.rerere.k3d.scene.geometry.BufferGeometry
import me.rerere.k3d.scene.material.StandardMaterial
import me.rerere.k3d.util.math.Matrix4
import me.rerere.k3d.util.math.transform.setModelMatrix
import java.io.DataInputStream
import java.io.InputStream
import java.nio.Buffer
import java.nio.ByteBuffer
import java.util.Stack

private const val GLB_MAGIC = 0x676c5446 // "glTF"
private const val GLB_VERSION = 0x02000000 // 2.0

/**
 * glTF Loader
 *
 * It only supports glTF 2.0
 *
 * [glTF 2.0 Specification](https://registry.khronos.org/glTF/specs/2.0/glTF-2.0.html)
 */
@OptIn(ExperimentalStdlibApi::class)
object GltfLoader {
    fun load(inputStream: InputStream): GltfLoadResult {
        return DataInputStream(inputStream).use {
            readGlb(it)
        }
    }

    private fun readGlb(buffered: DataInputStream): GltfLoadResult {
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

        var jsonChunk: String? = null
        var binChunk: ByteBuffer? = null

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
                    if (binChunk != null) {
                        error("Multiple BIN chunk")
                    }

                    val bin = ByteArray(chunkLength)
                    buffered.read(bin)
                    binChunk = ByteBuffer.wrap(bin)
                }

                else -> {
                    error("Unsupported chunk type: ${chunkType.toHexString()}")
                }
            }
        }

        println(jsonChunk)
        println(binChunk)

        val gltf = GsonInstance.fromJson(jsonChunk, Gltf::class.java)
        return parse(gltf = gltf, buffers = buildList {
            binChunk?.let {
                add(it)
            }
        })
    }

    private fun parse(gltf: Gltf, buffers: List<ByteBuffer>): GltfLoadResult {
        val scenes = parseScenes(gltf, buffers)
        val defaultScene = scenes[gltf.scene]

        println("Scenes: $scenes")
        println("Default scene: $defaultScene")

        // dump tree
        var currentDepth = 0
        val stack = Stack<Pair<ActorGroup, Int>>()
        stack.push(Pair(defaultScene, 0))
        while (stack.isNotEmpty()) {
            val pair = stack.pop()
            val actor = pair.first
            currentDepth = pair.second
            if (actor is ActorGroup) {
                actor.getChildren().forEach {
                    if (it is ActorGroup) {
                        stack.push(Pair(it, currentDepth + 1))
                    }
                }
            }
            println("  ".repeat(currentDepth) + actor)
        }

        return GltfLoadResult(
            scenes = scenes, defaultScene = defaultScene
        )
    }

    private fun parseScenes(gltf: Gltf, buffers: List<ByteBuffer>): List<Scene> {
        return gltf.scenes.map { gltfScene ->
            Scene().apply {
                name = gltfScene.name

                gltfScene.nodes.forEach { nodeIndex ->
                    val actor = parseNode(gltf, buffers, nodeIndex)
                    addChild(actor)
                }
            }
        }
    }

    private fun parseNode(gltf: Gltf, buffers: List<ByteBuffer>, node: Int): ActorGroup {
        val gltfNode = gltf.nodes[node]
        val group = ActorGroup().apply {
            name = gltfNode.name

            gltfNode.matrix?.let {
                setModelMatrix(Matrix4.fromColumnMajor(it.toFloatArray()))
            }
        }

        gltfNode.children?.forEach { childNode ->
            val childActor = parseNode(gltf, buffers, childNode)
            group.addChild(childActor)
        }

        gltfNode.mesh?.let {
            val mesh = parseMesh(gltf, buffers, it)
            group.addChild(mesh)
        }

        return group
    }

    private fun parseMesh(gltf: Gltf, buffers: List<ByteBuffer>, mesh: Int): Actor {
        val gltfMesh = gltf.meshes[mesh]
        val group = ActorGroup().apply {
            name = gltfMesh.name
        }

        gltfMesh.primitives.forEach { primitive ->
            // Attributes
            var positionCount = 0 // if there is no indices, use position count for draw count
            val attributes = primitive.attributes.map { (key: String, accessorIndex: Int) ->
                when (key) {
                    "POSITION" -> {
                        val accessor = accessorOf(gltf, buffers, accessorIndex)
                        positionCount = accessor.count
                        accessor.asAttribute(BuiltInAttributeName.POSITION.attributeName)
                    }

                    "NORMAL" -> {
                        val accessor = accessorOf(gltf, buffers, accessorIndex)
                        accessor.asAttribute(BuiltInAttributeName.NORMAL.attributeName)
                    }

                    "TANGENT" -> {
                        val accessor = accessorOf(gltf, buffers, accessorIndex)
                        accessor.asAttribute(BuiltInAttributeName.TANGENT.attributeName)
                    }

                    else -> {
                        println("Unsupported attribute: $key")
                        null
                    }
                }
            }.toMutableList()

            // Draw Mode
            val mode = gltfPrimitiveModeToDrawMode(primitive.mode)

            // Indices
            val indicesAccessor = primitive.indices?.let {
                accessorOf(gltf, buffers, it)
            }
            val indicesBuffer: Buffer? = primitive.indices?.let {
                val accessor = accessorOf(gltf, buffers, it)
                val bufferView = accessor.bufferView ?: error("Accessor bufferView is null")

                if (bufferView.byteStride > 0) {
                    error("BufferView byteStride > 0 is not supported yet for indices")
                }

                bufferView.buffer.sliceSafely(
                    start = bufferView.byteOffset + accessor.byteOffset,
                    end = bufferView.byteOffset + bufferView.byteLength + accessor.byteOffset
                )
            }

            // Material
            val materialData = primitive.material?.let {
                materialOf(gltf, buffers, it)
            }
            materialData?.let { material ->
                attributes += textureCoordAccessor(gltf, buffers, primitive.attributes, material.baseColorTextureCoord)
                    .asAttribute(BuiltInAttributeName.TEXCOORD_BASE.attributeName)

                attributes += textureCoordAccessor(gltf, buffers, primitive.attributes, material.normalTextureCoord)
                    .asAttribute(BuiltInAttributeName.TEXCOORD_NORMAL.attributeName)

                attributes += textureCoordAccessor(gltf, buffers, primitive.attributes, material.occulsionTextureCoord)
                    .asAttribute(BuiltInAttributeName.TEXCOORD_OCCLUSION.attributeName)

                attributes += textureCoordAccessor(gltf, buffers, primitive.attributes, material.metallicRoughnessTextureCoord)
                    .asAttribute(BuiltInAttributeName.TEXCOORD_ROUGHNESS.attributeName)

                attributes += textureCoordAccessor(gltf, buffers, primitive.attributes, material.metallicRoughnessTextureCoord)
                    .asAttribute(BuiltInAttributeName.TEXCOORD_METALLIC.attributeName)
            }

            val geometry = BufferGeometry().apply {
                attributes.filterNotNull().forEach { attr ->
                    setAttribute(attr)
                }
                indicesBuffer?.let {
                    setIndices(it)
                }
            }
            val material = StandardMaterial().apply {
                baseColorTexture = materialData?.baseColorTexture?.toTexture2d(requireLinear = true)
                normalTexture = materialData?.normalTexture?.toTexture2d()

//                occlusionTexture = materialData?.occlusionTexture?.toTexture2d()
//                roughnessTexture = materialData?.metallicRoughnessTexture?.toTexture2d()
//                metallicTexture = materialData?.metallicRoughnessTexture?.toTexture2d()
            }
            group.addChild(
                Primitive(
                    mode = mode,
                    geometry = geometry,
                    material = material,
                    count = indicesAccessor?.count ?: positionCount
                )
            )
        }

        return group
    }

    private fun Texture.toTexture2d(requireLinear: Boolean = false): me.rerere.k3d.renderer.resource.Texture.Texture2D {
        return image.use {
            me.rerere.k3d.renderer.resource.Texture.Texture2D(
                data = it.toByteBuffer(),
                wrapS = wrapS,
                wrapT = wrapT,
                minFilter = minFilter,
                magFilter = magFilter,
                width = image.width,
                height = image.height,
                // todo: set color space
            )
        }
    }

    private fun textureCoordAccessor(
        gltf: Gltf,
        buffers: List<ByteBuffer>,
        attributes: Map<String, Int>,
        coordIndex: Int
    ): Accessor {
        val key = "TEXCOORD_$coordIndex"
        val index = attributes[key] ?: error("No attribute: $key")
        return accessorOf(gltf, buffers, index)
    }

    private fun Accessor.asAttribute(name: String): Attribute {
        require(bufferView != null) {
            "Accessor bufferView is null, this is not supported yet"
        }

        val buffer = bufferView.buffer.sliceSafely(
            start = bufferView.byteOffset, end = bufferView.byteOffset + bufferView.byteLength
        )

        return Attribute(
            name = name,
            data = buffer,
            itemSize = gltfAccessorItemSizeOf(type),
            type = gltfAccessorComponentTypeToDataType(componentType),
            normalized = false,
            stride = bufferView.byteStride,
            offset = byteOffset,
        )
    }

    private fun bufferViewOf(gltf: Gltf, buffers: List<ByteBuffer>, index: Int): BufferView {
        val bufferView = gltf.bufferViews[index]
        val buffer = buffers[bufferView.buffer]
        val byteOffset = bufferView.byteOffset ?: 0
        val byteLength = bufferView.byteLength
        val byteStride = bufferView.byteStride ?: 0
        return BufferView(
            buffer = buffer,
            byteOffset = byteOffset,
            byteLength = byteLength,
            byteStride = byteStride,
        )
    }

    private fun accessorOf(gltf: Gltf, buffers: List<ByteBuffer>, index: Int): Accessor {
        val accessor = gltf.accessors[index]
        val bufferView = bufferViewOf(gltf, buffers, accessor.bufferView)
        return Accessor(
            bufferView = bufferView,
            componentType = accessor.componentType,
            count = accessor.count,
            type = accessor.type,
            byteOffset = accessor.byteOffset ?: 0,
        )
    }

    private fun materialOf(gltf: Gltf, buffers: List<ByteBuffer>, index: Int): Material {
        val material = gltf.materials[index]

//        val occlusionTexture = material.occlusionTexture?.let {
//            textureOf(gltf, buffers, it.index)
//        }
//        val metallicRoughnessTexture =
//            material.pbrMetallicRoughness?.metallicRoughnessTexture?.let {
//                textureOf(gltf, buffers, it.index)
//            }

        return Material(
            name = material.name ?: "",
            alphaMode = material.alphaMode ?: "OPAQUE",
            alphaCutoff = material.alphaCutoff ?: 0.5f,
            doubleSided = material.doubleSided ?: false,
            baseColorFactor = material.pbrMetallicRoughness?.baseColorFactor ?: listOf(
                1f, 1f, 1f, 1f
            ),
            baseColorTexture = material.pbrMetallicRoughness?.baseColorTexture?.let {
                textureOf(gltf, buffers, it.index)
            },
            baseColorTextureCoord = material.pbrMetallicRoughness?.baseColorTexture?.texCoord ?: 0,
            metallicFactor = material.pbrMetallicRoughness?.metallicFactor ?: 1f,
            roughnessFactor = material.pbrMetallicRoughness?.roughnessFactor ?: 1f,
            metallicRoughnessTexture = material.pbrMetallicRoughness?.metallicRoughnessTexture?.let {
                textureOf(gltf, buffers, it.index)
            },
            metallicRoughnessTextureCoord = material.pbrMetallicRoughness?.metallicRoughnessTexture?.texCoord ?: 0,
            normalTexture = material.normalTexture?.let {
                textureOf(gltf, buffers, it.index)
            },
            normalTextureCoord = material.normalTexture?.texCoord ?: 0,
            occlusionTexture = material.occlusionTexture?.let {
                textureOf(gltf, buffers, it.index)
            },
            occulsionTextureCoord = material.occlusionTexture?.texCoord ?: 0,
            emissiveFactor = material.emissiveFactor ?: listOf(0f, 0f, 0f),
            emissiveTexture = material.emissiveTexture?.let {
                textureOf(gltf, buffers, it.index)
            },
        )
    }

    private fun textureOf(gltf: Gltf, buffers: List<ByteBuffer>, index: Int): Texture {
        val texture = gltf.textures[index]
        val sampler = gltf.samplers[texture.sampler]
        return Texture(
            image = imageOf(gltf, buffers, texture.source),
            wrapS = TextureWrap.fromValue(sampler.wrapS),
            wrapT = TextureWrap.fromValue(sampler.wrapT),
            minFilter = TextureFilter.fromValue(sampler.minFilter),
            magFilter = TextureFilter.fromValue(sampler.magFilter),
        )
    }

    private fun imageOf(gltf: Gltf, buffers: List<ByteBuffer>, index: Int): Bitmap {
        val image = gltf.images[index]

        if (image.bufferView != null) {
            val bufferView = bufferViewOf(gltf, buffers, image.bufferView)
            val buffer = bufferView.buffer.sliceSafely(
                start = bufferView.byteOffset, end = bufferView.byteOffset + bufferView.byteLength
            )

            // Convert image to bitmap
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } else if (image.uri != null) {
            error("Unsupported image uri yet (gltf): ${image.uri}")
        } else {
            error("Unsupported image (gltf): $image")
        }
    }
}

private fun gltfPrimitiveModeToDrawMode(gltfMode: Int?) =
    if (gltfMode != null) DrawMode.entries[gltfMode] else DrawMode.TRIANGLES

private fun gltfAccessorComponentTypeToDataType(componentType: Int): DataType {
    return when (componentType) {
        5120 -> DataType.BYTE
        5121 -> DataType.UNSIGNED_BYTE
        5122 -> DataType.SHORT
        5123 -> DataType.UNSIGNED_SHORT
        5125 -> DataType.UNSIGNED_INT
        5126 -> DataType.FLOAT
        else -> error("Unsupported component type: $componentType")
    }.also {
        require(it.value == componentType) {
            "DataType value mismatch: ${it.value} != $componentType"
        }
    }
}

private fun gltfAccessorItemSizeOf(type: String): Int {
    return when (type) {
        "SCALAR" -> 1
        "VEC2" -> 2
        "VEC3" -> 3
        "VEC4" -> 4
        "MAT2" -> 4
        "MAT3" -> 9
        "MAT4" -> 16
        else -> error("Unsupported type: $type")
    }
}

data class GltfLoadResult(
    val scenes: List<Scene>, val defaultScene: Scene
)

private data class BufferView(
    val buffer: ByteBuffer,
    val byteOffset: Int,
    val byteLength: Int,
    val byteStride: Int,
)

private data class Accessor(
    val bufferView: BufferView?,
    val byteOffset: Int,
    val componentType: Int,
    val count: Int,
    val type: String,
)

private data class Material(
    val name: String,
    val baseColorFactor: List<Float>?,
    val baseColorTexture: Texture?,
    val baseColorTextureCoord: Int,
    val metallicFactor: Float?,
    val roughnessFactor: Float?,
    val metallicRoughnessTexture: Texture?,
    val metallicRoughnessTextureCoord: Int,
    val normalTexture: Texture?,
    val normalTextureCoord: Int,
    val occlusionTexture: Texture?,
    val occulsionTextureCoord: Int,
    val emissiveTexture: Texture?,
    val emissiveFactor: List<Float>?,
    val alphaMode: String?,
    val alphaCutoff: Float?,
    val doubleSided: Boolean?,
)

private data class Texture(
    val image: Bitmap,
    val wrapS: TextureWrap,
    val wrapT: TextureWrap,
    val minFilter: TextureFilter,
    val magFilter: TextureFilter,
)

private data class Gltf(
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
        val version: String, val generator: String, val extras: JsonObject?
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
     * [Material](https://registry.khronos.org/glTF/specs/2.0/glTF-2.0.html#reference-material)
     */
    data class Material(
        val name: String?,
        val pbrMetallicRoughness: MaterialPbrMetallicRoughness?,
        val normalTexture: TextureInfo?,
        val occlusionTexture: TextureInfo?,
        val emissiveTexture: TextureInfo?,
        val emissiveFactor: List<Float>?,
        val alphaMode: String?,
        val alphaCutoff: Float?,
        val doubleSided: Boolean?,
    )

    data class MaterialPbrMetallicRoughness(
        val baseColorFactor: List<Float>?,
        val baseColorTexture: TextureInfo?,
        val metallicFactor: Float?,
        val roughnessFactor: Float?,
        val metallicRoughnessTexture: TextureInfo?,
    )

    data class TextureInfo(
        val index: Int,
        val texCoord: Int?,
        val scale: Float?, // for normal texture
        val strength: Float?, // for occlusion texture
    )

    data class Mesh(
        val name: String,
        val primitives: List<Primitive>,
    )

    data class Primitive(
        val attributes: Map<String, Int>,
        val indices: Int?,
        val material: Int?,
        val mode: Int?,
    )

    data class Node(
        val name: String,
        val matrix: List<Float>?,
        val children: List<Int>?,
        val mesh: Int?,
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