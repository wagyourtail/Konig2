package xyz.wagyourtail.konig.editor.gui.canvas

import imgui.ImDrawList
import imgui.internal.ImGui
import xyz.wagyourtail.konig.editor.gui.block.BlockRenderer
import xyz.wagyourtail.konig.editor.gui.canvas.components.CanvasBlockRenderer
import xyz.wagyourtail.konig.editor.helper.Vec2d
import xyz.wagyourtail.konig.editor.helper.max
import xyz.wagyourtail.konig.headers.HeaderResolver
import xyz.wagyourtail.konig.structure.InnerCode
import xyz.wagyourtail.konig.structure.WireEnd

class InnerCanvas(id: String, headers: HeaderResolver, code: InnerCode, val parent: CanvasBlockRenderer): Canvas<InnerCode>(id, headers, code), BlockRenderer.HollowRenderer {

    override val minSize: Vec2d
        get() {
            var min = Vec2d(.1f, .1f)
            for (block in code.blocks.blocks) {
                val b = blockRenderers[block.id]
                min = max(Vec2d(b.x, b.y) + b.size, min)
            }
            for (wire in code.wires.wires) {
                val w = wireRenderers[wire.id]
                w.iterateBranches {
                    for (i in it) {
                        min = if (i is WireEnd) {
                            max(Vec2d(i.x, i.y), min)
                        } else {
                            max(Vec2d(i.x + .1f, i.y + .1f), min)
                        }
                    }
                }
            }
            return min
        }

    override fun drawBg() {
        super.drawBg()
        if (parent.active) {
            ImGui.setItemAllowOverlap()
        } else {
            if (ImGui.isItemClicked()) {
                parent.onBgClicked()
                println("Inner Activated")
            }
        }
    }

    override val activeComponent: MutableSet<CanvasComponent>
        get() = if (parent.active) super.activeComponent else {
            super.activeComponent.clear()
            mutableSetOf()
        }

    override var scale: Float
        get() = parent.scale
        set(value) = throw IllegalArgumentException()

    override var size: Vec2d = minSize
        set(value) {
            field = max(minSize, value)
        }

    override fun process() {
        super.process()
    }

}