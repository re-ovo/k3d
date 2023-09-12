package me.rerere.k3d.renderer

import android.opengl.GLES20
import android.opengl.GLES30
import me.rerere.k3d.renderer.resource.Attribute
import me.rerere.k3d.renderer.resource.DataType
import me.rerere.k3d.renderer.resource.VertexArray
import me.rerere.k3d.renderer.shader.ShaderProgram
import me.rerere.k3d.renderer.shader.createProgram
import me.rerere.k3d.renderer.shader.createShader
import me.rerere.k3d.renderer.shader.genBuffer
import me.rerere.k3d.renderer.shader.genVertexArray
import me.rerere.k3d.scene.Scene
import me.rerere.k3d.scene.camera.Camera
import me.rerere.k3d.util.Disposable
import me.rerere.k3d.util.cleanIfDirty
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.util.IdentityHashMap

private val PROGRAM = ShaderProgram(
    vertexShader = """
        #version 300 es
        
        in vec3 aPos;
        out vec4 vertexColor;
        
        void main() {
            gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);
            vertexColor = vec4(clamp(aPos, 0.0, 1.0), 1.0);
        }
    """.trimIndent(),
    fragmentShader = """
        #version 300 es
        precision mediump float;
        in vec4 vertexColor;
        out vec4 FragColor;
        void main() {
            FragColor = vertexColor;
        }
    """.trimIndent()
)

private val vao = VertexArray().apply {
    setIndices(listOf(0, 1, 2, 0, 2, 3))
    setAttribute(
        Attribute(
            name = "aPos",
            itemSize = 3,
            type = DataType.FLOAT,
            normalized = false,
            data = FloatBuffer.wrap(floatArrayOf( // rectangle
                -0.5f, 0.5f, 0f,
                0.5f, 0.5f, 0f,
                0.5f, -0.5f, 0f,
                -0.5f, -0.5f, 0f
            ))
        )
    )
}

class GL3Renderer : Renderer {
    private val resourceManager = GL3ResourceManager()
    override var viewportSize: ViewportSize = ViewportSize(0, 0)

    override fun dispose() {
        this.resourceManager.dispose()
    }

    override fun resize(width: Int, height: Int) {
        viewportSize = ViewportSize(width, height)
    }

    override fun render(scene: Scene, camera: Camera) {
        GLES30.glClearColor(0f, 0f, 0f, 1f)
        GLES30.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES30.glViewport(0, 0, viewportSize.width, viewportSize.height)

        resourceManager.useProgram(PROGRAM) {
            resourceManager.useVertexArray(this, vao) {
                GLES30.glDrawElements(GLES30.GL_TRIANGLES, 6, GLES30.GL_UNSIGNED_INT, 0)
            }
        }
    }
}

internal class GL3ResourceManager : Disposable {
    private val programs = IdentityHashMap<ShaderProgram, Int>()
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

    fun createVertexArray(program: ShaderProgram, vertexArray: VertexArray): Result<Int> = runCatching {
        require(!vertexArrays.containsKey(vertexArray)) { "VertexArray already exists" }

        val programId = programs[program] ?: throw IllegalStateException("Program not found")
        val vao = genVertexArray().getOrThrow()
        vertexArrays[vertexArray] = vao


        GLES30.glBindVertexArray(vao)
        vertexArray.getAttributes().forEach { attribute ->
            // Create buffer for attribute
            val vbo = genBuffer().getOrThrow()
            vertexArraysAttributesBuffer[attribute] = vbo
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo)
            GLES30.glBufferData(
                GLES30.GL_ARRAY_BUFFER,
                attribute.data.capacity() * attribute.type.size,
                attribute.data,
                GLES30.GL_STATIC_DRAW
            )

            // Set attribute
            val location = GLES30.glGetAttribLocation(programId, attribute.name)
            if (location != -1) {
                GLES30.glEnableVertexAttribArray(location)
                GLES30.glVertexAttribPointer(
                    location,
                    attribute.itemSize,
                    attribute.type.value,
                    attribute.normalized,
                    0,
                    0
                )
                println("location: $location")
            } else {
                println("location not found for ${attribute.name}")
            }
        }
        // set indices if exists
        vertexArray.getIndices()?.let { indices ->
            val buffer = genBuffer().getOrThrow()
            vertexArraysIndicesBuffer[vertexArray] = buffer
            GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, buffer)
            GLES30.glBufferData(
                GLES30.GL_ELEMENT_ARRAY_BUFFER,
                indices.size * 4,
                IntBuffer.wrap(indices.toIntArray()),
                GLES30.GL_STATIC_DRAW
            )
            println("indices buffer: $buffer")
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
                GLES30.glBufferData(
                    GLES30.GL_ARRAY_BUFFER,
                    attribute.data.capacity() * attribute.type.size,
                    attribute.data,
                    GLES30.GL_STATIC_DRAW
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