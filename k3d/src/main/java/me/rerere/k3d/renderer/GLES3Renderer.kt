package me.rerere.k3d.renderer

import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLUtils
import me.rerere.k3d.renderer.resource.Attribute
import me.rerere.k3d.renderer.resource.Texture
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
import me.rerere.k3d.scene.actor.traverse
import me.rerere.k3d.scene.camera.Camera
import me.rerere.k3d.scene.light.AmbientLight
import me.rerere.k3d.scene.light.DirectionalLight
import me.rerere.k3d.scene.light.PointLight
import me.rerere.k3d.scene.light.SpotLight
import me.rerere.k3d.util.Disposable
import me.rerere.k3d.util.cleanIfDirty
import me.rerere.k3d.util.math.Matrix4
import me.rerere.k3d.util.math.Vec3
import java.nio.Buffer
import java.util.IdentityHashMap

/**
 * GLES3 Renderer
 *
 * It is a renderer that uses OpenGL ES 3.0
 */
class GLES3Renderer : Renderer {
    private val shaderProcessor = ShaderProcessor()
    private val resourceManager = GL3ResourceManager(shaderProcessor)

    override var viewportSize: ViewportSize = ViewportSize(0, 0)

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
        GLES30.glClearColor(0f, 0f, 0f, 0f)
        GLES30.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES30.glViewport(0, 0, viewportSize.width, viewportSize.height)

        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glEnable(GLES30.GL_CULL_FACE)

        if (camera.dirty) {
            camera.updateMatrix()
            camera.markClean()
        }

        scene.traverse { actor ->
            if (actor.dirty) {
                actor.updateMatrix()
                actor.markClean()
            }

            if (actor is Primitive) {
                resourceManager.useProgram(actor.material.program) {
                    // Apply uniforms
                    actor.material.uniforms.forEach { (name, uniform) ->
                        resourceManager.useUniform(actor.material.program, uniform, name)
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
                            value.apply {
                                x = camera.position.x
                                y = camera.position.y
                                z = camera.position.z
                            }
                        },
                        BuiltInUniformName.CAMERA_POSITION.uniformName
                    )

                    // Apply Lights
                    resourceManager.useLights(this, scene)

                    // Apply textures
                    actor.material.textures.entries.forEachIndexed { index, mutableEntry ->
                        val (name, texture) = mutableEntry
                        resourceManager.useTexture(
                            actor.material.program,
                            name,
                            texture,
                            index
                        )
                    }

                    resourceManager.useVertexArray(this, actor.geometry.vao) {
                        if (actor.geometry.getIndices() == null) {
                            GLES30.glDrawArrays(
                                actor.mode.value,
                                0,
                                actor.count
                            )
                        } else {
                            GLES30.glDrawElements(
                                actor.mode.value,
                                actor.count,
                                GLES30.GL_UNSIGNED_INT,
                                0
                            )
                        }
                    }
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

    inline fun useProgram(program: ShaderProgramSource, scope: ShaderProgramSource.() -> Unit) {
        if(program.dirty) {
            deleteProgram(program)
            program.markClean()
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

    fun useTexture(program: ShaderProgramSource, name: String, texture: Texture, index: Int) {
        val programId = getProgram(program) ?: return
        val location = GLES30.glGetUniformLocation(programId, name)
        val internalIndex = index + 1 // 0 is reserved for default texture
        if (location != -1) {
            val textureId = getTextureBuffer(texture) ?: createTextureBuffer(texture)
                .getOrThrow()
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + internalIndex)
            val target = when (texture) {
                is Texture.Texture2D -> GLES30.GL_TEXTURE_2D
                is Texture.TextureCube -> GLES30.GL_TEXTURE_CUBE_MAP
            }
            GLES30.glBindTexture(target, textureId)
            GLES30.glUniform1i(location, internalIndex)
        }
    }

    fun useLights(program: ShaderProgramSource, scene: Scene) {
        if(scene.lights.isEmpty()) return
        val programId = getProgram(program) ?: return

        // ambient light
        val ambientLights = scene.lights.filterIsInstance<AmbientLight>()
        if(ambientLights.isNotEmpty()) {
            val theLight = ambientLights[0]
            uniformLocationOf(programId, "ambientLight.position") {
                GLES30.glUniform3f(it, theLight.position.x, theLight.position.y, theLight.position.z)
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
        val directionalLights = scene.lights.filterIsInstance<DirectionalLight>()
        if(directionalLights.isNotEmpty()) {
            val theLight = directionalLights[0]
            uniformLocationOf(programId, "directionalLight.position") {
                GLES30.glUniform3f(it, theLight.position.x, theLight.position.y, theLight.position.z)
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

        val pointLights = scene.lights.filterIsInstance<PointLight>()
        val spotLights = scene.lights.filterIsInstance<SpotLight>()
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
        // println(programProcessResult.vertexShader)
        // println(programProcessResult.fragmentShader)
        val vertexShader = createShader(GLES20.GL_VERTEX_SHADER, programProcessResult.vertexShader)
            .getOrThrow()
        val fragmentShader = createShader(GLES20.GL_FRAGMENT_SHADER, programProcessResult.fragmentShader)
            .getOrThrow()
        val programId = createProgram(vertexShader, fragmentShader)
            .getOrThrow()
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
        }
        GLES30.glBindTexture(target, textureId)
        GLUtils.texImage2D(
            target,
            0,
            GLES30.GL_RGBA,
            texture.data,
            GLES30.GL_UNSIGNED_BYTE,
           0
        )
        GLES30.glGenerateMipmap(target)
        GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_MIN_FILTER, texture.minFilter.value)
        GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_MAG_FILTER, texture.magFilter.value)
        GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_WRAP_S, texture.wrapS.value)
        GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_WRAP_T, texture.wrapT.value)
        GLES30.glBindTexture(target, 0)

        textureId
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
                        attribute.stride,
                        attribute.offset
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
                indices.rewind()
                GLES30.glBufferData(
                    GLES30.GL_ELEMENT_ARRAY_BUFFER,
                    indices.sizeInBytes(),
                    indices,
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
            attribute.cleanIfDirty { // update attribute buffer if dirty
                val vbo = vertexArraysAttributesBuffer[attribute] ?: return@forEach
                GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo)
                GLES30.glBufferSubData(
                    GLES30.GL_ARRAY_BUFFER,
                    0,
                    attribute.data.capacity() * attribute.type.size,
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

private fun Buffer.sizeInBytes(): Int {
    return this.remaining() * when (this) {
        is java.nio.ByteBuffer -> 1
        is java.nio.ShortBuffer -> 2
        is java.nio.IntBuffer -> 4
        is java.nio.FloatBuffer -> 4
        is java.nio.DoubleBuffer -> 8
        else -> throw IllegalStateException("Unknown buffer type")
    }
}