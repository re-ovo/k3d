package me.rerere.k3d.renderer

import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLUtils
import me.rerere.k3d.renderer.resource.Attribute
import me.rerere.k3d.renderer.resource.Texture
import me.rerere.k3d.renderer.resource.TextureFilter
import me.rerere.k3d.renderer.resource.TextureWrap
import me.rerere.k3d.renderer.resource.Uniform
import me.rerere.k3d.renderer.resource.VertexArray
import me.rerere.k3d.renderer.shader.BuiltInUniformName
import me.rerere.k3d.renderer.shader.ShaderProcessor
import me.rerere.k3d.renderer.shader.ShaderProgramSource
import me.rerere.k3d.renderer.shader.createProgram
import me.rerere.k3d.renderer.shader.createShader
import me.rerere.k3d.renderer.shader.genBuffer
import me.rerere.k3d.renderer.shader.genTexture
import me.rerere.k3d.renderer.shader.genVertexArray
import me.rerere.k3d.scene.actor.Primitive
import me.rerere.k3d.scene.actor.Scene
import me.rerere.k3d.scene.actor.Skeleton
import me.rerere.k3d.scene.actor.SkinMesh
import me.rerere.k3d.scene.actor.traverse
import me.rerere.k3d.scene.camera.Camera
import me.rerere.k3d.scene.light.AmbientLight
import me.rerere.k3d.scene.light.DirectionalLight
import me.rerere.k3d.scene.light.PointLight
import me.rerere.k3d.scene.light.SpotLight
import me.rerere.k3d.scene.material.AlphaMode
import me.rerere.k3d.util.math.Matrix4
import me.rerere.k3d.util.math.Vec3
import me.rerere.k3d.util.math.ceilPowerOf2
import me.rerere.k3d.util.system.DirtyQueue
import me.rerere.k3d.util.system.Disposable
import me.rerere.k3d.util.system.currentFrameDirty
import me.rerere.k3d.util.system.fastForeach
import me.rerere.k3d.util.system.markCurrentFrameDirty
import me.rerere.k3d.util.system.withoutMarkDirty
import java.nio.ByteBuffer
import java.util.IdentityHashMap
import kotlin.math.ceil
import kotlin.math.sqrt

/**
 * GLES3 Renderer
 *
 * It is a renderer that uses OpenGL ES 3.0
 */
class GLES3Renderer : Renderer {
    private val shaderProcessor = ShaderProcessor()
    private val resourceManager = GL3ResourceManager(shaderProcessor)

    override var viewportSize: ViewportSize = ViewportSize(0, 0)

    private val _opaqueActors = arrayListOf<Primitive>()
    private val _transparentActors = arrayListOf<Primitive>()

    override fun dispose() {
        this.resourceManager.dispose()
    }

    override fun resize(width: Int, height: Int) {
        viewportSize = ViewportSize(width, height)
    }

    private val worldMatrixUniform = Uniform.Mat4(Matrix4.identity(), true)
    private val viewMatrixUniform = Uniform.Mat4(Matrix4.identity(), true)
    private val projectionMatrixUniform = Uniform.Mat4(Matrix4.identity(), true)
    private val cameraPositionUniform = Uniform.Vec3f(Vec3(0f, 0f, 0f))

    override fun render(scene: Scene, camera: Camera) {
        // update dirty actors
        DirtyQueue.frameStart()
        this.render0(scene, camera)

        DirtyQueue.frameEnd()
    }

    private fun render0(scene: Scene, camera: Camera) {
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES20.glViewport(0, 0, viewportSize.width, viewportSize.height)

        GLES20.glEnable(GLES30.GL_BLEND)
        GLES20.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        scene.traverse { actor ->
            if (actor is Primitive) {
                when (actor.material.alphaMode) {
                    AlphaMode.OPAQUE -> _opaqueActors.add(actor)
                    AlphaMode.BLEND -> _transparentActors.add(actor)
                    AlphaMode.MASK -> _transparentActors.add(actor).also {
                        error("AlphaMode.MASK is not supported yet")
                    }
                }
            }
        }

        // render opaque actors
        _opaqueActors.fastForeach { actor ->
            renderPrimitive(actor, camera, scene)
        }

        // render transparent actors
        GLES20.glDepthMask(false)
        _transparentActors.fastForeach { actor ->
            renderPrimitive(actor, camera, scene)
        }
        GLES20.glDepthMask(true)

        _opaqueActors.clear()
        _transparentActors.clear()
    }

