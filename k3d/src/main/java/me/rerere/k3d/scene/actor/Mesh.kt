package me.rerere.k3d.scene.actor

import me.rerere.k3d.renderer.resource.DrawMode
import me.rerere.k3d.renderer.shader.BuiltInMarcoDefinition
import me.rerere.k3d.scene.geometry.BufferGeometry
import me.rerere.k3d.scene.material.ShaderMaterial
import me.rerere.k3d.util.math.Matrix4
import me.rerere.k3d.util.system.Dirty
import me.rerere.k3d.util.system.Disposable
import me.rerere.k3d.util.system.alsoDispose
import me.rerere.k3d.util.system.fastForeach

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
) : Primitive(geometry, material, DrawMode.TRIANGLES)

class SkinMesh(
    geometry: BufferGeometry,
    material: ShaderMaterial,
    val skeleton: Skeleton,
) : Mesh(geometry, material) {
    init {
        material.program.addMarcoDefinition(BuiltInMarcoDefinition.USE_SKIN.marcoDefinition)

        alsoDispose(skeleton)
    }
}

class Skeleton(val bones: List<Bone>): Dirty, Disposable {
    class Bone(
        val node: Actor,
        val inverseBindMatrix: Matrix4
    )

    override fun isDirty(): Boolean {
        bones.fastForeach {
            if(it.node.isDirty()) return true
        }
        return false
    }

    override fun markDirtyNew() {
        error("Skeleton is not allowed to mark dirty")
    }

    override fun updateDirty() {}

    override fun clearDirty() {}

    override fun dispose() {}
}