package me.rerere.k3d.loader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import me.rerere.k3d.renderer.resource.Attribute
import me.rerere.k3d.renderer.resource.DataType
import me.rerere.k3d.renderer.resource.DrawMode
import me.rerere.k3d.renderer.resource.Texture
import me.rerere.k3d.renderer.resource.TextureFilter
import me.rerere.k3d.renderer.resource.TextureWrap
import me.rerere.k3d.renderer.shader.BuiltInAttributeName
import me.rerere.k3d.scene.actor.Actor
import me.rerere.k3d.scene.actor.ActorGroup
import me.rerere.k3d.scene.actor.Mesh
import me.rerere.k3d.scene.actor.Scene
import me.rerere.k3d.scene.actor.Skeleton
import me.rerere.k3d.scene.actor.SkinMesh
import me.rerere.k3d.scene.actor.traverse
import me.rerere.k3d.scene.animation.AnimationClip
import me.rerere.k3d.scene.animation.AnimationTarget
import me.rerere.k3d.scene.animation.Interpolation
import me.rerere.k3d.scene.animation.KeyframeTrack
import me.rerere.k3d.scene.geometry.BufferGeometry
import me.rerere.k3d.scene.material.AlphaMode
import me.rerere.k3d.scene.material.StandardMaterial
import me.rerere.k3d.util.Color
import me.rerere.k3d.util.math.Matrix4
import me.rerere.k3d.util.math.Vec3
import me.rerere.k3d.util.math.transform.setModelMatrix
import java.io.DataInputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.IdentityHashMap
import java.util.UUID

private const val GLB_MAGIC = 0x676c5446 // "glTF"
private const val GLB_VERSION = 0x02000000 // 2.0

/**
 * glTF Loader
 *
 * Note that it only supports glTF 2.0
 *
 * [glTF 2.0 Specification](https://registry.khronos.org/glTF/specs/2.0/glTF-2.0.html)
 *
 * Supported extensions:
 * - [KHR_materials_pbrSpecularGlossiness](https://github.com/KhronosGroup/glTF/tree/main/extensions/2.0/Archived/KHR_materials_pbrSpecularGlossiness): support `diffuseTexture` only
 */
@OptIn(ExperimentalStdlibApi::class)
class GltfLoader {
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
        JsonParser.parseString(jsonChunk).asJsonObject.let {
            it.keySet().forEach { key ->
                println("$key: ${it.get(key)}")
            }
        }

