package me.rerere.k3d.loader

import com.google.gson.JsonObject
import me.rerere.k3d.renderer.resource.Attribute
import me.rerere.k3d.renderer.resource.DataType
import me.rerere.k3d.renderer.resource.DrawMode
import me.rerere.k3d.scene.Actor
import me.rerere.k3d.scene.ActorGroup
import me.rerere.k3d.scene.Scene
import me.rerere.k3d.scene.actor.Primitive
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
        return parse(
            gltf = gltf,
            buffers = buildList {
                binChunk?.let {
                    add(it)
                }
            }
        )
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
            scenes = scenes,
            defaultScene = defaultScene
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
            var positionCount = 0 // if there is no indices, use position count for draw count
            val attributes = primitive.attributes.map { (key: String, accessorIndex: Int) ->
                when (key) {
                    "POSITION" -> {
                        val accessor = accessorOf(gltf, buffers, accessorIndex)
                        positionCount = accessor.count
                        accessor.asAttribute("a_pos")
                    }

                    "NORMAL" -> {
                        val accessor = accessorOf(gltf, buffers, accessorIndex)
                        accessor.asAttribute("a_normal")
                    }

                    else -> {
                        // println("Unsupported attribute: $key")
                        null
                    }
                }
            }
            val mode = gltfPrimitiveModeToDrawMode(primitive.mode)
            val indicesAccessor = primitive.indices?.let {
                accessorOf(gltf, buffers, it)
            }
            val indicesBuffer: Buffer? = primitive.indices?.let {
                val accessor = accessorOf(gltf, buffers, it)
                val bufferView = accessor.bufferView ?: error("Accessor bufferView is null")

                if (bufferView.byteStride > 0) {
                    error("BufferView byteStride > 0 is not supported yet for indices")
                }

                ((bufferView.buffer
                    .clear()
                    .position(bufferView.byteOffset + accessor.byteOffset)
                    .limit(bufferView.byteOffset + bufferView.byteLength + accessor.byteOffset)) as ByteBuffer)
                    .slice()
            }

            group.addChild(
                Primitive(
                    mode = mode,
                    geometry = BufferGeometry().apply {
                        attributes.filterNotNull().forEach { attr ->
                            setAttribute(attr)
                        }

                        indicesBuffer?.let {
                            setIndices(it)
                        }
                    },
                    material = StandardMaterial(),
                    count = indicesAccessor?.count ?: positionCount
                )
            )
        }

        return group
    }

    private fun Accessor.asAttribute(name: String): Attribute {
        require(bufferView != null) {
            "Accessor bufferView is null, this is not supported yet"
        }

        val buffer = ((
                bufferView.buffer
                    .clear()
                    .position(bufferView.byteOffset)
                    .limit(
                        bufferView.byteOffset + bufferView.byteLength
                    )
                ) as ByteBuffer)
            .slice()

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
    val scenes: List<Scene>,
    val defaultScene: Scene
)

internal data class BufferView(
    val buffer: ByteBuffer,
    val byteOffset: Int,
    val byteLength: Int,
    val byteStride: Int,
)

internal data class Accessor(
    val bufferView: BufferView?,
    val byteOffset: Int,
    val componentType: Int,
    val count: Int,
    val type: String,
)

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