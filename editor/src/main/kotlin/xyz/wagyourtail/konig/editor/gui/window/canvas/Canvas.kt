package xyz.wagyourtail.konig.editor.gui.window.canvas

import imgui.ImDrawList
import imgui.ImVec2
import imgui.flag.ImGuiHoveredFlags
import imgui.flag.ImGuiKey
import imgui.internal.ImGui
import imgui.internal.flag.ImGuiButtonFlags
import xyz.wagyourtail.commonskt.collection.defaultedMapOf
import xyz.wagyourtail.commonskt.position.Pos2D
import xyz.wagyourtail.konig.editor.ABGR
import xyz.wagyourtail.konig.editor.gui.KonigEditor
import xyz.wagyourtail.konig.editor.gui.window.canvas.components.CanvasBlockRenderer
import xyz.wagyourtail.konig.editor.gui.window.canvas.components.CanvasWireRenderer
import xyz.wagyourtail.konig.editor.helper.copy
import xyz.wagyourtail.konig.editor.helper.div
import xyz.wagyourtail.konig.editor.helper.times
import xyz.wagyourtail.konig.editor.helper.toPos2D
import xyz.wagyourtail.konig.headers.HeaderResolver
import xyz.wagyourtail.konig.structure.*
import java.util.*
import kotlin.math.ceil

abstract class Canvas<T: Code>(
    val id: String,
    val headers: HeaderResolver,
    val code: T,
) {

    open var scale: Double = 50.0
    open var screenPos = ImVec2()
    open val pos = Pos2D.ZERO
    abstract val size: Pos2D

    open fun drawCode() {
        var renderer: CanvasComponent
        for (wire in code.wires.wires) {
            renderer = wireRenderers[wire.id]
            renderer.process()
        }
        for (block in code.blocks.blocks) {
            renderer = blockRenderers[block.id]
            renderer.process()
        }
        if (hovered) {
            val placing = KonigEditor.placingBlock
            if (placing != null) {
                placing.scale = scale
                ImGui.setCursorPos(ImGui.getMousePos() - ImVec2(0f, ImGui.getFrameHeight()) - (placing.size / 2.0).screenSize())
                placing.process()
            }
        }
    }

    protected val wireRenderers = defaultedMapOf<Int, CanvasWireRenderer>(WeakHashMap()) { wireId ->
        val wire = code.wires.wires.first { it.id == wireId }
        CanvasWireRenderer(this, wire)
    }

    protected val blockRenderers = defaultedMapOf<Int, CanvasBlockRenderer>(WeakHashMap()) { blockId ->
        val block = code.blocks.blocks.first { it.id == blockId }
        CanvasBlockRenderer(this, block).also {
            it.fixWireEndPositions()
        }
    }

    open fun onLeftClick() {
    }

    open fun getBlockRenderer(id: Int): CanvasBlockRenderer? {
        try {
            return blockRenderers[id]
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    open fun getWireRenderer(id: Int): CanvasWireRenderer? {
        try {
            return wireRenderers[id]
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    open val activeComponents = mutableSetOf<CanvasComponent>()
    var hovered = false

    open fun drawBg() {
        val draw = ImGui.getWindowDrawList()

        val topLeftPos = pos
        val bottomRightPos = pos + size

        draw.addRectFilled(
            topLeftPos.screenPos(),
            bottomRightPos.screenPos(),
            if (hovered) ABGR(255, 25, 25, 25).col else ABGR(255, 20, 20, 20).col
        )//ABGR(ImGui.getStyle().getColor(ImGuiCol.ChildBg)).col)

        drawBgLines(draw, topLeftPos, bottomRightPos)

        ImGui.setCursorPos(screenPos.copy() - ImVec2(0f, ImGui.getFrameHeight()))

        bgButton(ImGui.invisibleButton("$id.bg", size.screenSize(), ImGuiButtonFlags.MouseButtonMask_ or ImGuiButtonFlags.PressedOnClick))
        hovered = ImGui.isItemHovered(ImGuiHoveredFlags.AllowWhenOverlapped or ImGuiHoveredFlags.AllowWhenBlockedByActiveItem)
    }

    fun placeNew(header: HeaderBlock, offset: Pos2D) {
        var i = 1
        while (code.blocks.blocks.any { it.id == i }) i++
        val p = fromScreenSpace(ImGui.getMousePos()) - offset
        val block = Block(
            header.name, i,
            p.x.toFloat(), p.y.toFloat()
        )
        for (hollow in header.hollow) {
            block.innercode.add(InnerCode(hollow.name))
        }
        code.blocks.blocks.add(block)
    }

    open fun bgButton(btn: Boolean) {
        if (btn) {
            activeComponents.clear()
            val placing = KonigEditor.placingBlock
            if (placing != null) {
                if (!ImGui.isKeyDown(ImGuiKey.LeftCtrl)) {
                    KonigEditor.placingBlock = null
                }
                placeNew(placing.header, placing.size / 2.0)
            }
        }
    }

    fun drawBgLines(draw: ImDrawList, topLeftPos: Pos2D, bottomRightPos: Pos2D) {
        val lineColor = ABGR(0xFF3F3F3F).col
        val spacing = if (scale > 50) 5.0 else if (scale > 20) 10.0 else 50.0
        val xRange = (ceil(topLeftPos.x / spacing).toInt() until ceil(bottomRightPos.x / spacing).toInt()).toList()
        val yRange = (ceil(topLeftPos.y / spacing).toInt() until ceil(bottomRightPos.y / spacing).toInt()).toList()
        val lAlt = ImGui.isKeyDown(ImGuiKey.LeftAlt)
        for (i in xRange) {
            val startPos = Pos2D(i * spacing, topLeftPos.y)
            val endPos = Pos2D(startPos.x, bottomRightPos.y)
            draw.addLine(startPos.screenPos(), endPos.screenPos(), lineColor, 2f)
            if (lAlt) {
                draw.addText(ImGui.getFont(), ImGui.getFontSize(), startPos.screenPos(), -1, "%.0f".format(i * spacing))
            }
        }
        for (i in yRange) {
            val startPos = Pos2D(topLeftPos.x, i * spacing)
            val endPos = Pos2D(bottomRightPos.x, startPos.y)
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

        if (ImGui.isKeyPressed(ImGuiKey.Escape)) {
            activeComponents.clear()
            if (KonigEditor.placingBlock != null) {
                KonigEditor.placingBlock = null
            }
        }

        drawBg()
        drawCode()

        ImGui.dummy(0f, 0f)

        ImGui.endGroup()
    }

    fun fromScreenSpace(vec: ImVec2): Pos2D {
        return ((vec.copy() - screenPos) / scale.toFloat()).toPos2D() + pos
    }

    fun Pos2D.screenPos(): ImVec2 {
        return ImVec2((x - pos.x).toFloat(), (y - pos.y).toFloat()) * scale.toFloat() + screenPos
    }

    fun Pos2D.screenSize(): ImVec2 {
        return ImVec2(x.toFloat(), y.toFloat()) * scale.toFloat()
    }

}
