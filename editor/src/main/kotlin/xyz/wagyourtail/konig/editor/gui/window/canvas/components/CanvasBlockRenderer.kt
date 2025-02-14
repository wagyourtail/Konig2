package xyz.wagyourtail.konig.editor.gui.window.canvas.components

import imgui.ImVec2
import imgui.flag.ImGuiKey
import imgui.flag.ImGuiMouseButton
import imgui.internal.ImGui
import xyz.wagyourtail.commonskt.collection.DefaultMap
import xyz.wagyourtail.commonskt.collection.defaultedMapOf
import xyz.wagyourtail.commonskt.position.Pos2D
import xyz.wagyourtail.commonskt.utils.mutliAssociate
import xyz.wagyourtail.commonskt.utils.roundToMultipleOf
import xyz.wagyourtail.konig.editor.gui.window.Settings
import xyz.wagyourtail.konig.editor.gui.components.BlockRenderer
import xyz.wagyourtail.konig.editor.gui.window.canvas.Canvas
import xyz.wagyourtail.konig.editor.gui.window.canvas.CanvasComponent
import xyz.wagyourtail.konig.editor.gui.window.canvas.InnerCanvas
import xyz.wagyourtail.konig.editor.helper.*
import xyz.wagyourtail.konig.structure.*

class CanvasBlockRenderer(override val canvas: Canvas<*>, val block: Block) : BlockRenderer<InnerCanvas>(canvas.headers.getValue(block.type), block.id.toString()),
    CanvasComponent {

    val _ports = CachedValue {
        val p = super.ports.mapValues { it.value.mapValues { it.value.toMutableList() }.toMutableMap() }.toMutableMap()
        for (virtualPorts in this.virtualPorts.get().values) {
            for ((side, ports) in virtualPorts) {
                for ((justify, port) in ports) {
                    p.getOrPut(side) { mutableMapOf() }.getOrPut(justify) { mutableListOf() }.addAll(port)
                }
            }
        }
        p
    }

    val virtualPorts: CachedValue<Map<List<String>, Map<Side, Map<Justify, List<Port>>>>> = CachedValue(_ports) {
        block.virtual?.ports?.mutliAssociate { it.hollow to it }?.mapValues { it.value.mutliAssociate { it.side to it }.mapValues { it.value.mutliAssociate { it.justify to it } } } ?: emptyMap()
    }

    override val ports: Map<Side, Map<Justify, List<HeaderBlockIOField>>>
        get() = _ports.get()

    override var size: Pos2D
        get() = super.size
        set(value) {
            super.size = value
            block.scaleX = super.size.x.toFloat()
            block.scaleY = super.size.y.toFloat()
        }

    override var scale: Double
        get() = canvas.scale
        set(value) {
            canvas.scale = value
        }

    // snap to grid deltas.
    var x = 0f
    var y = 0f

    override val hollowRenderers: DefaultMap<Hollow, InnerCanvas> = defaultedMapOf { hollow ->
        InnerCanvas(
            canvas.id + "#hollow#$id#${hollow.name}",
            canvas.headers,
            block.innercode.first { it.name == hollow.name },
            hollow,
            this
        ).also {
            it.fixWireEndPositions()
        }
    }

    init {
        super.size = Pos2D(block.scaleX.toDouble(), block.scaleY.toDouble())
    }

    fun Block.toPos2D(): Pos2D = Pos2D(x.toDouble(), y.toDouble())

    override fun process() {
        val pos = with(canvas) {
            Pos2D(block.x.toDouble(), block.y.toDouble()).screenPos() - ImVec2(0f, ImGui.getFrameHeight())
        }
        ImGui.setCursorPos(pos)
        super.process()
    }

    override fun drawBg() {
        super.drawBg()
        if (active) {
            val draw = ImGui.getWindowDrawList()
            draw.addRect(Pos2D.ZERO.screenPos(), size.screenPos(), -1)
        }
    }

    fun onLeftClick() {
        if (!ImGui.isKeyDown(ImGuiKey.LeftCtrl)) {
            canvas.activeComponents.clear()
        }
        active = true
    }

    fun tryDrag() {
        val currentPos = block.toPos2D()
        val (x,y) = ImGui.getIO().mouseDelta / scale.toFloat()
        if (Settings.Editor.Canvas.snapToGrid) {
            this.x += x
            this.y += y
            // snap to .2f sized grid
            val dx = this.x.roundToMultipleOf(Settings.Editor.Canvas.gridSize)
            val dy = this.y.roundToMultipleOf(Settings.Editor.Canvas.gridSize)
            block.x += dx
            block.y += dy
            this.x -= dx
            this.y -= dy
        } else {
            block.x += x
            block.y += y
        }
        val newPos = block.toPos2D()
        val diff = newPos - currentPos
        if (diff == Pos2D.ZERO) return
        fixWireEndPositions()
    }

    fun fixWireEndPositions() {
        val pos = block.toPos2D()
        val offsets = portOffsetLocations.get()
        for (port in block.io.ports.toList()) {
            try {
                val wire = canvas.getWireRenderer(port.wire)
                if (wire?.activeConnections?.any { it is WireEnd && it.block == block.id && it.port == port.name } == true) continue
                if (wire?.moveEndTo(
                        block.id to port.name,
                        offsets.getValue(
                            header.io.byName[port.name] ?: block.virtual?.byName?.getValue(port.name) ?: error("Unknown port: $port")
                        ) + pos
                    ) != true
                ) {
                    block.io.ports.remove(port)
                }
            } catch (e: Exception) {
                throw RuntimeException("error with block $block", e)
            }
        }
    }

    override fun bgButton(btn: Boolean) {
        super.bgButton(btn)
        ImGui.setItemAllowOverlap()
        if (active && canvas.hovered && ImGui.isMouseDown(ImGuiMouseButton.Left) && !hollowRenderers.values.any { it.activeComponents.isNotEmpty() }) {
            tryDrag()
        } else {
            this.x = 0f
            this.y = 0f
        }
        if (btn) {
            if (ImGui.isMouseClicked(ImGuiMouseButton.Left)) {
                println("block clicked ${block}")
                onLeftClick()
            }
            if (ImGui.isMouseClicked(ImGuiMouseButton.Right)) {
                println("block right clicked")
            }
        }
    }

    override fun portButton(inp: HeaderBlockIOField, btn: Boolean) {
        if (btn) {
            println("clicked $inp on $block")
        }
    }

}