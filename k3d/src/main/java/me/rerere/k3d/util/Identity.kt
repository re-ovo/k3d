package me.rerere.k3d.util

import java.util.UUID

/**
 * Mark a object with a unique id
 *
 * Some object in k3d may have bound some opengl resources (like texture, geometry, etc.)
 * In order to dispose the resources when the object is no longer used, we need a unique id
 * to identify the object, and bind the resources to the object, so that we can dispose the
 * resources when the object is no longer used.
 *
 * You can use [UUID.randomUUID] to generate a unique id for your object, for example:
 * ```kotlin
 * class MyObject : Identity {
 *    override val id = UUID.randomUUID()
 *    // ...
 *    // some other code
 * }
 * ```
 *
 */
interface Identity {
    val id: UUID
}