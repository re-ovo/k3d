package me.rerere.k3d.scene.actor

import me.rerere.k3d.renderer.resource.DrawMode
import me.rerere.k3d.scene.geometry.BufferGeometry
import me.rerere.k3d.scene.material.ShaderMaterial

open class Primitive(
    val geometry: BufferGeometry,
    val material: ShaderMaterial,
    val mode: DrawMode = DrawMode.TRIANGLES,
    val count: Int = 0,
) : Actor()