package me.rerere.k3d.scene.actor

import me.rerere.k3d.renderer.resource.DrawMode
import me.rerere.k3d.scene.geometry.BufferGeometry
import me.rerere.k3d.scene.material.PointMaterial

class Point(
    geometry: BufferGeometry,
    material: PointMaterial,
) : Primitive(geometry, material, DrawMode.POINTS)