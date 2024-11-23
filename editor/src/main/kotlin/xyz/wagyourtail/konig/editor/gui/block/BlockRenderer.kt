package xyz.wagyourtail.konig.editor.gui.canvas

import imgui.ImDrawList
import imgui.ImVec2
import imgui.flag.ImGuiKey
import imgui.internal.ImGui
import xyz.wagyourtail.commonskt.utils.mutliAssociate
import xyz.wagyourtail.konig.editor.helper.ImageRegistry
import xyz.wagyourtail.konig.editor.helper.div
import xyz.wagyourtail.konig.editor.helper.times
import xyz.wagyourtail.konig.structure.HeaderBlock
import xyz.wagyourtail.konig.structure.HeaderBlockIOField
import xyz.wagyourtail.konig.structure.Justify
import xyz.wagyourtail.konig.structure.Side
import kotlin.math.max

open class BlockRenderer(val header: HeaderBlock, val id: String) {

    val ports = header.io.ports.mutliAssociate { it.side to it }.mapValues { it.value.mutliAssociate { it.justify to it } }

    val bgId = "${header.name}##${id}"

    fun portId(port: HeaderBlockIOField) = "##block-${header.name}#${id}#${port.name}"


    open val scale = 50f

    fun ImVec2.toScreenSpace(): ImVec2 {
        return this * scale
    }

    fun ImVec2.toCanvasSpace(): ImVec2 {
        return this / scale
    }

    val minWidth = run {
        var width = 0f
        for (side in listOf(Side.TOP, Side.BOTTOM)) {
            val count = ports[side]?.values?.sumOf { it.size } ?: 0
            val minDim = count * .2f + .2f
            width = max(width, minDim)
        }
        width
    }

    val minHeight = run {
        var height = 0f
        for (side in listOf(Side.LEFT, Side.RIGHT)) {
            val count = ports[side]?.values?.sumOf { it.size } ?: 0
            val minDim = count * .2f + .2f
            height = max(height, minDim)
        }
        height
    }

    var width = max(1f, minWidth)
        set(value) {
            field = max(value, minWidth)
            portOffsetLocations = null
        }

    var height = max(1f, minHeight)
        set(value) {
            field = max(value, minHeight)
            portOffsetLocations = null
        }

    open fun draw() {
        val currentPos = ImGui.getCursorPos() +
                ImVec2(0f, ImGui.getFrameHeight())
        drawBg(currentPos)
        drawPorts(currentPos)
    }

    var portOffsetLocations: Map<HeaderBlockIOField, ImVec2>? = null
        get() {
            return if (field == null) {
                computePortOffsets().also {
                    field = it
                }
            } else {
                field
            }
        }


    open fun computePortOffsets(): Map<HeaderBlockIOField, ImVec2> {
        return buildMap {
            for (side in Side.entries) {
                val begin = when (side) {
                    Side.TOP -> ImVec2(0f, 0f)
                    Side.BOTTOM -> ImVec2(0f, height)
                    Side.LEFT -> ImVec2(0f, 0f)
                    Side.RIGHT -> ImVec2(width, 0f)
                }
                for (justify in Justify[side]) {
                    val p = ports[side]?.get(justify)
                    if (p.isNullOrEmpty()) continue
                    when (justify) {
                        Justify.TOP -> {
                            for ((i, port) in p.withIndex()) {
                                val lPos = i * .2f + .1f
                                put(port, ImVec2(0f, lPos) + begin)
                            }
                        }
                        Justify.BOTTOM -> {
                            for ((i, port) in p.reversed().withIndex()) {
                                val lPos = i * .2f + .1f
                                put(port, ImVec2(0f, height - lPos) + begin)
                            }
                        }
                        Justify.RIGHT -> {
                            for ((i, port) in p.reversed().withIndex()) {
                                val lPos = i * .2f + .1f
                                put(port, ImVec2(width - lPos, 0f) + begin)
                            }
                        }
                        Justify.LEFT -> {
                            for ((i, port) in p.withIndex()) {
                                val lPos = i * .2f + .1f
                                put(port, ImVec2(lPos, 0f) + begin)
                            }
                        }
                        Justify.CENTER -> {
                            val offset = (p.size - 1) * .1f
                            val firstPos = when (side) {
                                Side.TOP, Side.BOTTOM -> ImVec2(width / 2f - offset, 0f) + begin
                                Side.LEFT, Side.RIGHT -> ImVec2(0f, height / 2f - offset) + begin
                            }
                            for ((i, port) in p.withIndex()) {
                                val lPos = i * .2f
                                put(port, when (side) {
                                    Side.TOP, Side.BOTTOM -> ImVec2(lPos, 0f) + firstPos
                                    Side.LEFT, Side.RIGHT -> ImVec2(0f, lPos) + firstPos
                                })
                            }
                        }
                    }
                }
            }
        }
    }

    val portRadius = .05f

    open fun drawPorts(pos: ImVec2) {
        val draw = ImGui.getWindowDrawList()
        val offset = ImVec2(0f, ImGui.getFrameHeight())
        for ((port, portPos) in portOffsetLocations!!) {
            val centerPos = portPos.toScreenSpace() + pos
            drawPort(port, centerPos, draw)
            ImGui.setCursorPos(ImVec2(-portRadius, -portRadius).toScreenSpace() + centerPos - offset)
            portButton(port, ImGui.invisibleButton(portId(port), ImVec2(portRadius * 2, portRadius * 2).toScreenSpace()))
        }
    }

    open fun drawPort(inp: HeaderBlockIOField, pos: ImVec2, draw: ImDrawList) {
        draw.addCircleFilled(pos, portRadius * scale, 0xFFFFFFFF.toInt())
    }

    fun drawBg(pos: ImVec2) {
        val draw = ImGui.getWindowDrawList()
        val imageId = header.image?.let { ImageRegistry[it] }
        val size = ImVec2(width, height).toScreenSpace()
        bgButton(if (imageId != null) {
            ImGui.imageButton(imageId.toLong(), size)
        } else {
            draw.addRectFilled(pos, ImVec2(width, height).toScreenSpace() + pos, 0xFFFF0000.toInt())
            ImGui.invisibleButton(bgId, ImVec2(width, height).toScreenSpace())
        })
        val lAlt = ImGui.isKeyDown(ImGuiKey.LeftAlt)
        if (lAlt || imageId == null) {
            val textSize = ImGui.calcTextSize(header.name)
            if (!lAlt) {
                ImGui.pushClipRect(pos, ImVec2(pos) + size, true)
            }
            draw.addText(ImVec2(width / 2, height / 2).toScreenSpace() + pos - textSize / 2f, 0xFFFFFFFF.toInt(), header.name)
            if (!lAlt) {
                ImGui.popClipRect()
            }
        }
    }

    open fun bgButton(btn: Boolean) {
        ImGui.setItemAllowOverlap()
        if (btn) {
            onBgClicked()
        }
    }

    open fun portButton(inp: HeaderBlockIOField, btn: Boolean) {
        if (btn) {
            onPortClicked(inp)
        }
    }

    open fun onBgClicked() {

    }

    open fun onPortClicked(inp: HeaderBlockIOField) {

    }

}