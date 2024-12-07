package xyz.wagyourtail.konig.editor.gui.settings

import imgui.ImGui
import imgui.ImGuiWindowClass
import imgui.ImVec2
import imgui.internal.flag.ImGuiDockNodeFlags
import xyz.wagyourtail.konig.editor.lang.L10N

open class RootSettingGroup : SettingGroup("settings") {
    final override val root: RootSettingGroup = this
    var active: SettingGroup = this

    var shown = false
    private val applyBtn = L10N.translate("settings.apply")
    private val closeBtn = L10N.translate("settings.close")

    override val allSettings by lazy {
        ensureInit()
        super.allSettings
    }

    open fun process() {
        if (!shown) return

        ImGui.setNextWindowSizeConstraints(ImVec2(500f, 500f), ImVec2(10000f,10000f))
        ImGui.setNextWindowClass(ImGuiWindowClass().apply {
            dockNodeFlagsOverrideSet = ImGuiDockNodeFlags.NoDocking
        })
        if (ImGui.begin(translateValue.imguiString())) {
            val style = ImGui.getStyle()
            val h = ImGui.getTextLineHeight() + style.itemSpacingY * 2
            val avail = ImGui.getContentRegionAvail()
            avail.y -= h
            if (ImGui.beginChild("##settingHolder", avail)) {
                ImGui.columns(2)
                renderSubtree()
                ImGui.nextColumn()
                active.renderContent()
                ImGui.columns()
                ImGui.endChild()
            }

            // compute length of buttons
            val applySize = ImGui.calcTextSize(applyBtn.toString())
            val closeSize = ImGui.calcTextSize(closeBtn.toString())
            val len = applySize.x + closeSize.x + style.itemInnerSpacingX * 4 + style.itemSpacingX * 2

            ImGui.setCursorPos(ImGui.getContentRegionMax() - ImVec2(len, h))
            val changed = allSettings.all { it.value == it.temp }
            ImGui.beginDisabled(changed)
            if (ImGui.button(applyBtn.imguiString())) {
                apply()
            }
            ImGui.endDisabled()
            ImGui.sameLine()
            if (ImGui.button(closeBtn.imguiString())) {
                if (changed) reset()
                shown = false
            }
        }

        ImGui.end()
    }
}