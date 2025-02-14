package xyz.wagyourtail.konig.editor.gui.window

import imgui.ImGuiWindowClass
import imgui.flag.ImGuiViewportFlags
import imgui.internal.ImGui
import imgui.internal.flag.ImGuiDockNodeFlags
import xyz.wagyourtail.konig.editor.gui.KonigEditor.canvas
import xyz.wagyourtail.konig.editor.gui.window.canvas.components.CanvasBlockRenderer

object BlockOptions {

    fun process() {
        if (canvas.activeComponents.size != 1) return
        val first = canvas.activeComponents.first()
        if (first !is CanvasBlockRenderer) return
        ImGui.setNextWindowClass(ImGuiWindowClass().apply {
            viewportFlagsOverrideSet = ImGuiViewportFlags.Minimized
            dockNodeFlagsOverrideSet = ImGuiDockNodeFlags.NoTabBar
        })
        if (ImGui.begin("blockOptions")) {
            ImGui.textWrapped("${first.header} ${first.id}")
        }
        ImGui.end()
    }

}