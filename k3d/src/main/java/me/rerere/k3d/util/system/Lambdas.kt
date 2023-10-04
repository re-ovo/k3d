package me.rerere.k3d.util.system

// Type-specialized FloatMapper to prevent boxing caused by generics
// Equal to (value: Float) -> Float
fun interface FloatUnaryMapper {
    fun map(value: Float): Float
}

// Type-specialized FloatMapper to prevent boxing caused by generics
// Equal to (a: Float, b: Float) -> Float
fun interface FloatBinaryMapper {
    fun map(a: Float, b: Float): Float
}