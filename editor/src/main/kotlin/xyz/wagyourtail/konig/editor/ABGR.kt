package xyz.wagyourtail.konig.editor

import imgui.ImVec4

@JvmInline
value class ABGR(val col: Int) {

    constructor(a: Int, b: Int, g: Int, r: Int): this(((a shl 24) or (b shl 16) or (g shl 8) or (r)))

    constructor(col: Long): this(col.toInt())

    constructor(vec: ImVec4): this(vec.x, vec.y, vec.z, vec.w)

    constructor(a: Float, b: Float, g: Float, r: Float): this((a * 255).toInt(), (b * 255).toInt(), (g * 255).toInt(), (r * 255).toInt())

}