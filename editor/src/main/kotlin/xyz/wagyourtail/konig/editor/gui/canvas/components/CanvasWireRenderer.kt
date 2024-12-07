package xyz.wagyourtail.konig.editor.gui.canvas.components

import imgui.ImVec2
import imgui.internal.ImGui
import xyz.wagyourtail.konig.editor.gui.Settings.General.Appearance
import xyz.wagyourtail.konig.editor.gui.canvas.Canvas
import xyz.wagyourtail.konig.editor.gui.canvas.CanvasComponent
import xyz.wagyourtail.konig.editor.helper.Vec2d
import xyz.wagyourtail.konig.editor.helper.div
import xyz.wagyourtail.konig.editor.helper.times
import xyz.wagyourtail.konig.structure.Wire
import xyz.wagyourtail.konig.structure.WireBranch
import xyz.wagyourtail.konig.structure.WireConnection
import xyz.wagyourtail.konig.structure.WireSegment
import kotlin.math.abs

data class CanvasWireRenderer(override val canvas: Canvas<*>, val wire: Wire) : CanvasComponent {

    val scale: Float
        get() = canvas.scale

    val wireId = "##wire${wire.id}"
    val wireWidth = 4f
    val offset = ImVec2(0f, ImGui.getFrameHeight())

    @Volatile
    var activeConnection: WireConnection? = null
        get() {
            if (active) {
                return field
            } else {
                if (field != null) {
                    synchronized(this) {
                        if (field != null) {
                            clearActive(field!!)
                            field = null
                        }
                    }
                }
            }
            return null
        }
        set(value) {
            active = true
            field = value
        }

    inline fun iterateBranches(iter: (conns: MutableList<WireConnection>) -> Unit) {
        val stack = mutableListOf(wire.connections)
        while (stack.isNotEmpty()) {
            val conns = stack.removeFirst()
            iter(conns)
            stack.addAll(conns.filterIsInstance<WireBranch>().map { it.connections })
        }
    }

    fun findConnectionHolder(conn: WireConnection): MutableList<WireConnection>? {
        iterateBranches {
            if (conn in it) {
                return it
            }
        }
        return null
    }

    fun clearActive(conn: WireConnection) {
        val conns = findConnectionHolder(conn) ?: return
        if (conns.lastOrNull() == conn) {
            conns.removeLast()
        }
    }

    override fun process() {
        drawConnections(wire.connections, wireId)
    }

    fun WireConnection.toVec2(): Vec2d = Vec2d(x, y)

    fun drawConnections(connections: List<WireConnection>, id: String) {
        with (canvas) {
            for ((i, v) in connections.zipWithNext().withIndex()) {
                val (p, n) = v
                if (p is WireBranch && i != 0) {
                    ImGui.getWindowDrawList().addCircleFilled(p.toVec2().screenPos(), 5f, -1)
                    drawConnections(listOf(p) + p.connections, "$id#$i")
                }
                if (p.x == n.x) {
                    val diff = abs(p.y - n.y) * scale
                    val start = (if (p.y < n.y) {
                        p.toVec2()
                    } else {
                        n.toVec2()
                    }).screenPos() - offset - ImVec2(wireWidth / 2, wireWidth / 2f)
                    ImGui.setCursorPos(start)
                    ImGui.setItemAllowOverlap()
                    if (ImGui.button("$id#$i", ImVec2(wireWidth, diff + wireWidth / 2f))) {
                        val mousePos = ImGui.getMousePos() / scale
                        println("clicked at $mousePos (${p})")
                    }
                } else if (p.y == n.y) {
                    val diff = abs(p.x - n.x) * scale
                    val start = (if (p.x < n.x) {
                        p.toVec2()
                    } else {
                        n.toVec2()
                    }).screenPos() - offset - ImVec2(wireWidth / 2f, wireWidth / 2f)
                    ImGui.setCursorPos(start)
                    if (ImGui.button("$id#$i", ImVec2(diff + wireWidth / 2f, wireWidth))) {
                        val mousePos = ImGui.getMousePos() / scale
                        println("clicked at $mousePos (${p})")
                    }
                    ImGui.setItemAllowOverlap()
                } else {
                    TODO()
                }
            }
        }
    }

}