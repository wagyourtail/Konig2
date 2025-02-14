package xyz.wagyourtail.konig.editor.gui.window.canvas.components

import imgui.ImVec2
import imgui.flag.ImGuiKey
import imgui.flag.ImGuiMouseButton
import imgui.internal.ImGui
import imgui.internal.flag.ImGuiButtonFlags
import xyz.wagyourtail.commonskt.position.Pos2D
import xyz.wagyourtail.commonskt.utils.insertBefore
import xyz.wagyourtail.commonskt.utils.roundToMultipleOf
import xyz.wagyourtail.konig.editor.gui.window.Settings
import xyz.wagyourtail.konig.editor.gui.window.canvas.Canvas
import xyz.wagyourtail.konig.editor.gui.window.canvas.CanvasComponent
import xyz.wagyourtail.konig.editor.helper.CachedValue
import xyz.wagyourtail.konig.editor.helper.component1
import xyz.wagyourtail.konig.editor.helper.component2
import xyz.wagyourtail.konig.editor.helper.div
import xyz.wagyourtail.konig.structure.*
import kotlin.math.abs

data class CanvasWireRenderer(override val canvas: Canvas<*>, val wire: Wire) : CanvasComponent {

    val scale: Double
        get() = canvas.scale

    val wireId = "##wire${wire.id}"
    val wireWidth = 4f
    val offset = ImVec2(0f, ImGui.getFrameHeight())

    // snap to grid deltas.
    var x = 0f
    var y = 0f

    val activeConnections = mutableSetOf<WireConnection>()
        get() {
            if (active) {
                return field
            } else {
                field.clear()
                return field
            }
        }

    val wireEnds = CachedValue {
        buildMap {
            iterateBranches { conns ->
                if (conns.first() is WireEnd) {
                    val c = conns.first() as WireEnd
                    put(c.block to c.port, conns)
                }
                if (conns.last() is WireEnd) {
                    val c = conns.last() as WireEnd
                    put(c.block to c.port, conns)
                }
            }
        }
    }

    val WireEnd.endId: Pair<Int, String>
        get() = block to port

    fun moveEndTo(endId: Pair<Int, String>, position: Pos2D): Boolean {
        var connections = wireEnds.get()[endId] ?: return false
        val last = connections.last() as? WireEnd
        if (last?.endId == endId) connections = connections.asReversed()
        val first = connections.first() as? WireEnd
        if (first?.endId != endId) return false
        val next = connections.getOrNull(1) as? WireSegment
        if (next != null) {
            if (next.x == first.x) {
                next.x = position.x.toFloat()
            } else {
                next.y = position.y.toFloat()
            }
        }
        first.x = position.x.toFloat()
        first.y = position.y.toFloat()
        return true
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
        drawConnections(null, wire.connections, wireId)
    }

    fun WireConnection.toPos2D(): Pos2D = Pos2D(x.toDouble(), y.toDouble())

    fun tryDrag() {
        val (x,y) = ImGui.getIO().mouseDelta / scale.toFloat()
        var dx = 0f
        var dy = 0f
        if (Settings.Editor.Canvas.snapToGrid) {
            this.x += x
            this.y += y
            // snap to .2f sized grid
            dx = this.x.roundToMultipleOf(Settings.Editor.Canvas.gridSize)
            dy = this.y.roundToMultipleOf(Settings.Editor.Canvas.gridSize)
            this.x -= dx
            this.y -= dy
        } else {
            dx = x
            dy = y
        }
        for (wire in activeConnections) {
            if (wire is WireEnd) {
                if (canvas.getBlockRenderer(wire.block)?.active != true) continue
            }
            wire.x += dx
            wire.y += dy
        }
    }

