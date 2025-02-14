package xyz.wagyourtail.konig.editor.helper

import imgui.ImVec2
import xyz.wagyourtail.commonskt.position.Pos2D
import kotlin.math.max

operator fun ImVec2.times(scale: Float): ImVec2 {
    return ImVec2(x * scale, y * scale)
}

operator fun ImVec2.div(scale: Float): ImVec2 {
    return ImVec2(x / scale, y / scale)
}

fun max(a: ImVec2, b: ImVec2): ImVec2 {
    return ImVec2(max(a.x, b.x), max(a.y, b.y))
}

operator fun ImVec2.unaryMinus(): ImVec2 {
    return ImVec2(-this.x, -this.y)
}

operator fun ImVec2.component1(): Float {
    return this.x
}

operator fun ImVec2.component2(): Float {
    return this.y
}

fun ImVec2.copy(): ImVec2 {
    return ImVec2(x, y)
}

fun ImVec2.toPos2D(): Pos2D {
    return Pos2D(x.toDouble(), y.toDouble())
}