        val gltf = GsonInstance.fromJson(jsonChunk, Gltf::class.java)
        return Parser().parse(gltf = gltf, buffers = buildList {
            binChunk?.let {
                add(it)
            }
        })
    }

    private class Parser {
        private val id2NodeMapping = hashMapOf<Int, Actor>()
        private val node2IdMapping = hashMapOf<Actor, Int>()

        private val id2WrapperTexture = hashMapOf<Int, WrapperTexture>()
        private val wrapperTexture2Texture2d = IdentityHashMap<WrapperTexture, Texture.Texture2D>()

        fun parse(gltf: Gltf, buffers: List<ByteBuffer>): GltfLoadResult {
            val scenes = parseScenes(gltf, buffers)
            val defaultScene = scenes[gltf.scene]

            println("Scenes: $scenes")
            println("Default scene: $defaultScene")

            defaultScene.dump() // for debug purpose, remove it in the future

            return GltfLoadResult(
                scenes = scenes,
                defaultScene = defaultScene,
                animations = parseAnimations(gltf, buffers),
            )
        }

        private fun parseAnimations(gltf: Gltf, buffers: List<ByteBuffer>): ArrayList<AnimationClip> {
            val animations = arrayListOf<AnimationClip>()
            gltf.animations?.forEach { animationClip ->
                val tracks = arrayListOf<KeyframeTrack>()

                animationClip.channels.forEach { channel ->
                    val node = id2NodeMapping[channel.target.node]
                        ?: error("No node for index: ${channel.target.node}")
                    val target = when (channel.target.path) {
                        "translation" -> AnimationTarget.Position(node)
                        "rotation" -> AnimationTarget.Rotation(node)
                        "scale" -> AnimationTarget.Scale(node)
                        "weights" -> AnimationTarget.Weight(node)
                        else -> error("Unsupported animation target path: ${channel.target.path}")
                    }
                    val sampler = animationClip.samplers[channel.sampler]

                    val accessorInput = accessorOf(gltf, buffers, sampler.input)
                    val accessorOutput = accessorOf(gltf, buffers, sampler.output)

                    val input = FloatArray(accessorInput.count)
                    accessorInput.bufferView?.let { bufferView ->
                        val buffer = bufferView.buffer.sliceSafely(
                            start = bufferView.byteOffset + accessorInput.byteOffset,
                            end = bufferView.byteOffset + bufferView.byteLength + accessorInput.byteOffset
                        )
                        buffer.order(ByteOrder.nativeOrder())
                        buffer.asFloatBuffer().get(input)
                    }

                    require(gltfAccessorComponentTypeToDataType(accessorOutput.componentType) == DataType.FLOAT) {
                        "Animation output data type must be FLOAT, but got ${
                            gltfAccessorComponentTypeToDataType(
                                accessorOutput.componentType
                            )
                        }"
                    }
                    val itemSize = gltfAccessorItemSizeOf(accessorOutput.type)
                    val output = FloatArray(accessorOutput.count * itemSize)
                    accessorOutput.bufferView?.let { bufferView ->
                        val buffer = bufferView.buffer.sliceSafely(
                            start = bufferView.byteOffset + accessorOutput.byteOffset,
                            end = bufferView.byteOffset + bufferView.byteLength + accessorOutput.byteOffset
                        )
                        buffer.order(ByteOrder.nativeOrder())
                        buffer.asFloatBuffer().get(output)
                    }

                    val frames = buildList {
                        for (i in 0 until accessorInput.count) {
                            val start = i * itemSize
                            val end = start + itemSize
                            add(input[i] to output.slice(start until end).toFloatArray())
                        }
                    }
                    tracks += KeyframeTrack(
                        target = target,
                        keyframes = frames,
                        interpolation = Interpolation.fromString(
                            sampler.interpolation ?: "LINEAR"
                        ),
                    )
                }

                val duration = tracks.maxOfOrNull { it.keyframes.last().first } ?: 0f

                animations += AnimationClip(
                    name = animationClip.name ?: "",
                    tracks = tracks,
                    duration = duration,
                )
            }
            return animations
        }

        private fun parseScenes(
            gltf: Gltf,
            buffers: List<ByteBuffer>,
        ): List<Scene> {
            return gltf.scenes.map { gltfScene ->
                Scene().apply {
                    name = gltfScene.name

                    // Parse Nodes
                    gltfScene.nodes.forEach { nodeIndex ->
                        val actor =
                            parseNode(gltf, buffers, nodeIndex)
                        addChild(actor)
                    }

                    // Parse Meshes
                    traverse { actor ->
                        if (actor is ActorGroup) {
                            val nodeIndex = node2IdMapping[actor] ?: return@traverse
                            val gltfNode = gltf.nodes[nodeIndex]

                            gltfNode.mesh?.let { meshIndex ->
                                val skin = gltfNode.skin?.let { skinIndex ->
                                    gltf.skins?.get(skinIndex)
                                        ?: error("No skin for index: $skinIndex")
                                }

                                val mesh = parseMesh(gltf, buffers, meshIndex, skin)
                                actor.addChild(mesh)
                            }
                        }
                    }
                }
            }
        }

        private fun parseNode(
            gltf: Gltf,
            buffers: List<ByteBuffer>,
            node: Int,
        ): ActorGroup {
            val gltfNode = gltf.nodes[node]
            val group = ActorGroup().apply {
                name = gltfNode.name

                // Load rotation/scale/translation from matrix
                gltfNode.matrix?.let {
                    setModelMatrix(Matrix4.fromColumnMajor(it))
                }

                // Load rotation/scale/translation from individual properties
                gltfNode.rotation?.let {
                    rotation.set(it[0], it[1], it[2], it[3])
                }
                gltfNode.scale?.let {
                    scale.set(it.x, it.y, it.z)
                }
                gltfNode.translation?.let {
                    position.set(it.x, it.y, it.z)
                }
            }

            gltfNode.children?.forEach { childNode ->
                val childActor = parseNode(gltf, buffers, childNode)
                group.addChild(childActor)
            }

            id2NodeMapping[node] = group
            node2IdMapping[group] = node

            return group
        }

        private fun parseMesh(
            gltf: Gltf,
            buffers: List<ByteBuffer>,
            meshIndex: Int,
            skin: Gltf.Skin?,
        ): Actor {
            val gltfMesh = gltf.meshes[meshIndex]
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
                            println("Position count: ${accessor.count}")
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
                            val accessor = accessorOf(gltf, buffers, accessorIndex)
                            println(
                                "Unsupported attribute: $key(${accessor.type}/${
                                    gltfAccessorComponentTypeToDataType(
                                        accessor.componentType
                                    )
                                }/${accessor.count})"
                            )
                            null
                        }
                    }
                }.toMutableList()

                // Skin
                var skeleton: Skeleton? = null
                if (skin != null) {
                    // Read joints and weights attributes
                    primitive.attributes["JOINTS_0"]?.let {
                        val accessor = accessorOf(gltf, buffers, it)
                        attributes += accessor.asAttribute(BuiltInAttributeName.JOINTS.attributeName)
                    }

                    primitive.attributes["WEIGHTS_0"]?.let {
                        val accessor = accessorOf(gltf, buffers, it)
                        attributes += accessor.asAttribute(BuiltInAttributeName.WEIGHTS.attributeName)
                    }

                    require(primitive.attributes["JOINTS_1"] == null) {
                        "Multiple JOINTS is not supported yet"
                    }
                    require(primitive.attributes["WEIGHTS_1"] == null) {
                        "Multiple WEIGHTS is not supported yet"
                    }

                    // Read inverseBindMatrices
                    val inverseBindMatricesAccessor = skin.inverseBindMatrices?.let {
                        accessorOf(gltf, buffers, it)
                    }
                    val inverseBindMatrices = inverseBindMatricesAccessor?.let { accessor ->
                        val bufferView = accessor.bufferView ?: error("Accessor bufferView is null")

                        require(accessor.type == "MAT4") {
                            "InverseBindMatrices type must be MAT4, but got ${accessor.type}"
                        }
                        require(accessor.count >= skin.joints.size) {
                            "InverseBindMatrices count must >= joints count, but got ${accessor.count} < ${skin.joints.size}"
                        }

                        val data = FloatArray(accessor.count * 16)
                        val buffer = bufferView.buffer.sliceSafely(
                            start = bufferView.byteOffset + accessor.byteOffset,
                            end = bufferView.byteOffset + bufferView.byteLength + accessor.byteOffset
                        )
                        buffer.order(ByteOrder.nativeOrder())
                        buffer.asFloatBuffer().get(data)

                        buildList {
                            for (i in 0 until accessor.count) {
                                val start = i * 16
                                val end = start + 16
                                add(Matrix4.fromColumnMajor(data.slice(start until end)))
                            }
                        }
                    }

                    skeleton = Skeleton(
                        bones = skin.joints
                            .map { id2NodeMapping[it] ?: error("No node for joint: $it") }
                            .mapIndexed { index, actor ->
                                Skeleton.Bone(
                                    node = actor,
                                    inverseBindMatrix = inverseBindMatrices?.get(index)
                                        ?: Matrix4.identity()
                                )
                            },
                    )
                }

                // Draw Mode
                val mode = gltfPrimitiveModeToDrawMode(primitive.mode)
                require(mode == DrawMode.TRIANGLES) {
                    "Draw mode must be TRIANGLES, but got $mode"
                }

                // Indices
                val indicesAccessor = primitive.indices?.let {
                    accessorOf(gltf, buffers, it)
                }
                val indicesBuffer: Pair<ByteBuffer, DataType>? = primitive.indices?.let {
                    val accessor = accessorOf(gltf, buffers, it)
                    val bufferView = accessor.bufferView ?: error("Accessor bufferView is null")

                    val dataType = gltfAccessorComponentTypeToDataType(accessor.componentType)
                    require(dataType == DataType.UNSIGNED_INT || dataType == DataType.UNSIGNED_SHORT) {
                        "Indices data type must be UNSIGNED_INT/SHORT, but got $dataType"
                    }

                    if (bufferView.byteStride > 0) {
                        error("BufferView byteStride > 0 is not supported yet for indices: ${bufferView.byteStride}")
                    }

                    bufferView.buffer.sliceSafely(
                        start = bufferView.byteOffset + accessor.byteOffset,
                        end = bufferView.byteOffset + bufferView.byteLength + accessor.byteOffset
                    ) to dataType
                }

                // Material
                val materialData = primitive.material?.let {
                    materialOf(gltf, buffers, it)
                }
                materialData?.let { material ->
                    material.baseColorTexture?.let {
                        attributes += textureCoordAccessor(
                            gltf,
                            buffers,
                            primitive.attributes,
                            material.baseColorTextureCoord ?: 0
                        )
                            .asAttribute(BuiltInAttributeName.TEXCOORD_BASE.attributeName)
                    }

                    material.normalTexture?.let {
                        attributes += textureCoordAccessor(
                            gltf,
                            buffers,
                            primitive.attributes,
                            material.normalTextureCoord ?: 0
                        )
                            .asAttribute(BuiltInAttributeName.TEXCOORD_NORMAL.attributeName)
                    }

                    material.occlusionTexture?.let {
                        attributes += textureCoordAccessor(
                            gltf,
                            buffers,
                            primitive.attributes,
                            material.occulsionTextureCoord ?: 0
                        )
                            .asAttribute(BuiltInAttributeName.TEXCOORD_OCCLUSION.attributeName)
                    }

                    material.metallicRoughnessTexture?.let {
                        attributes += textureCoordAccessor(
                            gltf,
                            buffers,
                            primitive.attributes,
                            material.metallicRoughnessTextureCoord ?: 0
                        )
                            .asAttribute(BuiltInAttributeName.TEXCOORD_ROUGHNESS.attributeName)
                    }

                    material.metallicRoughnessTexture?.let {
                        attributes += textureCoordAccessor(
                            gltf,
                            buffers,
                            primitive.attributes,
                            material.metallicRoughnessTextureCoord ?: 0
                        )
                            .asAttribute(BuiltInAttributeName.TEXCOORD_METALLIC.attributeName)
                    }

                    material.emissiveTexture?.let {
                        attributes += textureCoordAccessor(
                            gltf,
                            buffers,
                            primitive.attributes,
                            material.emissiveTextureCoord ?: 0
                        )
                            .asAttribute(BuiltInAttributeName.TEXCOORD_EMISSIVE.attributeName)
                    }
                }

                val material = StandardMaterial().apply {
                    name = materialData?.name ?: UUID.randomUUID().toString()

                    alphaMode = materialData?.alphaMode ?: AlphaMode.OPAQUE
                    alphaCutoff = materialData?.alphaCutoff ?: 0.5f
                    doubleSided = materialData?.doubleSided ?: false

                    baseColor = materialData?.baseColorFactor ?: Color.white()
                    baseColorTexture = materialData?.baseColorTexture?.toTexture2d()

                    normalTexture = materialData?.normalTexture?.toTexture2d()

                    roughnessTexture = materialData?.metallicRoughnessTexture?.toTexture2d()
                    roughness = materialData?.roughnessFactor ?: 1f

                    metallicTexture = roughnessTexture
                    metallic = materialData?.metallicFactor ?: 1f

                    occlusionTexture = materialData?.occlusionTexture?.toTexture2d()

                    emissiveTexture = materialData?.emissiveTexture?.toTexture2d()
                    emissive = materialData?.emissiveFactor ?: Color.black()
                }

                val geometry = BufferGeometry().apply {
                    attributes.filterNotNull().forEach { (name, attr) ->
                        setAttribute(name, attr)
                    }
                    indicesBuffer?.let {
                        setIndices(
                            Attribute(
                                itemSize = 1,
                                type = it.second,
                                normalized = false,
                                count = indicesAccessor?.count ?: error("No indices count"),
                                data = it.first
                            )
                        )
                    }
                }

                val mesh = if (skeleton == null) {
                    Mesh(
                        geometry = geometry,
                        material = material,
                    )
                } else {
                    SkinMesh(
                        geometry = geometry,
                        material = material,
                        skeleton = skeleton,
                    )
                }

                group.addChild(mesh)
            }

            return group
        }

        private fun textureCoordAccessor(
            gltf: Gltf,
            buffers: List<ByteBuffer>,
            attributes: Map<String, Int>,
            coordIndex: Int
        ): WrapperAccessor {
            val key = "TEXCOORD_$coordIndex"
            val index = attributes[key] ?: error("No attribute: $key")
            return accessorOf(gltf, buffers, index)
        }

        private fun WrapperAccessor.asAttribute(name: String): Pair<String, Attribute> {
            require(bufferView != null) {
                "Accessor bufferView is null, this is not supported yet"
            }

            val buffer = bufferView.buffer.sliceSafely(
                start = bufferView.byteOffset + this.byteOffset,
                end = bufferView.byteOffset + bufferView.byteLength
            )

            val itemSize = gltfAccessorItemSizeOf(type)
            val dataType = gltfAccessorComponentTypeToDataType(componentType)

            require(bufferView.byteStride == itemSize * dataType.size || bufferView.byteStride == 0) {
                "BufferView byteStride != itemSize * dataType.size, this is not supported: ${bufferView.byteStride} != ${itemSize * dataType.size}"
            }

            return name to Attribute(
                data = buffer,
                itemSize = itemSize,
                type = dataType,
                normalized = false,
                count = count,
            )
        }

        private fun bufferViewOf(gltf: Gltf, buffers: List<ByteBuffer>, index: Int): WrapperBufferView {
            val bufferView = gltf.bufferViews[index]
            val buffer = buffers[bufferView.buffer]
            val byteOffset = bufferView.byteOffset ?: 0
            val byteLength = bufferView.byteLength
            val byteStride = bufferView.byteStride ?: 0
            return WrapperBufferView(
                buffer = buffer,
                byteOffset = byteOffset,
                byteLength = byteLength,
                byteStride = byteStride,
            )
        }

        private fun accessorOf(gltf: Gltf, buffers: List<ByteBuffer>, index: Int): WrapperAccessor {
            val accessor = gltf.accessors[index]
            val bufferView = bufferViewOf(gltf, buffers, accessor.bufferView)

            require(accessor.sparse == null) {
                // TODO: support accessor.sparse
                "Accessor sparse is not supported yet"
            }

            return WrapperAccessor(
                bufferView = bufferView,
                componentType = accessor.componentType,
                count = accessor.count,
                type = accessor.type,
                byteOffset = accessor.byteOffset ?: 0,
            )
        }

        private fun materialOf(gltf: Gltf, buffers: List<ByteBuffer>, index: Int): WrapperMaterial {
            val materialData = gltf.materials[index]
            val material = WrapperMaterial(
                name = materialData.name ?: "",
                alphaMode = materialData.alphaMode ?: AlphaMode.OPAQUE,
                alphaCutoff = materialData.alphaCutoff ?: 0.5f,
                doubleSided = materialData.doubleSided ?: false,
                baseColorFactor = materialData.pbrMetallicRoughness?.baseColorFactor,
                baseColorTexture = materialData.pbrMetallicRoughness?.baseColorTexture?.let {
                    textureOf(gltf, buffers, it.index)
                },
                baseColorTextureCoord = materialData.pbrMetallicRoughness?.baseColorTexture?.texCoord,
                metallicFactor = materialData.pbrMetallicRoughness?.metallicFactor ?: 1f,
                roughnessFactor = materialData.pbrMetallicRoughness?.roughnessFactor ?: 1f,
                metallicRoughnessTexture = materialData.pbrMetallicRoughness?.metallicRoughnessTexture?.let {
                    textureOf(gltf, buffers, it.index)
                },
                metallicRoughnessTextureCoord = materialData.pbrMetallicRoughness?.metallicRoughnessTexture?.texCoord,
                normalTexture = materialData.normalTexture?.let {
                    textureOf(gltf, buffers, it.index)
                },
                normalTextureCoord = materialData.normalTexture?.texCoord,
                occlusionTexture = materialData.occlusionTexture?.let {
                    textureOf(gltf, buffers, it.index)
                },
                occulsionTextureCoord = materialData.occlusionTexture?.texCoord,
                emissiveFactor = materialData.emissiveFactor ?: Color.black(),
                emissiveTexture = materialData.emissiveTexture?.let {
                    textureOf(gltf, buffers, it.index)
                },
                emissiveTextureCoord = materialData.emissiveTexture?.texCoord,
            )

            materialData.extensions?.let { extensions ->
                extensions.pbrSpecularGlossiness?.let { pbrSpecularGlossiness ->
                    pbrSpecularGlossiness.diffuseTexture?.let { textureInfo ->
                        material.baseColorTexture = textureOf(gltf, buffers, textureInfo.index)
                        material.baseColorTextureCoord = textureInfo.texCoord
                    }
                    pbrSpecularGlossiness.diffuseFactor?.let { factor ->
                        material.baseColorFactor = factor
                    }
                }
            }

            return material
        }

        private fun textureOf(gltf: Gltf, buffers: List<ByteBuffer>, index: Int): WrapperTexture {
            id2WrapperTexture[index]?.let {
                return it
            }

            val texture = gltf.textures[index]
            val sampler = gltf.samplers[texture.sampler]
            return WrapperTexture(
                image = imageOf(gltf, buffers, texture.source),
                wrapS = sampler.wrapS?.let { TextureWrap.fromValue(it) } ?: TextureWrap.REPEAT,
                wrapT = sampler.wrapT?.let { TextureWrap.fromValue(it) } ?: TextureWrap.REPEAT,
                minFilter = sampler.minFilter?.let { TextureFilter.fromValue(it) }
                    ?: TextureFilter.LINEAR,
                magFilter = sampler.magFilter?.let { TextureFilter.fromValue(it) }
                    ?: TextureFilter.LINEAR,
            ).also {
                id2WrapperTexture[index] = it
            }
        }

        private fun imageOf(gltf: Gltf, buffers: List<ByteBuffer>, index: Int): Bitmap {
            val image = gltf.images[index]

            if (image.bufferView != null) {
                val bufferView = bufferViewOf(gltf, buffers, image.bufferView)
                val buffer = bufferView.buffer.sliceSafely(
                    start = bufferView.byteOffset,
                    end = bufferView.byteOffset + bufferView.byteLength
                )

                // Convert image to bitmap
                val bytes = ByteArray(buffer.remaining())
                buffer.order(ByteOrder.nativeOrder())
                buffer.get(bytes)
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } else if (image.uri != null) {
                error("Unsupported image uri yet (gltf): ${image.uri}")
            } else {
                error("Unsupported image (gltf): $image")
            }
        }

        private fun WrapperTexture.toTexture2d(): Texture.Texture2D {
            wrapperTexture2Texture2d[this]?.let {
                return it
            }

            return image.let {
                Texture.Texture2D(
                    data = it,
                    wrapS = wrapS,
                    wrapT = wrapT,
                    minFilter = minFilter,
                    magFilter = magFilter,
                    width = image.width,
                    height = image.height,
                )
            }.also {
                wrapperTexture2Texture2d[this] = it
            }
        }
    }

    private data class WrapperBufferView(
        val buffer: ByteBuffer,
        val byteOffset: Int,
        val byteLength: Int,
        val byteStride: Int,
    )

    private data class WrapperAccessor(
        val bufferView: WrapperBufferView?,
        val byteOffset: Int,
        val componentType: Int,
        val count: Int,
        val type: String,
    )

    private data class WrapperMaterial(
        val name: String,
        var baseColorFactor: Color?,
        var baseColorTexture: WrapperTexture?,
        var baseColorTextureCoord: Int?,
        val metallicFactor: Float?,
        val roughnessFactor: Float?,
        val metallicRoughnessTexture: WrapperTexture?,
        val metallicRoughnessTextureCoord: Int?,
        val normalTexture: WrapperTexture?,
        val normalTextureCoord: Int?,
        val occlusionTexture: WrapperTexture?,
        val occulsionTextureCoord: Int?,
        val emissiveTexture: WrapperTexture?,
        val emissiveTextureCoord: Int?,
        val emissiveFactor: Color?,
        val alphaMode: AlphaMode?,
        val alphaCutoff: Float?,
        val doubleSided: Boolean?,
    )

    private data class WrapperTexture(
        val image: Bitmap,
        val wrapS: TextureWrap,
        val wrapT: TextureWrap,
        val minFilter: TextureFilter,
        val magFilter: TextureFilter,
    )

}

