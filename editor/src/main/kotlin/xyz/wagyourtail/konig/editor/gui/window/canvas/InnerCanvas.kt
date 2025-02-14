package xyz.wagyourtail.konig.editor.gui.window.canvas

import imgui.ImDrawList
import imgui.ImVec2
import imgui.internal.ImGui
import xyz.wagyourtail.commonskt.position.Pos2D
import xyz.wagyourtail.commonskt.position.max
import xyz.wagyourtail.commonskt.utils.mutliAssociate
import xyz.wagyourtail.konig.editor.gui.components.BlockRenderer
import xyz.wagyourtail.konig.editor.gui.window.canvas.components.CanvasBlockRenderer
import xyz.wagyourtail.konig.editor.helper.CachedValue
import xyz.wagyourtail.konig.headers.HeaderResolver
import xyz.wagyourtail.konig.structure.*
import kotlin.math.max

class InnerCanvas(id: String, headers: HeaderResolver, code: InnerCode, val hollow: Hollow, val parent: CanvasBlockRenderer): Canvas<InnerCode>(id, headers, code), BlockRenderer.HollowRenderer {

    val ports = CachedValue {
        val p = hollow.io.mutliAssociate { it.side to it }.mapValues { it.value.mutliAssociate { it.justify to it }.mapValues { it.value.toMutableList() }.toMutableMap() }.toMutableMap()
        for ((hollows, virtualPorts) in parent.virtualPorts.get()) {
            if (hollow.groupId !in hollows) continue
            for ((side, ports) in virtualPorts) {
                for ((justify, port) in ports) {
                    p.getOrPut(side) { mutableMapOf() }.getOrPut(justify) { mutableListOf() }.addAll(port)
                }
            }
        }
        p
    }

    override val minSize: Pos2D
        get() {
            var min = Pos2D(.2, .2)
            // min size from code content
            for (block in code.blocks.blocks) {
                val b = blockRenderers[block.id]
                min = max(Pos2D(b.x.toDouble(), b.y.toDouble()) + b.size, min)
            }
            for (wire in code.wires.wires) {
                val w = wireRenderers[wire.id]
                w.iterateBranches {
                    for (i in it) {
                        min = if (i is WireEnd) {
                            max(Pos2D(i.x.toDouble(), i.y.toDouble()), min)
                        } else {
                            max(Pos2D(i.x + .1, i.y + .1), min)
                        }
                    }
                }
            }
            // min size from port content
            var width = .2
            for (side in listOf(Side.TOP, Side.BOTTOM)) {
                val ports = ports.get()[side]?.values?.asSequence() ?: emptySequence()
                val minDim = ports.flatMap { it }.map { if (it is Port && it.loopback) .3 else .2 }.sum() + .2
                width = max(width, minDim)
            }
            var height = .2
            for (side in listOf(Side.LEFT, Side.RIGHT)) {
                val ports = ports.get()[side]?.values?.asSequence() ?: emptySequence()
                val minDim = ports.flatMap { it }.map { if (it is Port && it.loopback) .3 else .2 }.sum() + .2
                height = max(height, minDim)
            }
            return max(min, Pos2D(width, height))
        }

    override fun bgButton(btn: Boolean) {
        super.bgButton(btn)
        ImGui.setItemAllowOverlap()
        if (btn) {
            parent.onLeftClick()
            println("Inner Activated")
        }
    }

    override val activeComponents: MutableSet<CanvasComponent>
        get() = if (parent.active) super.activeComponents else {
            super.activeComponents.clear()
            mutableSetOf()
        }

    override fun getBlockRenderer(id: Int): CanvasBlockRenderer? {
        if (id == 0) return null
        return super.getBlockRenderer(id)
    }

    override var scale: Double
        get() = parent.scale
        set(value) = throw IllegalArgumentException()

    override var size: Pos2D = minSize
        set(value) {
            field = max(minSize, value)
            portOffsetLocations.invalidate()
            fixWireEndPositions()
        }

