package xyz.wagyourtail.konig.editor.helper

import imgui.ImVec2
import kotlin.math.max
import kotlin.math.min

data class Vec2d(val x: Double, val y: Double) {

    constructor() : this(0.0, 0.0)

    constructor(x: Float, y: Float) : this(x.toDouble(), y.toDouble())

    constructor(vec: ImVec2): this(vec.x, vec.y)

    operator fun plus(other: Vec2d): Vec2d {
        return Vec2d(x + other.x, y + other.y)
    }

    operator fun minus(other: Vec2d): Vec2d {
        return Vec2d(x - other.x, y - other.y)
    }

    operator fun times(other: Vec2d): Vec2d {
        return Vec2d(x * other.x, y * other.y)
    }

    operator fun div(other: Vec2d): Vec2d {
        return Vec2d(x / other.x, y / other.y)
    }

    operator fun unaryMinus(): Vec2d {
        return Vec2d(-x, -y)
    }

    override fun toString(): String {
        return "($x, $y)"
    }

}

fun max(vec1: Vec2d, vec2: Vec2d): Vec2d {
    return Vec2d(max(vec1.x, vec2.x), max(vec1.y, vec2.y))
}

fun min(vec1: Vec2d, vec2: Vec2d): Vec2d {
    return Vec2d(min(vec1.x, vec2.x), min(vec1.y, vec2.y))
}
