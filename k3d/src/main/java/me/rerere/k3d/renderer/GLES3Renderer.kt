package me.rerere.k3d.renderer

import android.opengl.GLES20
import android.opengl.GLES30
import me.rerere.k3d.renderer.resource.Attribute
import me.rerere.k3d.renderer.resource.VertexArray
import me.rerere.k3d.renderer.shader.ShaderProgram
import me.rerere.k3d.renderer.shader.Uniform
import me.rerere.k3d.renderer.shader.createProgram
import me.rerere.k3d.renderer.shader.createShader
import me.rerere.k3d.renderer.shader.genBuffer
import me.rerere.k3d.renderer.shader.genVertexArray
import me.rerere.k3d.scene.Scene
import me.rerere.k3d.scene.actor.Primitive
import me.rerere.k3d.scene.camera.Camera
import me.rerere.k3d.scene.material.ShaderMaterial
import me.rerere.k3d.scene.traverse
import me.rerere.k3d.util.Disposable
import me.rerere.k3d.util.cleanIfDirty
import java.nio.Buffer
import java.util.IdentityHashMap

class GLES3Renderer : Renderer {
    private val resourceManager = GL3ResourceManager()
    override var viewportSize: ViewportSize = ViewportSize(0, 0)

    override fun dispose() {
        this.resourceManager.dispose()
    }

    override fun resize(width: Int, height: Int) {
        viewportSize = ViewportSize(width, height)
    }

    private val worldMatrixUniform = Uniform.Mat4("u_worldMatrix", FloatArray(16), true)
    private val viewMatrixUniform = Uniform.Mat4("u_viewMatrix", FloatArray(16), true)
    private val projectionMatrixUniform = Uniform.Mat4("u_projectionMatrix", FloatArray(16), true)

