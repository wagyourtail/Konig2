package xyz.wagyourtail.konig.editor.gui.canvas.components

import imgui.ImVec2
import imgui.flag.ImGuiKey
import imgui.flag.ImGuiMouseButton
import imgui.internal.ImGui
import xyz.wagyourtail.commonskt.collection.DefaultMap
import xyz.wagyourtail.commonskt.collection.defaultedMapOf
import xyz.wagyourtail.commonskt.utils.roundToMultipleOf
import xyz.wagyourtail.konig.editor.gui.Settings
import xyz.wagyourtail.konig.editor.gui.block.BlockRenderer
import xyz.wagyourtail.konig.editor.gui.canvas.Canvas
import xyz.wagyourtail.konig.editor.gui.canvas.CanvasComponent
import xyz.wagyourtail.konig.editor.gui.canvas.InnerCanvas
import xyz.wagyourtail.konig.editor.helper.*
import xyz.wagyourtail.konig.structure.Block
import xyz.wagyourtail.konig.structure.HeaderBlockIOField
import xyz.wagyourtail.konig.structure.Hollow

data class CanvasBlockRenderer(override val canvas: Canvas<*>, val block: Block) : BlockRenderer(canvas.headers.getValue(block.type), block.id.toString()), CanvasComponent {

    override var size: Vec2d
        get() = super.size
        set(value) {
            super.size = value
            block.scaleX = super.size.x.toFloat()
            block.scaleY = super.size.y.toFloat()
        }

    override val scale: Float
        get() = canvas.scale

    // snap to grid deltas.
    var x = block.x
    var y = block.y

    override val hollowRenderers: DefaultMap<Hollow, HollowRenderer> = defaultedMapOf { hollow ->
        InnerCanvas(
            canvas.id + "\$hollow\$$id\$${hollow.name}",
            canvas.headers,
            block.innercode.first { it.name == hollow.name },
            this
        )
    }

    init {
        super.size = Vec2d(block.scaleX, block.scaleY)
    }

    override fun process() {
        val pos = with(canvas) {
            Vec2d(block.x, block.y).screenPos() - ImVec2(0f, ImGui.getFrameHeight())
        }
        ImGui.setCursorPos(pos)
        super.process()
    }

    override fun drawBg() {
        super.drawBg()
        if (active) {
            val draw = ImGui.getWindowDrawList()
            draw.addRect(Vec2d().screenPos(), size.screenPos(), -1)
        }
    }

    override fun bgButton(btn: Boolean) {
        super.bgButton(btn)
        if (active && ImGui.isMouseDown(ImGuiMouseButton.Left)) {
            val (x,y) = ImGui.getIO().mouseDelta / scale
            if (Settings.Editor.Canvas.snapToGrid) {
                this.x += x
                this.y += y
                // snap to .2f sized grid
                block.x = this.x.roundToMultipleOf(Settings.Editor.Canvas.gridSize)
                block.y = this.y.roundToMultipleOf(Settings.Editor.Canvas.gridSize)
            } else {
                block.x += x
                block.y += y
            }
        } else {
            this.x = block.x
            this.y = block.y
        }
        if (ImGui.isItemClicked(ImGuiMouseButton.Right)) {
            println("r-click $block")
        }
    }

    override fun onBgClicked() {
        if (!ImGui.isKeyDown(ImGuiKey.LeftCtrl)) {
            canvas.activeComponent.clear()
        }
        active = true
    }

    override fun onPortClicked(inp: HeaderBlockIOField) {
        super.onPortClicked(inp)
        println("clicked $inp on $block")
    }

}