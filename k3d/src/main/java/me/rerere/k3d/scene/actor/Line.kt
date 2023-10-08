package me.rerere.k3d.scene.actor

import me.rerere.k3d.renderer.resource.DrawMode
import me.rerere.k3d.scene.geometry.BufferGeometry
import me.rerere.k3d.scene.material.ShaderMaterial

class Line(
    geometry: BufferGeometry,
    material: ShaderMaterial,
    mode: DrawMode = DrawMode.LINES
) : Primitive(geometry, material, mode)