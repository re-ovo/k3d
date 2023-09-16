package me.rerere.k3d.scene.actor

import me.rerere.k3d.scene.Actor
import me.rerere.k3d.scene.geometry.BufferGeometry
import me.rerere.k3d.scene.material.RawShaderMaterial

class Mesh(
    val geometry: BufferGeometry,
    val material: RawShaderMaterial
) : Actor()