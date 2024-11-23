package xyz.wagyourtail.konig.editor

import imgui.ImGui.*
import imgui.ImGuiWindowClass
import imgui.ImVec2
import imgui.app.Application
import imgui.app.Configuration
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiConfigFlags
import imgui.flag.ImGuiDir
import imgui.flag.ImGuiWindowFlags
import imgui.internal.ImGui
import imgui.internal.flag.ImGuiDockNodeFlags
import imgui.type.ImInt
import imgui.type.ImString
import org.lwjgl.glfw.GLFW

fun main() {
    Application.launch(EditorMain)
}

object EditorMain : Application() {

    val actions = mutableListOf<() -> Unit>()

    override fun initImGui(config: Configuration?) {
        super.initImGui(config)
        val io = getIO()
        io.iniFilename = null
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard)
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable)
        io.configViewportsNoTaskBarIcon = true
        io.wantCaptureKeyboard = true
        io.wantCaptureMouse = true
//        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable)
    }

    override fun preProcess() {
        styleColorsDark()
        actions.forEach { it() }
    }

    var first = false
    var pos: ImVec2? = null

    val testStr = ImString()

    override fun process() {
        val dockId = dockSpaceOverViewport(getMainViewport())

        if (beginMainMenuBar()) {
            if (beginMenu("file")) {
                if (menuItem("Open File")) {
                    println("Open")
                }
                if (menuItem("Exit")) {
                    actions.add { GLFW.glfwSetWindowShouldClose(getHandle(), true) }
                }
                endMenu()
            }
            endMainMenuBar()
        }

        if (begin("test")) {
            text("Test Text")
        }
        end()

        ImGui.setNextWindowClass(ImGuiWindowClass().apply {
            dockNodeFlagsOverrideSet = ImGuiDockNodeFlags.NoTabBar
        })
        if (begin("test2")) {
            if (ImGui.isItemActive()) {
                println("test2")
            }
            if (pos == null) {
                pos = ImGui.getCursorPos()
            }
            ImGui.setCursorPos(pos!!)
            ImGui.pushStyleColor(ImGuiCol.ChildBg, 0xFFFF0000.toInt())
            if (ImGui.beginChild("childTest", 50f, 50f, true, ImGuiWindowFlags.NoScrollbar or ImGuiWindowFlags.NoScrollWithMouse)) {
                val p = ImGui.getCursorPos()
                ImGui.invisibleButton("button", 50f, 50f)
                if (ImGui.isItemClicked()) {
                    println("clicked")
                }
                if (ImGui.isItemActive()) {
                    pos = pos!! + ImGui.getIO().mouseDelta
                }
                if (ImGui.isItemHovered()) {
                    ImGui.setTooltip("tooltip")
                }
                ImGui.setCursorPos(p)
                ImGui.text("test")
            }
            ImGui.endChild()
            ImGui.popStyleColor()
        }
        end()

        if (!first && !ImGui.dockBuilderGetNode(dockId).isSplitNode) {
            val first = ImInt()
            val sec = ImInt()
            ImGui.dockBuilderSplitNode(dockId, ImGuiDir.Down, 0.3f, first, sec)
            ImGui.dockBuilderDockWindow("test", first.get())
            ImGui.dockBuilderDockWindow("test2", sec.get())
            this.first = true
        }

    }

}