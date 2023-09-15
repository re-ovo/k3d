package me.rerere.k3d.scene.actor

import me.rerere.k3d.scene.Actor
import me.rerere.k3d.scene.geometry.Geometry
import me.rerere.k3d.scene.material.RawShaderMaterial
import me.rerere.k3d.scene.material.ShaderMaterial

class Mesh(
    val geometry: Geometry,
    val material: RawShaderMaterial
) : Actor() {

}