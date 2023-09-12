package me.rerere.k3d.renderer

import android.opengl.GLES20
import me.rerere.k3d.renderer.shader.ShaderProgram
import me.rerere.k3d.renderer.shader.createProgram
import me.rerere.k3d.renderer.shader.createShader
import me.rerere.k3d.scene.Scene
import me.rerere.k3d.scene.camera.Camera
import java.util.IdentityHashMap

class GL3Renderer : Renderer {
    private val resourceManager = GL3ResourceManager()
    override var viewportSize: ViewportSize = ViewportSize(0, 0)

    override fun resize(width: Int, height: Int) {
        viewportSize = ViewportSize(width, height)
    }

    override fun render(scene: Scene, camera: Camera) {

    }
}

class GL3ResourceManager {
    private val programs = IdentityHashMap<ShaderProgram, Int>()

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
}