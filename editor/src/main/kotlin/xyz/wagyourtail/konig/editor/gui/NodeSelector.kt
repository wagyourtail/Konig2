package xyz.wagyourtail.konig.editor.gui

import imgui.ImGui
import xyz.wagyourtail.konig.headers.HeaderResolver

class NodeSelector(
    val group: String,
    val headers: HeaderResolver
) {

    val windowId = "###canvas#$group"

    fun process() {
        if (ImGui.begin("$group$windowId")) {
            ImGui.text(group)
        }
        ImGui.end()

    }

}
