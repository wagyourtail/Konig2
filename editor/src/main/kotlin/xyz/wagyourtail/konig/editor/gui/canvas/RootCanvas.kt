package xyz.wagyourtail.konig.editor.gui.canvas

import imgui.ImGuiWindowClass
import imgui.ImVec2
import imgui.flag.ImGuiMouseButton
import imgui.flag.ImGuiViewportFlags
import imgui.flag.ImGuiWindowFlags
import imgui.internal.ImGui
import xyz.wagyourtail.konig.editor.ABGR
import xyz.wagyourtail.konig.editor.helper.Vec2d
import xyz.wagyourtail.konig.editor.helper.copy
import xyz.wagyourtail.konig.editor.helper.div
import xyz.wagyourtail.konig.editor.helper.max
import kotlin.math.max
import xyz.wagyourtail.konig.headers.HeaderResolver
import xyz.wagyourtail.konig.structure.Main

class RootCanvas(
    val path: String,
    headers: HeaderResolver,
    code: Main
) : Canvas<Main>("###canvas#$path", headers, code) {

    val windowId = "###canvasWindow#$path"

    override var pos = Vec2d(-5f, -5f)

    override var size: Vec2d = Vec2d(.1, .1)

    override fun drawBg() {
        super.drawBg()
        ImGui.setItemAllowOverlap()
        if (ImGui.isItemClicked(ImGuiMouseButton.Right)) {
            println("r-click on $id")
        }
        if (ImGui.isItemHovered()) {
            val delta = ImGui.getIO().mouseWheel
            if (delta != 0f) {
                scale = max(1f, scale - delta)
            }
            if (ImGui.isMouseDown(ImGuiMouseButton.Middle)) {
                pos -= Vec2d(ImGui.getIO().mouseDelta / scale)
            }
        }
    }

    fun drawEditorInfo() {
        val draw = ImGui.getWindowDrawList()
        val mousePos = (ImGui.getMousePos() - screenPos) / scale
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
        })
        if (ImGui.begin("$path$windowId", ImGuiWindowFlags.NoScrollWithMouse or ImGuiWindowFlags.NoScrollbar or ImGuiWindowFlags.NoMove)) {

            ImGui.newLine()

            val min = ImGui.getCursorPos()
            val max = ImGui.getWindowPos() + ImGui.getWindowSize()
//            val max = ImGui.getContentRegionMax()
            // max(ImGui.getContentRegionAvail(), ImVec2(1f, 1f))
            size = max(Vec2d((max - min) / scale), Vec2d(.1f, .1f))

            super.process()

            drawEditorInfo()
        }
        ImGui.end()
    }

}