    var portOffsetLocations: CachedValue<Map<HeaderBlockIOField, Pos2D>> = CachedValue {
        buildMap {
            for (side in Side.entries) {
                val begin = when (side) {
                    Side.TOP -> Pos2D.ZERO
                    Side.BOTTOM -> Pos2D(0.0, this@InnerCanvas.size.y)
                    Side.LEFT -> Pos2D.ZERO
                    Side.RIGHT -> Pos2D(this@InnerCanvas.size.x, 0.0)
                }
                for (justify in Justify[side]) {
                    val p = ports.get()[side]?.get(justify)
                    if (p.isNullOrEmpty()) continue
                    var c = .1
                    when (justify) {
                        Justify.TOP -> {
                            for (port in p) {
                                put(port, Pos2D(0.0, c) + begin)
                                c += if (port is Port && port.loopback) .3 else .2
                            }
                        }
                        Justify.BOTTOM -> {
                            for (port in p.reversed()) {
                                put(port, Pos2D(0.0, this@InnerCanvas.size.y - c) + begin)
                                c += if (port is Port && port.loopback) .3 else .2
                            }
                        }
                        Justify.RIGHT -> {
                            for ((i, port) in p.reversed().withIndex()) {
                                put(port, Pos2D(this@InnerCanvas.size.x - c, 0.0) + begin)
                                c += if (port is Port && port.loopback) .3 else .2
                            }
                        }
                        Justify.LEFT -> {
                            for ((i, port) in p.withIndex()) {
                                put(port, Pos2D(c, 0.0) + begin)
                                c += if (port is Port && port.loopback) .3 else .2
                            }
                        }
                        Justify.CENTER -> {
                            val length = when (side) {
                                Side.TOP, Side.BOTTOM -> this@InnerCanvas.size.x
                                Side.LEFT, Side.RIGHT -> this@InnerCanvas.size.y
                            }
                            c = length / 2f - (if (p.size == 1) 0.0 else p.sumOf { if (it is Port && it.loopback) .3 else .2 } / .2f)
                            for ((i, port) in p.withIndex()) {
                                put(port, when (side) {
                                    Side.TOP, Side.BOTTOM -> Pos2D(c, 0.0) + begin
                                    Side.LEFT, Side.RIGHT -> Pos2D(0.0, c) + begin
                                })
                                c += if (port is Port && port.loopback) .3 else .2
                            }
                        }
                    }
                }
            }
        }
    }

    val portRadius = .05

    fun portId(port: HeaderBlockIOField) = "##canvas-${hollow.groupId}#${id}#${port.name}"

    fun fixWireEndPositions() {
        val offsets = portOffsetLocations.get()
        for (port in code.io.ports.toList()) {
            try {
                val wire = getWireRenderer(port.wire)
                if (wire?.activeConnections?.any { it is WireEnd && it.block == 0 && it.port == port.name } == true) continue
                val prt = hollow.byName[port.name] ?: parent.block.virtual?.byName?.get(port.name.removeSuffix("|loopback")) ?: error("unknown port $port")
                val offset = if (prt is Port && port.name.endsWith("|loopback")) {
                    when(prt.side) {
                        Side.TOP, Side.BOTTOM -> Pos2D(.1, 0)
                        else -> Pos2D(0, .1)
                    }
                } else {
                    Pos2D.ZERO
                }
                if (wire?.moveEndTo(
                        0 to port.name,
                        offsets.getValue(prt) + pos + offset
                    ) != true
                ) {
                    code.io.ports.remove(port)
                }
            } catch (e: Exception) {
                throw RuntimeException("error with block ${parent.block} inner $code", e)
            }
        }
    }

    fun drawPorts() {
        val draw = ImGui.getWindowDrawList()
        val offset = ImVec2(0f, ImGui.getFrameHeight())
        for ((port, portPos) in portOffsetLocations.get()) {
            val centerPos = portPos.screenPos()
            drawPort(port, centerPos, draw)
            ImGui.setCursorPos(Pos2D(-portRadius, -portRadius).screenSize() + centerPos - offset)
            portButton(port, ImGui.button(portId(port), Pos2D(portRadius * 2, portRadius * 2).screenSize()), false)
            if (port is Port && port.loopback) {
                val loopbackPos = (portPos + if (port.side in listOf(Side.TOP, Side.BOTTOM)) Pos2D(0.1, 0.0) else Pos2D(0.0, 0.1)).screenPos()
                drawPort(port, loopbackPos, draw)
                ImGui.setCursorPos(Pos2D(-portRadius, -portRadius).screenSize() + loopbackPos - offset)
                portButton(port, ImGui.button(portId(port) + "#loopback", Pos2D(portRadius * 2, portRadius * 2).screenSize()), true)
            }
        }
    }

    private fun portButton(inp: HeaderBlockIOField, btn: Boolean, loopback: Boolean) {
        if (btn) {
            println("clicked $inp, $loopback")
        }
    }

    fun drawPort(inp: HeaderBlockIOField, pos: ImVec2, draw: ImDrawList) {
        draw.addCircleFilled(pos, (portRadius * scale).toFloat(), 0xFFFFFFFF.toInt())
    }

    override fun process() {
        super.process()
        drawPorts()
    }

}