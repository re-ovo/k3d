package me.rerere.k3d.renderer.resource

import java.nio.Buffer

class Attribute(
    val name: String,
    val size: Int,
    val normalized: Boolean,
    val data: Buffer
)