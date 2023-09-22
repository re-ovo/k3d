package me.rerere.k3d.scene.actor

import me.rerere.k3d.renderer.resource.DrawMode
import me.rerere.k3d.scene.geometry.BufferGeometry
import me.rerere.k3d.scene.material.ShaderMaterial

/**
 * A mesh is a primitive represents a polygon mesh.
 *
 * @property geometry The geometry of this mesh
 * @property material The material of this mesh
 * @property mode The draw mode of this mesh
 * @property count The count of this mesh
 */
class Mesh(
    geometry: BufferGeometry,
    material: ShaderMaterial,
    mode: DrawMode = DrawMode.TRIANGLES,
    count: Int = 0
) : Primitive(geometry, material, mode, count)