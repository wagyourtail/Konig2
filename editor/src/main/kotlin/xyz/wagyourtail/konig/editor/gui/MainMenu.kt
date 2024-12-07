package xyz.wagyourtail.konig.editor.gui

import imgui.ImGui
import org.lwjgl.glfw.GLFW
import xyz.wagyourtail.konig.editor.lang.L10N
import java.nio.file.Path

object MainMenu {

    fun process() {
        if (ImGui.beginMainMenuBar()) {
            file()
            edit()
            view()
        }
        ImGui.endMainMenuBar()
    }

    val file = L10N.translate("menu.file")
    val fileNew = L10N.translate("menu.file.new")
    val fileOpen = L10N.translate("menu.file.open")
    val fileSave = L10N.translate("menu.file.save")
    val fileSaveAs = L10N.translate("menu.file.saveAs")
    val fileRecent = L10N.translate("menu.file.recent")
    val fileRecentEmpty = L10N.translate("menu.file.recent.empty")
    val fileExit = L10N.translate("menu.file.exit")

    fun file() {
        if (ImGui.beginMenu(file.imguiString())) {
            if (ImGui.menuItem(fileNew.imguiString())) {
                fileNew()
            }
            if (ImGui.menuItem(fileOpen.imguiString())) {
                fileOpen()
            }
            if (ImGui.menuItem(fileSave.imguiString())) {
                fileSave()
            }
            if (ImGui.menuItem(fileSaveAs.imguiString())) {
                fileSaveAs()
            }
            if (ImGui.beginMenu(fileRecent.imguiString())) {
                if (Settings.recentOpened.isEmpty()) {
                    ImGui.menuItem(fileRecentEmpty.imguiString(), false, false)
                }
                for ((i, recent) in Settings.recentOpened.withIndex()) {
                    if (ImGui.menuItem("$recent###recent$i")) {
                        fileOpenRecent(recent)
                    }
                }
                ImGui.endMenu()
            }
            if (ImGui.menuItem(fileExit.imguiString())) {
                fileExit()
            }
            ImGui.endMenu()
        }
    }

    val edit = L10N.translate("menu.edit")
    val settings = L10N.translate("menu.edit.settings")

    fun edit() {
        if (ImGui.beginMenu(edit.imguiString())) {
            if (ImGui.menuItem(settings.imguiString())) {
                Settings.shown = true
            }
            ImGui.endMenu()
        }
    }

    val view = L10N.translate("menu.view")
    val viewReset = L10N.translate("menu.view.reset")

    fun view() {
        if (ImGui.beginMenu(view.imguiString())) {
            if (ImGui.menuItem(viewReset.imguiString())) {
                viewReset()
            }
            ImGui.endMenu()
        }
    }

    fun fileNew() {
        println("new")
    }

    fun fileOpen() {
        println("open")
    }

    fun fileSave() {
        println("save")
    }

    fun fileSaveAs() {
        println("saveAs")
    }

    fun fileOpenRecent(option: Path) {
        println("openRecent $option")
    }

    fun fileExit() {
        GLFW.glfwSetWindowShouldClose(EditorWindow.handle, true)
    }

    fun viewReset() {
        Settings.resetUI = true
        Settings.apply()
    }

}