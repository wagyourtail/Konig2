package xyz.wagyourtail.konig.editor.gui.window.selector.components

import imgui.ImGui
import xyz.wagyourtail.commonskt.collection.DefaultMap
import xyz.wagyourtail.commonskt.collection.defaultedMapOf
import xyz.wagyourtail.commonskt.position.Pos2D
import xyz.wagyourtail.konig.editor.ABGR
import xyz.wagyourtail.konig.editor.gui.KonigEditor
import xyz.wagyourtail.konig.editor.gui.components.BlockRenderer
import xyz.wagyourtail.konig.structure.HeaderBlock
import xyz.wagyourtail.konig.structure.Hollow

open class SelectorBlockRenderer(header: HeaderBlock, id: String = "selector") : BlockRenderer<SelectorBlockRenderer.SelectorHollowRenderer>(header, id) {


    override val hollowRenderers: DefaultMap<Hollow, SelectorHollowRenderer> = defaultedMapOf {
        SelectorHollowRenderer()
    }

    override fun bgButton(btn: Boolean) {
        super.bgButton(btn)
        if (btn) {
            KonigEditor.placingBlock = object : SelectorBlockRenderer(header, "selected") {
                override val noButtons: Boolean = true
            }
        }
    }

    inner class SelectorHollowRenderer : HollowRenderer {
        override val minSize: Pos2D = Pos2D(.1, .1)
        override var size: Pos2D = minSize

        override fun process() {
            val draw = ImGui.getWindowDrawList()
            draw.addRectFilled(ImGui.getCursorPos(), ImGui.getCursorPos() + size.screenSize(), ABGR(255, 0, 0, 0).col)
        }
    }


}