    private fun renderPrimitive(actor: Primitive, camera: Camera, scene: Scene) {
        if (actor.material.doubleSided) {
            GLES20.glDisable(GLES20.GL_CULL_FACE)
        } else {
            GLES20.glEnable(GLES20.GL_CULL_FACE)
        }

        resourceManager.useProgram(actor.material.program) {
            // Apply Lights
            resourceManager.useLights(this, scene)

            // Apply uniforms
            actor.material.uniforms.forEach { (name, uniform) ->
                resourceManager.useUniform(actor.material.program, uniform, name)
            }

            // Apply skin uniforms
            if (actor is SkinMesh) {
                resourceManager.useSkinBones(this, actor)

//                actor.geometry.getAttribute(BuiltInAttributeName.POSITION)?.let { positionAttr ->
//                    val positions = positionAttr.readFloatData(positionAttr.count)
//                    val skinIndex = actor.geometry.getAttribute(BuiltInAttributeName.JOINTS)?.readShortData(positionAttr.count)!!
//                    val skinWeight = actor.geometry.getAttribute(BuiltInAttributeName.WEIGHTS)?.readFloatData(positionAttr.count)!!
//
//                    positions.forEachIndexed { index, floats ->
//                        val (x, y, z) = floats
//                        val skinIndexs = skinIndex[index]
//                        val skinWeights = skinWeight[index]
//
//
//
//                        var matrix = Matrix4.zero()
//                        repeat(4) { i ->
//                            val boneIndex = skinIndexs[i].toInt()
//                            val i = boneIndex * 4
//                            val x = i % 32
//                            val y = i / 32
//
//                            require(x in 0..31) { "x: $x" }
//                            require(y in 0..31) { "y: $y" }

//                            val boneWeight = skinWeights[i]
//                            val bone = actor.skeleton.bones[boneIndex]
//                            val boneMatrix = bone.node.worldMatrix * bone.inverseBindMatrix

//                            matrix += boneMatrix * boneWeight
//                        }
//
//                        val position = Vec4(x, y, z, 1f)
//                        val transformed = camera.projectionMatrix * camera.worldMatrixInverse * matrix * position
//                        val w = transformed.w
//                        println("w: $w")
//                        if (w != 0f) {
//                            position.x = transformed.x / w
//                            position.y = transformed.y / w
//                            position.z = transformed.z / w
//                        } else {
//                            error("w == 0")
//                        }
//
//                        require(position.z >= -1f && position.z <= 1f) { "z: ${position.z}" }
//                    }
//               }
            }

            // Apply built-in uniforms
            resourceManager.useUniform(
                actor.material.program,
                worldMatrixUniform.apply {
                    value = actor.worldMatrix
                },
                BuiltInUniformName.MODEL_MATRIX.uniformName
            )
            resourceManager.useUniform(
                actor.material.program,
                viewMatrixUniform.apply {
                    value = camera.worldMatrixInverse
                },
                BuiltInUniformName.VIEW_MATRIX.uniformName
            )
            resourceManager.useUniform(
                actor.material.program,
                projectionMatrixUniform.apply {
                    value = camera.projectionMatrix
                },
                BuiltInUniformName.PROJECTION_MATRIX.uniformName
            )
            resourceManager.useUniform(
                actor.material.program,
                cameraPositionUniform.apply {
                    value.withoutMarkDirty {
                        x = camera.position.x
                        y = camera.position.y
                        z = camera.position.z
                    }
                },
                BuiltInUniformName.CAMERA_POSITION.uniformName
            )

            // Apply textures
            actor.material.textures.entries.forEachIndexed { index, mutableEntry ->
                val (name, texture) = mutableEntry
                resourceManager.useTexture(
                    actor.material.program,
                    name,
                    texture,
                    index,
                    false
                )
            }

            resourceManager.useVertexArray(this, actor.geometry.vao) { // bind vao
                // Draw call
                if (actor.geometry.getIndices() == null) {
                    GLES20.glDrawArrays(
                        actor.mode.value,
                        0,
                        actor.geometry.drawCount
                    )
                } else {
                    GLES20.glDrawElements(
                        actor.mode.value,
                        actor.geometry.drawCount,
                        actor.geometry.vao.getIndices()?.type?.value
                            ?: error("Invalid indice type"),
                        0
                    )
                }
            }
        }
    }
}

