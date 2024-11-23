package xyz.wagyourtail.konig.editor.gui.settings

import imgui.ImGui
import imgui.type.ImFloat
import xyz.wagyourtail.konig.editor.gui.EditorWindow
import xyz.wagyourtail.konig.editor.lang.L10N

object SettingsGui {
    var shown = false
    var currentPage: SettingGroup = General

    val tempConfig = EditorWindow.config.copy()

    val settings = L10N.translate("settings")
    val apply = L10N.translate("settings.apply")
    val close = L10N.translate("settings.close")

    fun render() {
        if (!shown) return

        if (ImGui.begin(settings.imguiString())) {
            ImGui.beginTable("settingsTable", 2)
            ImGui.tableNextColumn()
            renderTree()
            ImGui.tableNextColumn()
            currentPage.renderContent()
            ImGui.endTable()

            ImGui.beginDisabled(tempConfig != EditorWindow.config)
            if (ImGui.button(apply.imguiString())) {
                EditorWindow.config = tempConfig.copy()
            }
            ImGui.endDisabled()
            if (ImGui.button(close.imguiString())) {
                shown = false
            }
        }
    }

    fun renderTree() {
        if (ImGui.treeNode(General.value.imguiString())) {
            if (ImGui.isItemClicked()) {
                currentPage = General
            }
            if (ImGui.treeNode(Appearance.value.imguiString())) {
                if (ImGui.isItemClicked()) {
                    currentPage = Appearance
                }
            }
        }
    }

    sealed class SettingGroup(key: String) {

        val value = L10N.translate(key)

        abstract fun renderContent()
    }

    data object General : SettingGroup("settings.general") {

        override fun renderContent() {
            ImGui.text("empty")
        }

    }

    data object Appearance : SettingGroup("settings.general.appearance") {

        val scale = L10N.translate("settings.general.appearance.scale")
        val fontSize = L10N.translate("settings.general.appearance.fontSize")

        override fun renderContent() {
            val scaleValue = ImFloat(tempConfig.scale)
            ImGui.inputFloat(scale.imguiString(), scaleValue)


        }
    }
}