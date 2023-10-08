package me.rerere.k3d.scene.actor

import me.rerere.k3d.renderer.resource.DrawMode
import me.rerere.k3d.scene.geometry.BufferGeometry
import me.rerere.k3d.scene.material.ShaderMaterial

/**
 * A line primitive
 *
 * It draws a line from the first vertex to the second vertex, from the second vertex to the third
 * vertex, and so on. For example, if the vertices are [A, B, C, D], it will draw a line from A to B,
 * from B to C, and from C to D.
 *
 * @param geometry The geometry of this line
 * @param material The material of this line
 */
class Line(
    geometry: BufferGeometry,
    material: ShaderMaterial,
) : Primitive(geometry, material, DrawMode.LINE_STRIP)