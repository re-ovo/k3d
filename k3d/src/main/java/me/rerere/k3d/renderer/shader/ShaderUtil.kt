package me.rerere.k3d.renderer.shader

import android.opengl.GLES20
import android.opengl.GLES30

/**
 * Create shader from source
 *
 * @param type shader type (GLES30.GL_VERTEX_SHADER or GLES30.GL_FRAGMENT_SHADER)
 * @param source shader source
 */
fun createShader(type: Int, source: String): Int {
    val shader = GLES20.glCreateShader(type)
    GLES30.glShaderSource(shader, source)
    GLES30.glCompileShader(shader)
    val status = IntArray(1)
    GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, status, 0)
    if (status[0] == 0) {
        val log = GLES30.glGetShaderInfoLog(shader)
        GLES30.glDeleteShader(shader)
        throw RuntimeException("Shader compile failed: $log")
    }
    return shader
}

/**
 * Create program from shader
 *
 * @param vertexShader vertex shader
 * @param fragmentShader fragment shader
 */
fun createProgram(vertexShader: Int, fragmentShader: Int): Int {
    val program = GLES20.glCreateProgram()
    GLES30.glAttachShader(program, vertexShader)
    GLES30.glAttachShader(program, fragmentShader)
    GLES30.glLinkProgram(program)
    val status = IntArray(1)
    GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, status, 0)
    if (status[0] == 0) {
        val log = GLES30.glGetProgramInfoLog(program)
        GLES30.glDeleteProgram(program)
        throw RuntimeException("Program link failed: $log")
    }
    return program
}