private fun gltfPrimitiveModeToDrawMode(gltfMode: Int?) =
    if (gltfMode != null) DrawMode.entries[gltfMode] else DrawMode.TRIANGLES

private fun gltfAccessorComponentTypeToDataType(componentType: Int): DataType =
    DataType.fromValue(componentType)

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
    val defaultScene: Scene,
    val animations: List<AnimationClip>,
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
    val skins: List<Skin>?,
    val animations: List<Animation>?,
    val extensionsUsed: List<String>?,
) {
    data class Accessor(
        val bufferView: Int,
        val byteOffset: Int?,
        val componentType: Int,
        val count: Int,
        val max: List<Float>,
        val min: List<Float>,
        val type: String,
        val sparse: Sparse?,
    ) {
        data class Sparse(
            val count: Int,
        )
    }

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
        val emissiveFactor: Color?,
        val alphaMode: AlphaMode?,
        val alphaCutoff: Float?,
        val doubleSided: Boolean?,
        val extensions: MaterialExtensions?,
    )

    data class MaterialPbrMetallicRoughness(
        val baseColorFactor: Color?,
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
        val weights: List<Float>?,
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
        val skin: Int?,
        val rotation: List<Float>?,
        val scale: Vec3?,
        val translation: Vec3?,
        val weights: List<Float>?,
    )

    data class Sampler(
        val magFilter: Int?,
        val minFilter: Int?,
        val wrapS: Int?,
        val wrapT: Int?,
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

    data class Skin(
        val joints: List<Int>,
        val skeleton: Int?,
        val inverseBindMatrices: Int?,
    )

    data class Animation(
        val name: String?,
        val channels: List<Channel>,
        val samplers: List<Sampler>,
    ) {
        data class Channel(
            val sampler: Int,
            val target: Target,
        ) {
            data class Target(
                val node: Int?,
                val path: String, // target property: oneOf("translation", "rotation", "scale", "weights")
            )
        }

        data class Sampler(
            val input: Int,
            val interpolation: String?,
            val output: Int,
        )
    }

    // Material Extension
    data class MaterialExtensions(
        @SerializedName("KHR_materials_pbrSpecularGlossiness")
        val pbrSpecularGlossiness: ExtMaterialPbrSpecularGlossiness?,
    )

    // Extension: KHR_materials_pbrSpecularGlossiness
    data class ExtMaterialPbrSpecularGlossiness(
        val diffuseFactor: Color?,
        val diffuseTexture: TextureInfo?,
        val glossinessFactor: Float?,
        val specularFactor: Color?,
        val specularGlossinessTexture: TextureInfo?,
    )
}