package me.rerere.k3d.util.math.rotation

import kotlin.math.PI

/**
 * Convert degree to radian
 *
 * @receiver degree
 * @return radian
 */
fun Float.toDegree() = this * 180f / PI.toFloat()

/**
 * Convert radian to degree
 *
 * @receiver radian
 * @return degree
 */
fun Float.toRadian() = this * PI.toFloat() / 180f