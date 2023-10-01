package me.rerere.k3d.scene.actor

import me.rerere.k3d.renderer.resource.DrawMode
import me.rerere.k3d.renderer.shader.BuiltInAttributeName
import me.rerere.k3d.renderer.shader.BuiltInMarcoDefinition
import me.rerere.k3d.scene.geometry.BufferGeometry
import me.rerere.k3d.scene.material.ShaderMaterial
import me.rerere.k3d.util.math.Matrix4

/**
 * A mesh is a primitive represents a polygon mesh.
 *
 * @property geometry The geometry of this mesh
 * @property material The material of this mesh
 * @property mode The draw mode of this mesh
 * @property count The count of this mesh
 */
open class Mesh(
    geometry: BufferGeometry,
    material: ShaderMaterial,
    count: Int = 0
) : Primitive(geometry, material, DrawMode.TRIANGLES, count)

class SkinMesh(
    geometry: BufferGeometry,
    material: ShaderMaterial,
    val skeleton: Skeleton,
    count: Int = 0
) : Mesh(geometry, material, count) {
    init {
        material.program.addMarcoDefinition(BuiltInMarcoDefinition.USE_SKIN.marcoDefinition)
    }
}

class Skeleton(val bones: List<Bone>) {
    class Bone(
        val node: Actor,
        val inverseBindMatrix: Matrix4
    )
}