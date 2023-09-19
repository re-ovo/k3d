package me.rerere.k3d.loader

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import me.rerere.k3d.util.Color4f
import me.rerere.k3d.util.math.Vec3

internal object Color4fAdapter : TypeAdapter<Color4f>() {
    override fun write(out: JsonWriter, value: Color4f?) {
        if (value == null) {
            out.nullValue()
            return
        } else {
            out.beginArray()
                .value(value.r)
                .value(value.g)
                .value(value.b)
                .value(value.a)
                .endArray()
        }
    }

    override fun read(`in`: JsonReader): Color4f? {
        if(`in`.peek() == JsonToken.NULL){
            `in`.nextNull()
            return null
        }
        `in`.beginArray()
        val r = `in`.nextDouble().toFloat()
        val g = `in`.nextDouble().toFloat()
        val b = `in`.nextDouble().toFloat()
        val a = `in`.nextDouble().toFloat()
        `in`.endArray()
        return Color4f(r, g, b, a)
    }
}

internal object Vec3fAdapter : TypeAdapter<Vec3>() {
    override fun write(out: JsonWriter, value: Vec3?) {
        if (value == null) {
            out.nullValue()
            return
        } else {
            out.beginArray()
                .value(value.x)
                .value(value.y)
                .value(value.z)
                .endArray()
        }
    }

    override fun read(`in`: JsonReader): Vec3? {
        if(`in`.peek() == JsonToken.NULL){
            `in`.nextNull()
            return null
        }
        `in`.beginArray()
        val x = `in`.nextDouble().toFloat()
        val y = `in`.nextDouble().toFloat()
        val z = `in`.nextDouble().toFloat()
        `in`.endArray()
        return Vec3(x, y, z)
    }
}