internal class GL3ResourceManager(private val shaderProcessor: ShaderProcessor) : Disposable {
    // program(shaders) related resources
    private val programs = IdentityHashMap<ShaderProgramSource, Int>()

    // vao related resources
    private val vertexArrays = IdentityHashMap<VertexArray, Int>()
    private val vertexArraysAttributesBuffer = IdentityHashMap<Attribute, Int>()
    private val vertexArraysIndicesBuffer = IdentityHashMap<VertexArray, Int>()

    // texture related resources
    private val textureBuffers = IdentityHashMap<Texture, Int>()

    // bone matrix texture
    private val boneTextures = IdentityHashMap<Skeleton, Texture.DataTexture>()

    inline fun useProgram(program: ShaderProgramSource, scope: ShaderProgramSource.() -> Unit) {
        if (program.currentFrameDirty) {
            deleteProgram(program)
            println("Update program: $program due to dirty")
        }

        val programId = this.getProgram(program) ?: this.createProgram(program)
            .getOrThrow()
        GLES30.glUseProgram(programId)
        scope(program)
        GLES30.glUseProgram(0)
    }

    inline fun useVertexArray(
        program: ShaderProgramSource,
        vertexArray: VertexArray,
        scope: () -> Unit
    ) {
        val vao = getVertexArray(vertexArray) ?: createVertexArray(program, vertexArray)
            .getOrThrow()

        updateVertexArray(vertexArray)

        GLES30.glBindVertexArray(vao)
        scope()
        GLES30.glBindVertexArray(0)
    }

    fun useUniform(program: ShaderProgramSource, uniform: Uniform, name: String) {
        val programId = getProgram(program) ?: return
        when (uniform) {
            is Uniform.Float -> {
                val location = GLES30.glGetUniformLocation(programId, name)
                if (location != -1) {
                    GLES30.glUniform1f(location, uniform.value)
                }
            }

            is Uniform.Int -> {
                val location = GLES30.glGetUniformLocation(programId, name)
                if (location != -1) {
                    GLES30.glUniform1i(location, uniform.value)
                }
            }

            is Uniform.Vec3f -> {
                val location = GLES30.glGetUniformLocation(programId, name)
                if (location != -1) {
                    GLES30.glUniform3f(location, uniform.value.x, uniform.value.y, uniform.value.z)
                }
            }

            is Uniform.Vec4f -> {
                val location = GLES30.glGetUniformLocation(programId, name)
                if (location != -1) {
                    GLES30.glUniform4f(
                        location,
                        uniform.value.x,
                        uniform.value.y,
                        uniform.value.z,
                        uniform.value.w
                    )
                }
            }

            is Uniform.Mat4 -> {
                val location = GLES30.glGetUniformLocation(programId, name)
                if (location != -1) {
                    GLES30.glUniformMatrix4fv(location, 1, uniform.transpose, uniform.value.data, 0)
                }
            }
        }
    }

