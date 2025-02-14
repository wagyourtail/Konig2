package xyz.wagyourtail.konig.editor.gui.window.selector

import imgui.ImGui
import imgui.ImVec2
import imgui.flag.ImGuiWindowFlags
import xyz.wagyourtail.commonskt.position.Pos2D
import xyz.wagyourtail.konig.editor.gui.window.Settings.General.Appearance.scale
import xyz.wagyourtail.konig.editor.gui.window.selector.components.SelectorBlockRenderer
import xyz.wagyourtail.konig.editor.helper.times
import xyz.wagyourtail.konig.headers.HeaderResolver

class NodeSelector(
    val group: String,
    val headers: HeaderResolver
) {

    val windowId = "###canvas#$group"

    val blockRenderers = buildList {
        for (header in headers.byGroup.getValue(group).values) {
            add(SelectorBlockRenderer(header))
        }
    }

    fun process() {
        val cursor = ImGui.getCursorPos()

        if (ImGui.begin("$group$windowId", ImGuiWindowFlags.NoMove or ImGuiWindowFlags.HorizontalScrollbar or ImGuiWindowFlags.NoScrollWithMouse)) {
            ImGui.setScrollX(ImGui.getScrollX() - ImGui.getIO().mouseWheel * 9)
            for (blockRenderer in blockRenderers) {
                blockRenderer.process()
                val draw = ImGui.getWindowDrawList()
                draw.addCircleFilled(ImGui.getCursorScreenPos(), 5f, 0xFFFFFFFF.toInt())
                with (blockRenderer) {
                    ImGui.setCursorPos(cursor + Pos2D(size.x + .5f, 0).screenSize())
                }
            }
        }

        ImGui.end()
    }


}