    fun handleClick(p: WireConnection, n: WireConnection, connections: MutableList<WireConnection>) {
        var (x,y) = ImGui.getMousePos() / scale.toFloat()
        if (Settings.Editor.Canvas.snapToGrid) {
            x = x.roundToMultipleOf(Settings.Editor.Canvas.gridSize)
            y = y.roundToMultipleOf(Settings.Editor.Canvas.gridSize)
        }
        if (ImGui.isMouseClicked(ImGuiMouseButton.Left)) {
            if (!ImGui.isKeyDown(ImGuiKey.LeftCtrl)) {
                canvas.activeComponents.clear()
                activeConnections.clear()
            }
            active = true
            activeConnections.add(p)
            activeConnections.add(n)
        }
        if (ImGui.isMouseClicked(ImGuiMouseButton.Right)) {
            println("mouse r-clicked at ${x}, ${y}")
        }
    }

    var addDir = false
        get() {
            field = !field
            return field
        }

    fun drawConnections(parent: WireConnection?, connections: MutableList<WireConnection>, id: String) {
        val draw = ImGui.getWindowDrawList()
        if (active && canvas.hovered && ImGui.isMouseDown(ImGuiMouseButton.Left)) {
            tryDrag()
        } else {
            this.x = 0f
            this.y = 0f
        }
        with (canvas) {
            var pp: WireConnection? = null
            for ((i, v) in (listOf(parent) + connections).filterNotNull().zipWithNext().withIndex()) {
                val (p, n) = v
                if (p is WireBranch && i != 0) {
                    draw.addCircleFilled(p.toPos2D().screenPos(), (.06 * scale).toFloat(), -1)
                    drawConnections(p, p.connections, "$id#$i")
                }
                if (p.x == n.x) {
                    if (pp != null && p is WireSegment && pp.x == p.x) {
                        // remove unneeded segment
                        connections.remove(p)
                    }
                    val diff = abs(p.y - n.y) * scale
                    val start = (if (p.y < n.y) {
                        p.toPos2D()
                    } else {
                        n.toPos2D()
                    }).screenPos() - offset - ImVec2(wireWidth / 2, wireWidth / 2f)
                    ImGui.setCursorPos(start)
                    if (ImGui.buttonEx("$id#$i", ImVec2(wireWidth, (diff + wireWidth / 2f).toFloat()), ImGuiButtonFlags.MouseButtonMask_ or ImGuiButtonFlags.PressedOnClick)) {
                        handleClick(p, n, connections)
                    }
                    ImGui.setItemAllowOverlap()
                    pp = p
                } else if (p.y == n.y) {
                    if (pp != null && p is WireSegment && pp.y == p.y) {
                        // remove unneeded segment
                        connections.remove(p)
                    }
                    val diff = abs(p.x - n.x) * scale
                    val start = (if (p.x < n.x) {
                        p.toPos2D()
                    } else {
                        n.toPos2D()
                    }).screenPos() - offset - ImVec2(wireWidth / 2f, wireWidth / 2f)
                    ImGui.setCursorPos(start)
                    if (ImGui.buttonEx("$id#$i", ImVec2((diff + wireWidth / 2f).toFloat(), wireWidth), ImGuiButtonFlags.MouseButtonMask_ or ImGuiButtonFlags.PressedOnClick)) {
                        handleClick(p, n, connections)
                    }
                    ImGui.setItemAllowOverlap()
                    pp = p
                } else {
                    val toAdd = if (pp != null) {
                        if (pp.x == p.x) {
                            WireSegment(p.x, n.y)
                        } else {
                            WireSegment(n.x, p.y)
                        }
                    } else {
                        if (addDir) {
                            WireSegment(p.x, n.y)
                        } else {
                            WireSegment(n.x, p.y)
                        }

                    }
                    connections.insertBefore(n, toAdd)
                    if (p in activeConnections) {
                        activeConnections.add(toAdd)
                    }
                    pp = toAdd
                }
                if (p in activeConnections && n in activeConnections) {
                    draw.addLine(p.toPos2D().screenPos(), n.toPos2D().screenPos(), -1, wireWidth / 4f)
                }
            }
        }
    }

}