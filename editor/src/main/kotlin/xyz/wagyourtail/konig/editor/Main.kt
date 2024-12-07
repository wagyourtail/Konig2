package xyz.wagyourtail.konig.editor

import imgui.app.Application
import xyz.wagyourtail.konig.editor.gui.EditorWindow

fun main() {
    Application.launch(EditorWindow)
}

//object EditorMain : Application() {
//
//    val actions = mutableListOf<() -> Unit>()
//
//    override fun initImGui(config: Configuration?) {
//        super.initImGui(config)
//        val io = getIO()
//        io.iniFilename = null
//        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard)
//        io.addConfigFlags(ImGuiConfigFlags.DockingEnable)
//        io.configViewportsNoTaskBarIcon = true
//        io.wantCaptureKeyboard = true
//        io.wantCaptureMouse = true
////        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable)
//    }
//
//    override fun preProcess() {
//        styleColorsDark()
//        actions.forEach { it() }
//    }
//
//    var first = false
//    var pos: ImVec2? = null
//
//    val testStr = ImString()
//
//    override fun process() {
////
////        if (begin("test")) {
////            text("Test Text")
////        }
////        end()
////
////        ImGui.setNextWindowClass(ImGuiWindowClass().apply {
////            dockNodeFlagsOverrideSet = ImGuiDockNodeFlags.NoTabBar
////        })
////        if (begin("test2")) {
////            if (ImGui.isItemActive()) {
////                println("test2")
////            }
////            if (pos == null) {
////                pos = ImGui.getCursorPos()
////            }
////            ImGui.setCursorPos(pos!!)
////            ImGui.popStyleColor()
////        }
////        end()
////
////        if (!first && !ImGui.dockBuilderGetNode(dockId).isSplitNode) {
////            val first = ImInt()
////            val sec = ImInt()
////            ImGui.dockBuilderSplitNode(dockId, ImGuiDir.Down, 0.3f, first, sec)
////            ImGui.dockBuilderDockWindow("test", first.get())
////            ImGui.dockBuilderDockWindow("test2", sec.get())
////            this.first = true
////        }
//
//    }
//
//}