    fun useTexture(
        program: ShaderProgramSource,
        name: String,
        texture: Texture,
        index: Int,
        directIndex: Boolean
    ) {
        this.updateTextureBuffer(texture)

        val programId = getProgram(program) ?: return
        val location = GLES30.glGetUniformLocation(programId, name)
        val internalIndex = if (directIndex) index else index + 2
        if (location != -1) {
            val textureId = getTextureBuffer(texture) ?: createTextureBuffer(texture)
                .getOrThrow()
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + internalIndex)
            val target = when (texture) {
                is Texture.Texture2D -> GLES30.GL_TEXTURE_2D
                is Texture.TextureCube -> GLES30.GL_TEXTURE_CUBE_MAP
                is Texture.DataTexture -> GLES30.GL_TEXTURE_2D
            }
            GLES30.glBindTexture(target, textureId)
            GLES30.glUniform1i(location, internalIndex)
        }
    }

    private val _ambientLights = arrayListOf<AmbientLight>()
    private val _directionalLights = arrayListOf<DirectionalLight>()
    private val _pointLights = arrayListOf<PointLight>()
    private val _spotLights = arrayListOf<SpotLight>()

    fun useLights(program: ShaderProgramSource, scene: Scene) {
        if (scene.lights.isEmpty()) return
        val programId = getProgram(program) ?: return

        scene.lights.filterIsInstanceTo(_ambientLights)
        scene.lights.filterIsInstanceTo(_directionalLights)
        scene.lights.filterIsInstanceTo(_pointLights)
        scene.lights.filterIsInstanceTo(_spotLights)

        // ambient light
        if (_ambientLights.isNotEmpty()) {
            val theLight = _ambientLights[0]
            uniformLocationOf(programId, "ambientLight.position") {
                GLES30.glUniform3f(
                    it,
                    theLight.position.x,
                    theLight.position.y,
                    theLight.position.z
                )
            }
            uniformLocationOf(programId, "ambientLight.color") {
                GLES30.glUniform3f(it, theLight.color.r, theLight.color.g, theLight.color.b)
            }
            uniformLocationOf(programId, "ambientLight.intensity") {
                GLES30.glUniform1f(it, theLight.intensity)
            }
        } else {
            uniformLocationOf(programId, "ambientLight.intensity") {
                GLES30.glUniform1f(it, 0f)
            }
        }

        // directional light
        if (_directionalLights.isNotEmpty()) {
            val theLight = _directionalLights[0]
            uniformLocationOf(programId, "directionalLight.position") {
                GLES30.glUniform3f(
                    it,
                    theLight.position.x,
                    theLight.position.y,
                    theLight.position.z
                )
            }
            uniformLocationOf(programId, "directionalLight.target") {
                GLES30.glUniform3f(it, theLight.target.x, theLight.target.y, theLight.target.z)
            }
            uniformLocationOf(programId, "directionalLight.color") {
                GLES30.glUniform3f(it, theLight.color.r, theLight.color.g, theLight.color.b)
            }
            uniformLocationOf(programId, "directionalLight.intensity") {
                GLES30.glUniform1f(it, theLight.intensity)
            }
        } else {
            uniformLocationOf(programId, "directionalLight.intensity") {
                GLES30.glUniform1f(it, 0f)
            }
        }

        // point light
        uniformLocationOf(programId, "pointLightCount") {
            GLES30.glUniform1i(it, _pointLights.size)
        }
        if (_pointLights.isNotEmpty()) {
            _pointLights.forEachIndexed { index, t ->
                require(index < 4) { "Point light count must be less than 4" }

                uniformLocationOf(programId, "pointLight[$index].position") {
                    GLES30.glUniform3f(
                        it,
                        t.position.x,
                        t.position.y,
                        t.position.z
                    )
                }
                uniformLocationOf(programId, "pointLight[$index].color") {
                    GLES30.glUniform3f(it, t.color.r, t.color.g, t.color.b)
                }
                uniformLocationOf(programId, "pointLight[$index].intensity") {
                    GLES30.glUniform1f(it, t.intensity)
                }
                uniformLocationOf(programId, "pointLight[$index].constant") {
                    GLES30.glUniform1f(it, t.constant)
                }
                uniformLocationOf(programId, "pointLight[$index].linear") {
                    GLES30.glUniform1f(it, t.linear)
                }
                uniformLocationOf(programId, "pointLight[$index].quadratic") {
                    GLES30.glUniform1f(it, t.quadratic)
                }
            }
        }

        // spot light
        uniformLocationOf(programId, "spotLightCount") {
            GLES30.glUniform1i(it, _spotLights.size)
        }
        if (_spotLights.isNotEmpty()) {
            _spotLights.forEachIndexed { index, t ->
                require(index < 4) { "Spot light count must be less than 4" }

                uniformLocationOf(programId, "spotLight[$index].position") {
                    GLES30.glUniform3f(
                        it,
                        t.position.x,
                        t.position.y,
                        t.position.z
                    )
                }
                uniformLocationOf(programId, "spotLight[$index].target") {
                    GLES30.glUniform3f(it, t.target.x, t.target.y, t.target.z)
                }
                uniformLocationOf(programId, "spotLight[$index].color") {
                    GLES30.glUniform3f(it, t.color.r, t.color.g, t.color.b)
                }
                uniformLocationOf(programId, "spotLight[$index].intensity") {
                    GLES30.glUniform1f(it, t.intensity)
                }
                uniformLocationOf(programId, "spotLight[$index].angle") {
                    GLES30.glUniform1f(it, t.angle)
                }
                uniformLocationOf(programId, "spotLight[$index].penumbra") {
                    GLES30.glUniform1f(it, t.penumbra)
                }
            }
        }

        _ambientLights.clear()
        _directionalLights.clear()
        _pointLights.clear()
        _spotLights.clear()
    }

    fun useSkinBones(shaderProgramSource: ShaderProgramSource, actor: SkinMesh) {
        val programId = getProgram(shaderProgramSource) ?: return
        val skeleton = actor.skeleton

        // RGBA is a vec4, so we need a RGBA(pixel) * 4 for each bone(4x4 matrix)
        val bitMapSize = ceilPowerOf2( // ceilPowerOf2: make sure the size is power of 2
            ceil(sqrt(skeleton.bones.size * 4.0)).toInt()
        ).coerceAtLeast(4)

        if (!boneTextures.containsKey(skeleton)) {
            val boneMatrices = FloatArray(bitMapSize * bitMapSize * 4)
            skeleton.bones.forEachIndexed { index, bone ->
                val matrix = (bone.node.worldMatrix * bone.inverseBindMatrix).transpose()
                matrix.data.forEachIndexed { i, v ->
                    boneMatrices[index * 16 + i] = v
                }
            }
            val buffer = ByteBuffer.allocateDirect(boneMatrices.size * 4).apply {
                order(java.nio.ByteOrder.nativeOrder())
                asFloatBuffer().put(boneMatrices)
            }
            boneTextures[skeleton] = Texture.DataTexture(
                buffer,
                bitMapSize,
                bitMapSize,
                TextureWrap.CLAMP_TO_EDGE,
                TextureWrap.CLAMP_TO_EDGE,
                TextureFilter.LINEAR,
                TextureFilter.LINEAR
            )
            println("[K3D:Resource] create bone texture: $bitMapSize x $bitMapSize (${skeleton.bones.size} bones)")
        }

        val texture = boneTextures[skeleton] ?: return
        if(skeleton.bones.any { it.node.currentFrameDirty }) {
            val buffer = texture.data.asFloatBuffer()
            val boneMatrices = FloatArray(skeleton.bones.size * 16)
            buffer.get(boneMatrices)
            skeleton.bones.forEachIndexed { index, bone ->
                val matrix = (bone.node.worldMatrix * bone.inverseBindMatrix).transpose()
                matrix.data.forEachIndexed { i, v ->
                    boneMatrices[index * 16 + i] = v
                }
            }
            buffer.rewind()
            buffer.put(boneMatrices)
            texture.markCurrentFrameDirty()
        }

        useTexture(
            shaderProgramSource,
            BuiltInUniformName.SKIN_JOINTS_MATRIX.uniformName,
            texture,
            1,
            true
        )
        uniformLocationOf(
            programId,
            BuiltInUniformName.SKIN_JOINTS_MATRIX_SIZE.uniformName
        ) { location ->
            GLES20.glUniform1i(location, bitMapSize)
        }
    }

    private inline fun uniformLocationOf(programId: Int, name: String, scope: (Int) -> Unit) {
        val location = GLES20.glGetUniformLocation(programId, name)
        if (location != -1) {
            scope(location)
        }
    }

    fun createProgram(program: ShaderProgramSource): Result<Int> = runCatching {
        require(!programs.containsKey(program)) { "Program already exists" }
        val programProcessResult = shaderProcessor.process(program)
        val vertexShader = createShader(GLES20.GL_VERTEX_SHADER, programProcessResult.vertexShader)
            .getOrThrow()
        val fragmentShader =
            createShader(GLES20.GL_FRAGMENT_SHADER, programProcessResult.fragmentShader)
                .getOrThrow()
        val programId = createProgram(vertexShader, fragmentShader)
            .getOrThrow()
        // println(programProcessResult.vertexShader)
        // println(programProcessResult.fragmentShader)
        // println("ID: $programId")
        programs[program] = programId
        programId
    }

    fun deleteProgram(program: ShaderProgramSource) {
        val programId = programs[program] ?: return
        GLES20.glDeleteProgram(programId)
        programs.remove(program)
    }

    fun getProgram(program: ShaderProgramSource): Int? = programs[program]

    fun getTextureBuffer(texture: Texture): Int? = textureBuffers[texture]

    fun createTextureBuffer(texture: Texture): Result<Int> = runCatching {
        require(!textureBuffers.containsKey(texture)) { "Texture already exists" }

        val textureId = genTexture().getOrThrow()
        textureBuffers[texture] = textureId

        println("[K3D:Resource] create texture buffer: $textureId")

        val target = when (texture) {
            is Texture.Texture2D -> GLES30.GL_TEXTURE_2D
            is Texture.TextureCube -> GLES30.GL_TEXTURE_CUBE_MAP
            is Texture.DataTexture -> GLES30.GL_TEXTURE_2D
        }
        GLES30.glBindTexture(target, textureId)

        when (texture) {
            is Texture.Texture2D -> {
                GLUtils.texImage2D(
                    target,
                    0,
                    GLES30.GL_RGBA,
                    texture.data,
                    GLES30.GL_UNSIGNED_BYTE,
                    0
                )
            }

            is Texture.TextureCube -> {
                GLUtils.texImage2D(
                    target,
                    0,
                    GLES30.GL_RGBA,
                    texture.data,
                    GLES30.GL_UNSIGNED_BYTE,
                    0
                )
            }

            is Texture.DataTexture -> {
                GLES20.glTexImage2D(
                    target,
                    0,
                    GLES30.GL_RGBA32F,
                    texture.width,
                    texture.height,
                    0,
                    GLES30.GL_RGBA,
                    GLES30.GL_FLOAT,
                    texture.data
                )
            }
        }

        if (texture !is Texture.DataTexture) GLES30.glGenerateMipmap(target)

        GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_MIN_FILTER, texture.minFilter.value)
        GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_MAG_FILTER, texture.magFilter.value)
        GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_WRAP_S, texture.wrapS.value)
        GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_WRAP_T, texture.wrapT.value)

        GLES30.glBindTexture(target, 0) // unbind texture

        textureId
    }

    fun updateTextureBuffer(texture: Texture) {
        if (!texture.currentFrameDirty) return

        println("[K3D:Resource] update texture buffer: $texture")

        val textureId = textureBuffers[texture] ?: return
        val target = when (texture) {
            is Texture.Texture2D -> GLES30.GL_TEXTURE_2D
            is Texture.TextureCube -> GLES30.GL_TEXTURE_CUBE_MAP
            is Texture.DataTexture -> GLES30.GL_TEXTURE_2D
        }

        GLES30.glBindTexture(target, textureId)
        when (texture) {
            is Texture.Texture2D -> {
                GLUtils.texSubImage2D(
                    target,
                    0,
                    0,
                    0,
                    texture.data,
                    GLES30.GL_RGBA,
                    GLES30.GL_UNSIGNED_BYTE
                )
                GLES20.glGenerateMipmap(target)
            }

            is Texture.TextureCube -> {
                GLUtils.texSubImage2D(
                    target,
                    0,
                    0,
                    0,
                    texture.data,
                    GLES30.GL_RGBA,
                    GLES30.GL_UNSIGNED_BYTE
                )
                GLES20.glGenerateMipmap(target)
            }

            is Texture.DataTexture -> {
                GLES20.glTexSubImage2D(
                    target,
                    0,
                    0,
                    0,
                    texture.width,
                    texture.height,
                    GLES30.GL_RGBA,
                    GLES30.GL_FLOAT,
                    texture.data.rewind()
                )
            }
        }

        GLES30.glBindTexture(target, 0) // unbind texture
    }

    fun deleteTextureBuffer(texture: Texture) {
        val textureId = textureBuffers[texture] ?: return
        GLES30.glDeleteTextures(1, intArrayOf(textureId), 0)
        textureBuffers.remove(texture)
    }

    fun createVertexArray(program: ShaderProgramSource, vertexArray: VertexArray): Result<Int> =
        runCatching {
            require(!vertexArrays.containsKey(vertexArray)) { "VertexArray already exists" }

            val programId = programs[program] ?: throw IllegalStateException("Program not found")
            val vao = genVertexArray().getOrThrow()
            vertexArrays[vertexArray] = vao

            GLES30.glBindVertexArray(vao)
            vertexArray.getAttributes().forEach { (name, attribute) ->
                // Set attribute
                val location = GLES30.glGetAttribLocation(programId, name)
                if (location != -1) {
                    // Create buffer for attribute
                    val vbo = genBuffer().getOrThrow()
                    vertexArraysAttributesBuffer[attribute] = vbo
                    GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo)
                    attribute.data.rewind()
                    val size = attribute.data.sizeInBytes()
                    println("[K3D:Resource] stream attribute buffer: ${attribute.data} / size: $size")
                    GLES30.glBufferData(
                        GLES30.GL_ARRAY_BUFFER,
                        size,
                        attribute.data,
                        GLES30.GL_STATIC_DRAW
                    )

                    GLES30.glEnableVertexAttribArray(location)
                    GLES30.glVertexAttribPointer(
                        location,
                        attribute.itemSize,
                        attribute.type.value,
                        attribute.normalized,
                        0,
                        0
                    )
                } else {
                    println("location not found for $name")
                }
            }
            // set indices if exists
            vertexArray.getIndices()?.let { indices ->
                val buffer = genBuffer().getOrThrow()
                println("[K3D:Resource] stream indices buffer: $indices")
                vertexArraysIndicesBuffer[vertexArray] = buffer
                GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, buffer)
                indices.data.rewind()
                GLES30.glBufferData(
                    GLES30.GL_ELEMENT_ARRAY_BUFFER,
                    indices.data.sizeInBytes(),
                    indices.data,
                    GLES30.GL_STATIC_DRAW
                )
            }
            GLES30.glBindVertexArray(0)
            vao
        }

    fun updateVertexArray(vertexArray: VertexArray) {
        val vao = vertexArrays[vertexArray] ?: return
        GLES30.glBindVertexArray(vao)
        vertexArray.getAttributes().forEach { (_, attribute) ->
            attribute.takeIf { it.currentFrameDirty }?.let { // update attribute buffer if dirty
                println("[K3D:Resource] update attribute buffer: $attribute")
                val vbo = vertexArraysAttributesBuffer[attribute] ?: return@forEach
                GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo)
                attribute.data.rewind()
                GLES30.glBufferSubData(
                    GLES30.GL_ARRAY_BUFFER,
                    0,
                    attribute.data.sizeInBytes(),
                    attribute.data,
                )
            }
        }
        GLES30.glBindVertexArray(0)
    }

    fun deleteVertexArray(vertexArray: VertexArray) {
        val vao = vertexArrays[vertexArray] ?: return
        GLES30.glDeleteVertexArrays(1, intArrayOf(vao), 0)
        vertexArrays.remove(vertexArray)

        vertexArray.getAttributes().forEach { (_, attribute) ->
            val vbo = vertexArraysAttributesBuffer[attribute] ?: return@forEach
            GLES30.glDeleteBuffers(1, intArrayOf(vbo), 0)
            vertexArraysAttributesBuffer.remove(attribute)
        }

        vertexArraysIndicesBuffer[vertexArray]?.let { buffer ->
            GLES30.glDeleteBuffers(1, intArrayOf(buffer), 0)
            vertexArraysIndicesBuffer.remove(vertexArray)
        }
    }

    fun getVertexArray(vertexArray: VertexArray): Int? = vertexArrays[vertexArray]

    override fun dispose() {
        // program
        programs.values.forEach {
            GLES20.glDeleteProgram(it)
        }
        programs.clear()

        // vao
        vertexArrays.values.forEach {
            GLES30.glDeleteVertexArrays(1, intArrayOf(it), 0)
        }
        vertexArrays.clear()
        vertexArraysAttributesBuffer.values.forEach {
            GLES30.glDeleteBuffers(1, intArrayOf(it), 0)
        }
        vertexArraysAttributesBuffer.clear()
        vertexArraysIndicesBuffer.values.forEach {
            GLES30.glDeleteBuffers(1, intArrayOf(it), 0)
        }
        vertexArraysIndicesBuffer.clear()

        // texture
        textureBuffers.values.forEach {
            GLES30.glDeleteTextures(1, intArrayOf(it), 0)
        }
        textureBuffers.clear()
    }
}

private fun ByteBuffer.sizeInBytes(): Int {
    return this.remaining()
}