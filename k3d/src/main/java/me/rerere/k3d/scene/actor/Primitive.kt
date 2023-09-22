package me.rerere.k3d.scene.actor

import me.rerere.k3d.renderer.resource.DrawMode
import me.rerere.k3d.scene.geometry.BufferGeometry
import me.rerere.k3d.scene.material.ShaderMaterial

/**
 * A primitive is a renderable object, which contains a geometry and a material
 *
 * If you want to define a new primitive, you can extend this class, like:
 * ```
 * // define a Mesh, a mesh is a object with triangle faces
 * class MyMesh(geometry: BufferGeometry, material: ShaderMaterial) : Primitive(geometry, material)
 *
 * // define a Line, a line is a object with line segments
 * class MyLine(geometry: BufferGeometry, material: ShaderMaterial) : Primitive(geometry, material, DrawMode.LINES)
 * ```
 *
 * @property geometry The geometry of this primitive
 * @property material The material of this primitive
 * @property mode The draw mode of this primitive (default: TRIANGLES)
 * @property count The draw count of this primitive
 * @constructor Create empty Primitive
 */
abstract class Primitive(
    val geometry: BufferGeometry,
    val material: ShaderMaterial,
    val mode: DrawMode = DrawMode.TRIANGLES,
    val count: Int = 0,
) : Actor()