package me.rerere.k3d.helper

import me.rerere.k3d.renderer.resource.DrawMode
import me.rerere.k3d.scene.actor.Primitive
import me.rerere.k3d.scene.geometry.BufferGeometry
import me.rerere.k3d.scene.material.ShaderMaterial

// TODO: GridHelper
class GridHelper(
    geometry: BufferGeometry,
    material: ShaderMaterial,
    mode: DrawMode = DrawMode.TRIANGLES,
    count: Int = 0
) : Primitive(geometry, material, mode, count)