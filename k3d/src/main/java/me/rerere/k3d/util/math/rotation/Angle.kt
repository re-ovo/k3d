package me.rerere.k3d.util.math.rotation

import kotlin.math.PI

fun Float.toDegree() = this * 180f / PI.toFloat()

fun Float.toRadian() = this * PI.toFloat() / 180f