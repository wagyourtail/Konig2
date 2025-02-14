package xyz.wagyourtail.konig.editor.gui.window.canvas

import imgui.ImGuiWindowClass
import imgui.ImVec2
import imgui.flag.ImGuiMouseButton
import imgui.flag.ImGuiStyleVar
import imgui.flag.ImGuiViewportFlags
import imgui.flag.ImGuiWindowFlags
import imgui.internal.ImGui
import imgui.internal.flag.ImGuiDockNodeFlags
import xyz.wagyourtail.commonskt.position.Pos2D
import xyz.wagyourtail.commonskt.position.max
import xyz.wagyourtail.konig.editor.ABGR
import xyz.wagyourtail.konig.editor.helper.copy
import xyz.wagyourtail.konig.editor.helper.div
import xyz.wagyourtail.konig.editor.helper.toPos2D
import kotlin.math.max
import xyz.wagyourtail.konig.headers.HeaderResolver
import xyz.wagyourtail.konig.structure.Main

class RootCanvas(
    val path: String,
    headers: HeaderResolver,
    code: Main
) : Canvas<Main>("###canvas#$path", headers, code) {

    val windowId = "###canvasWindow#$path"

    override var pos = Pos2D(-5.0, -5.0)

    override var size: Pos2D = Pos2D(.1, .1)

    override fun bgButton(btn: Boolean) {
        super.bgButton(btn)
        ImGui.setItemAllowOverlap()
        if (ImGui.isItemHovered()) {
            val delta = ImGui.getIO().mouseWheel
            if (delta != 0f) {
                scale = max(1.0, scale + delta)
            }
        }
        if (ImGui.isMouseDown(ImGuiMouseButton.Middle) && ImGui.isItemHovered()) {
            pos -= (ImGui.getIO().mouseDelta / scale.toFloat()).toPos2D()
        }
        if (btn) {
            if (ImGui.isMouseReleased(ImGuiMouseButton.Left)) {
                println("click on $id")
            }
            if (ImGui.isMouseReleased(ImGuiMouseButton.Right)) {
                println("r-click on $id")
            }
        }
    }

    fun drawEditorInfo() {
        val draw = ImGui.getWindowDrawList()
        val mousePos = (ImGui.getMousePos() - screenPos) / scale.toFloat()
        val info = "x: %1.2f, y: %1.2f, scale: %1.1f, size: %1.1f %1.1f".format(mousePos.x, mousePos.y, scale, size.x, size.y)
        val infoSize = ImGui.calcTextSize(info)
        val infoPos = (pos + size).screenPos() - infoSize - ImVec2(3f, 3f)
        val infoEndPos = infoPos.copy() + infoSize
        draw.addRectFilled(infoPos, infoEndPos, ABGR(0xFF000000).col)
        draw.addText(infoPos, -1, info)
    }

    override fun process() {
        ImGui.setNextWindowClass(ImGuiWindowClass().apply {
            viewportFlagsOverrideSet = ImGuiViewportFlags.Minimized
            dockNodeFlagsOverrideSet = ImGuiDockNodeFlags.NoTabBar
        })
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, ImVec2(0f, 0f))
        if (ImGui.begin("$path$windowId", ImGuiWindowFlags.NoScrollWithMouse or ImGuiWindowFlags.NoScrollbar or ImGuiWindowFlags.NoMove)) {

            val min = ImGui.getCursorPos()
            val max = ImGui.getWindowPos() + ImGui.getWindowSize()
//            val max = ImGui.getContentRegionMax()
            // max(ImGui.getContentRegionAvail(), ImVec2(1f, 1f))
            size = max(((max - min) / scale.toFloat()).toPos2D(), Pos2D(.1, .1))

            super.process()

            drawEditorInfo()
        }
        ImGui.end()
        ImGui.popStyleVar()
    }

}