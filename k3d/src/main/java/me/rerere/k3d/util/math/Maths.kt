package me.rerere.k3d.util.math

import kotlin.math.ceil

fun ceilPowerOf2(value: Int): Int {
    var v = value
    v--
    v = v or (v shr 1)
    v = v or (v shr 2)
    v = v or (v shr 4)
    v = v or (v shr 8)
    v = v or (v shr 16)
    v++
    return v
}

fun main() {
    println(ceilPowerOf2(ceil(8.1).toInt()))
}