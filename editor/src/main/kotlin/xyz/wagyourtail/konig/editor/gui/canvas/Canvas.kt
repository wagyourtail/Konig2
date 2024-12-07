package xyz.wagyourtail.konig.editor.gui.canvas

import imgui.ImDrawList
import imgui.ImVec2
import imgui.flag.ImGuiKey
import imgui.flag.ImGuiMouseButton
import imgui.internal.ImGui
import xyz.wagyourtail.commonskt.collection.defaultedMapOf
import xyz.wagyourtail.konig.editor.ABGR
import xyz.wagyourtail.konig.editor.gui.canvas.components.CanvasBlockRenderer
import xyz.wagyourtail.konig.editor.gui.canvas.components.CanvasWireRenderer
import xyz.wagyourtail.konig.editor.helper.Vec2d
import xyz.wagyourtail.konig.editor.helper.copy
import xyz.wagyourtail.konig.editor.helper.div
import xyz.wagyourtail.konig.editor.helper.times
import xyz.wagyourtail.konig.headers.HeaderResolver
import xyz.wagyourtail.konig.structure.Code
import java.util.*
import kotlin.math.ceil

abstract class Canvas<T: Code>(
    val id: String,
    val headers: HeaderResolver,
    val code: T,
) {

    open var scale = 50f
    open var screenPos = ImVec2()
    open val pos = Vec2d(0f, 0f)
    abstract val size: Vec2d

    open fun drawCode() {
        var renderer: CanvasComponent
        for (wire in code.wires.wires) {
            renderer = wireRenderers[wire.id]
            if (renderer in activeComponent) {
                continue
            }
            renderer.process()
        }
        for (block in code.blocks.blocks) {
            renderer = blockRenderers[block.id]
            if (renderer in activeComponent) {
                continue
            }
            renderer.process()
        }
        for (active in activeComponent.toMutableSet()) {
            active.process()
        }
    }

    val wireRenderers = defaultedMapOf<Int, CanvasWireRenderer>(WeakHashMap()) { wireId ->
        val wire = code.wires.wires.first { it.id == wireId }
        CanvasWireRenderer(this, wire)
    }

    val blockRenderers = defaultedMapOf<Int, CanvasBlockRenderer>(WeakHashMap()) { blockId ->
        val block = code.blocks.blocks.first { it.id == blockId }
        CanvasBlockRenderer(this, block)
    }

    open val activeComponent = mutableSetOf<CanvasComponent>()

    open fun drawBg() {
        ImGui.invisibleButton("$id.bg", size.screenSize())
        ImGui.setItemAllowOverlap()

        val draw = ImGui.getWindowDrawList()

        val topLeftPos = pos
        val bottomRightPos = pos + size

        draw.addRectFilled(
            topLeftPos.screenPos(),
            bottomRightPos.screenPos(),
            ABGR(255, 25, 25, 25).col
        )//ABGR(ImGui.getStyle().getColor(ImGuiCol.ChildBg)).col)

        drawBgLines(draw, topLeftPos, bottomRightPos)

        if (ImGui.isItemClicked()) {
            activeComponent.clear()
            println("clicked bg")
        }
    }

    fun drawBgLines(draw: ImDrawList, topLeftPos: Vec2d, bottomRightPos: Vec2d) {
        val lineColor = ABGR(0xFF3F3F3F).col
        val spacing = if (scale > 50) 5f else if (scale > 20) 10f else 50f
        val xRange = (ceil(topLeftPos.x / spacing).toInt() until ceil(bottomRightPos.x / spacing).toInt()).toList()
        val yRange = (ceil(topLeftPos.y / spacing).toInt() until ceil(bottomRightPos.y / spacing).toInt()).toList()
        val lAlt = ImGui.isKeyDown(ImGuiKey.LeftAlt)
        for (i in xRange) {
            val startPos = Vec2d(i * spacing, topLeftPos.y.toFloat())
            val endPos = Vec2d(startPos.x, bottomRightPos.y)
            draw.addLine(startPos.screenPos(), endPos.screenPos(), lineColor, 2f)
            if (lAlt) {
                draw.addText(ImGui.getFont(), ImGui.getFontSize(), startPos.screenPos(), -1, "%.0f".format(i * spacing))
            }
        }
        for (i in yRange) {
            val startPos = Vec2d(topLeftPos.x.toFloat(), i * spacing)
            val endPos = Vec2d(bottomRightPos.x, startPos.y)
            draw.addLine(startPos.screenPos(), endPos.screenPos(), lineColor, 2f)
            if (lAlt) {
                draw.addText(ImGui.getFont(), ImGui.getFontSize(), startPos.screenPos() + ImVec2(10f, 0f), -1, "%.0f".format(i * spacing))
            }
        }
    }

    open fun process() {
        ImGui.beginGroup()
        screenPos = ImGui.getCursorPos()
        val windowPos = ImGui.getWindowPos()

        drawBg()
        drawCode()

        ImGui.endGroup()
    }

    fun fromScreenSpace(vec: ImVec2): Vec2d {
        return Vec2d((vec.copy() - screenPos) / scale) + pos
    }

    fun Vec2d.screenPos(): ImVec2 {
        return ImVec2((x - pos.x).toFloat(), (y - pos.y).toFloat()) * scale + screenPos
    }

    fun Vec2d.screenSize(): ImVec2 {
        return ImVec2(x.toFloat(), y.toFloat()) * scale
    }

}