    override fun render(scene: Scene, camera: Camera) {
        GLES30.glClearColor(0f, 0f, 0f, 1f)
        GLES30.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES30.glViewport(0, 0, viewportSize.width, viewportSize.height)

        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glEnable(GLES30.GL_CULL_FACE)

        if (camera.dirty) {
            camera.updateMatrix()
            camera.markClean()
        }

        scene.traverse { actor ->
            if (actor.position.dirty || actor.rotation.dirty || actor.scale.dirty) {
                actor.updateMatrix()
                actor.position.markClean()
                actor.rotation.markClean()
                actor.scale.markClean()
            }

            if (actor is Primitive) {
                // println("render $actor")

                resourceManager.useProgram(actor.material.program) {
                    if (actor.material is ShaderMaterial) {
                        actor.material.uniforms.forEach { uniform ->
                            resourceManager.useUniform(actor.material.program, uniform)
                        }

                        // Apply built-in uniforms
                        resourceManager.useUniform(
                            actor.material.program,
                            worldMatrixUniform.apply {
                                value = actor.worldMatrix.data
                            }
                        )
                        resourceManager.useUniform(
                            actor.material.program,
                            viewMatrixUniform.apply {
                                value = camera.worldMatrixInverse.data
                                // println(camera.worldMatrixInverse.toString())
                            }
                        )
                        resourceManager.useUniform(
                            actor.material.program,
                            projectionMatrixUniform.apply {
                                value = camera.projectionMatrix.data
                            }
                        )
                    } else {
                        // Raw shader material
                        actor.material.uniforms.forEach { uniform ->
                            resourceManager.useUniform(actor.material.program, uniform)
                        }
                    }

                    resourceManager.useVertexArray(this, actor.geometry.vao) {
                        if(actor.geometry.getIndices() == null) {
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

internal class GL3ResourceManager : Disposable {
    // program(shaders) related resources
    private val programs = IdentityHashMap<ShaderProgram, Int>()

    // vao related resources
    private val vertexArrays = IdentityHashMap<VertexArray, Int>()
    private val vertexArraysAttributesBuffer = IdentityHashMap<Attribute, Int>()
    private val vertexArraysIndicesBuffer = IdentityHashMap<VertexArray, Int>()

    inline fun useProgram(program: ShaderProgram, scope: ShaderProgram.() -> Unit) {
        val programId = this.getProgram(program) ?: this.createProgram(program)
            .getOrThrow()
        GLES30.glUseProgram(programId)
        scope(program)
        GLES30.glUseProgram(0)
    }

    inline fun useVertexArray(program: ShaderProgram, vertexArray: VertexArray, scope: () -> Unit) {
        val vao = getVertexArray(vertexArray) ?: createVertexArray(program, vertexArray)
            .getOrThrow()

        updateVertexArray(vertexArray)

        GLES30.glBindVertexArray(vao)
        scope()
        GLES30.glBindVertexArray(0)
    }

    fun useUniform(program: ShaderProgram, uniform: Uniform) {
        val programId = getProgram(program) ?: return
        when (uniform) {
            is Uniform.Float1 -> {
                val location = GLES30.glGetUniformLocation(programId, uniform.name)
                if (location != -1) {
                    GLES30.glUniform1f(location, uniform.value)
                }
            }

            is Uniform.Int1 -> {
                val location = GLES30.glGetUniformLocation(programId, uniform.name)
                if (location != -1) {
                    GLES30.glUniform1i(location, uniform.value)
                }
            }

            is Uniform.Vec3f -> {
                val location = GLES30.glGetUniformLocation(programId, uniform.name)
                if (location != -1) {
                    GLES30.glUniform3f(location, uniform.x, uniform.y, uniform.z)
                }
            }

            is Uniform.Vec4f -> {
                val location = GLES30.glGetUniformLocation(programId, uniform.name)
                if (location != -1) {
                    GLES30.glUniform4f(location, uniform.x, uniform.y, uniform.z, uniform.w)
                }
            }

            is Uniform.Mat4 -> {
                val location = GLES30.glGetUniformLocation(programId, uniform.name)
                if (location != -1) {
                    GLES30.glUniformMatrix4fv(location, 1, uniform.transpose, uniform.value, 0)
                }
            }
        }
    }

    fun createProgram(program: ShaderProgram): Result<Int> = runCatching {
        require(!programs.containsKey(program)) { "Program already exists" }
        val vertexShader = createShader(GLES20.GL_VERTEX_SHADER, program.vertexShader)
            .getOrThrow()
        val fragmentShader = createShader(GLES20.GL_FRAGMENT_SHADER, program.fragmentShader)
            .getOrThrow()
        val programId = createProgram(vertexShader, fragmentShader)
            .getOrThrow()
        programs[program] = programId
        programId
    }

    fun deleteProgram(program: ShaderProgram) {
        val programId = programs[program] ?: return
        GLES20.glDeleteProgram(programId)
        programs.remove(program)
    }

    fun getProgram(program: ShaderProgram): Int? = programs[program]

    fun createVertexArray(program: ShaderProgram, vertexArray: VertexArray): Result<Int> =
        runCatching {
            require(!vertexArrays.containsKey(vertexArray)) { "VertexArray already exists" }

            val programId = programs[program] ?: throw IllegalStateException("Program not found")
            val vao = genVertexArray().getOrThrow()
            vertexArrays[vertexArray] = vao

            GLES30.glBindVertexArray(vao)
            vertexArray.getAttributes().forEach { attribute ->
                // Set attribute
                val location = GLES30.glGetAttribLocation(programId, attribute.name)
                if (location != -1) {
                    // Create buffer for attribute
                    val vbo = genBuffer().getOrThrow()
                    vertexArraysAttributesBuffer[attribute] = vbo
                    GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo)
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
                    println("location not found for ${attribute.name}")
                }
            }
            // set indices if exists
            vertexArray.getIndices()?.let { indices ->
                val buffer = genBuffer().getOrThrow()
                println("[K3D:Resource] stream indices buffer: $indices")
                vertexArraysIndicesBuffer[vertexArray] = buffer
                GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, buffer)
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
        vertexArray.getAttributes().forEach { attribute ->
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

        vertexArray.getAttributes().forEach { attribute ->
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
        programs.values.forEach {
            GLES20.glDeleteProgram(it)
        }
        programs.clear()

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