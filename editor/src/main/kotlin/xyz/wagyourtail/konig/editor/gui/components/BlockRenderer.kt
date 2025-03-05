package xyz.wagyourtail.konig.editor.gui.components

import imgui.ImDrawList
import imgui.ImGui
import imgui.ImVec2
import imgui.internal.flag.ImGuiButtonFlags
import imgui.flag.ImGuiKey
import xyz.wagyourtail.commonskt.collection.DefaultMap
import xyz.wagyourtail.commonskt.collection.defaultedMapOf
import xyz.wagyourtail.commonskt.position.Pos2D
import xyz.wagyourtail.commonskt.position.max
import xyz.wagyourtail.commonskt.utils.mutliAssociate
import xyz.wagyourtail.konig.editor.ABGR
import xyz.wagyourtail.konig.editor.gui.components.BlockRenderer.HollowRenderer
import xyz.wagyourtail.konig.editor.helper.*
import xyz.wagyourtail.konig.structure.*
import kotlin.math.max

abstract class BlockRenderer<T: HollowRenderer>(val header: HeaderBlock, val id: String) {

    open val ports = header.io.ports.mutliAssociate { it.side to it }.mapValues { it.value.mutliAssociate { it.justify to it } }

    val bgId = "${header.name}##${id}"
    open val noButtons = false

    fun portId(port: HeaderBlockIOField) = "##block-${header.name}#${id}#${port.name}"

    open var scale = 50.0
    var screenPos = ImVec2()
    var screenPosWithScroll = ImVec2()

    val hollowsByGroup: Map<String, List<Hollow>> = header.hollow.mutliAssociate { hollow ->
        hollow.groupId to hollow
    }

    val activeHollows = defaultedMapOf<String, Hollow> { group ->
        if (group.startsWith("ungrouped|")) {
            header.hollow.single { it.groupId == group }
        } else {
            header.hollow.first { it.group == group }
        }
    }

    abstract val hollowRenderers: DefaultMap<Hollow, T>

    var hollowLocations: CachedValue<Map<String, Double>> = CachedValue {
        var maxX = 0.0
        var yPos = .1
        val yLocs = mutableMapOf<String, Double>()
        val entries = hollowsByGroup.entries
        for ((index, entry) in entries.withIndex()) {
            val (id, group) = entry
            yLocs[id] = yPos
            var minSize = Pos2D.ZERO
            for (hollow in group) {
                minSize = max(hollowRenderers[hollow].minSize, minSize)
            }
            maxX = max(maxX, minSize.x)
            yPos += minSize.y + .1f
        }
        yLocs
    }

    var minSize: CachedValue<Pos2D> = CachedValue {
        var width = .1
        for (side in listOf(Side.TOP, Side.BOTTOM)) {
            val count = ports[side]?.values?.sumOf { it.size } ?: 0
            val minDim = count * .2 + .2
            width = max(width, minDim)
        }
        var height = .1
        for (side in listOf(Side.LEFT, Side.RIGHT)) {
            val count = ports[side]?.values?.sumOf { it.size } ?: 0
            val minDim = count * .2 + .2
            height = max(height, minDim)
        }
        val loc = hollowLocations.get().entries.maxByOrNull { it.value }
        if (loc != null) {
            val rend = hollowRenderers[hollowsByGroup.getValue(loc.key).first()]
            width = max(rend.minSize.x + .2, width)
            height = max(height, loc.value + rend.minSize.y + .1)
        }
        Pos2D(width, height)
    }

    open var size: Pos2D = Pos2D(1.0, 1.0)
        set(value) {
            portOffsetLocations.invalidate()
            hollowLocations.invalidate()
            minSize.invalidate()
            field = max(minSize.get(), value)
        }

    open fun process() {
        screenPos = ImGui.getCursorPos() + ImVec2(0f, ImGui.getFrameHeight())
        screenPosWithScroll = ImGui.getCursorScreenPos()
        drawBg()
        val cursor = ImGui.getCursorPos()
        drawPorts()
        drawHollows()
        val draw = ImGui.getWindowDrawList()
        val lAlt = ImGui.isKeyDown(ImGuiKey.LeftAlt)
        if (lAlt) {
            drawName(draw)
        }
        ImGui.setCursorPos(cursor)
    }

    fun drawHollows() {
        val pos = Pos2D(0.1, 0.0)
        val locs = hollowLocations.get().entries.toList()
        for ((idx, grp) in locs.withIndex()) {
            val (group, loc) = grp
            val p = pos + Pos2D(0.0, loc)
            ImGui.setCursorPos(p.screenPos())
            val active = hollowRenderers[activeHollows[group]]
            active.size = Pos2D(
                size.x - .2f,
                if (idx == locs.size - 1) {
                    size.y - loc - .1f
                } else {
                    locs[idx + 1].value - .1f - loc
                }
            )
            active.process()
        }
    }

    var portOffsetLocations: CachedValue<Map<HeaderBlockIOField, Pos2D>> = CachedValue {
        buildMap {
            for (side in Side.entries) {
                val begin = when (side) {
                    Side.TOP -> Pos2D.ZERO
                    Side.BOTTOM -> Pos2D(0.0, this@BlockRenderer.size.y)
                    Side.LEFT -> Pos2D.ZERO
                    Side.RIGHT -> Pos2D(this@BlockRenderer.size.x, 0.0)
                }
                for (justify in Justify[side]) {
                    val p = ports[side]?.get(justify)
                    if (p.isNullOrEmpty()) continue
                    when (justify) {
                        Justify.TOP -> {
                            for ((i, port) in p.withIndex()) {
                                val lPos = i * .2 + .1
                                put(port, Pos2D(0.0, lPos) + begin)
                            }
                        }
                        Justify.BOTTOM -> {
                            for ((i, port) in p.reversed().withIndex()) {
                                val lPos = i * .2 + .1
                                put(port, Pos2D(0.0, this@BlockRenderer.size.y - lPos) + begin)
                            }
                        }
                        Justify.RIGHT -> {
                            for ((i, port) in p.reversed().withIndex()) {
                                val lPos = i * .2 + .1
                                put(port, Pos2D(this@BlockRenderer.size.x - lPos, 0.0) + begin)
                            }
                        }
                        Justify.LEFT -> {
                            for ((i, port) in p.withIndex()) {
                                val lPos = i * .2 + .1
                                put(port, Pos2D(lPos, 0.0) + begin)
                            }
                        }
                        Justify.CENTER -> {
                            val offset = (p.size - 1) * .1f
                            val firstPos = when (side) {
                                Side.TOP, Side.BOTTOM -> Pos2D(this@BlockRenderer.size.x / 2f - offset, 0.0) + begin
                                Side.LEFT, Side.RIGHT -> Pos2D(0.0, this@BlockRenderer.size.y / 2f - offset) + begin
                            }
                            for ((i, port) in p.withIndex()) {
                                val lPos = i * .2
                                put(port, when (side) {
                                    Side.TOP, Side.BOTTOM -> Pos2D(lPos, 0.0) + firstPos
                                    Side.LEFT, Side.RIGHT -> Pos2D(0.0, lPos) + firstPos
                                })
                            }
                        }
                    }
                }
            }
        }
    }

    val portRadius = .05

    open fun drawPorts() {
        val draw = ImGui.getWindowDrawList()
        val offset = ImVec2(0f, ImGui.getFrameHeight())
        for ((port, portPos) in portOffsetLocations.get()) {
            val centerPos = portPos.screenPos()
            drawPort(port, centerPos, draw)
            if (!noButtons) {
                ImGui.setCursorPos(
                    Pos2D(-portRadius, -portRadius).screenSize()
                    + centerPos
                    - offset
                    - screenPosWithScroll
                    + screenPos
                )
                portButton(
                    port,
                    ImGui.invisibleButton(portId(port), Pos2D(portRadius * 2, portRadius * 2).screenSize())
                )
            }
        }
    }


    open fun drawPort(inp: HeaderBlockIOField, pos: ImVec2, draw: ImDrawList) {
        draw.addCircleFilled(pos, (portRadius * scale).toFloat(), 0xFFFFFFFF.toInt())
    }

    open fun drawName(draw: ImDrawList) {
        val textSize = ImGui.calcTextSize(header.name)
        draw.addText(size.screenSize() / 2f + screenPosWithScroll - textSize / 2f, 0xFFFFFFFF.toInt(), header.name)
    }

    open fun drawBg() {
        val draw = ImGui.getWindowDrawList()
        val imageId = header.image?.let { ImageRegistry[it] }
        if (imageId != null) {
            draw.addImage(imageId.toLong(), Pos2D.ZERO.screenPos(), size.screenPos())
        } else {
            draw.addRectFilled(Pos2D.ZERO.screenPos(), size.screenPos(), 0xFFFF0000.toInt())
        }
        if (!noButtons) {
            ImGui.setNextItemAllowOverlap()
            bgButton(
                ImGui.invisibleButton(
                    bgId,
                    size.screenSize(),
                    ImGuiButtonFlags.MouseButtonMask_ or ImGuiButtonFlags.PressedOnClick
                )
            )
        }
        val lAlt = ImGui.isKeyDown(ImGuiKey.LeftAlt)
        if (!lAlt && imageId == null) {
            draw.pushClipRect(Pos2D.ZERO.screenPos(), size.screenPos(), true)
            drawName(draw)
            draw.popClipRect()
        }
    }

    open fun bgButton(btn: Boolean) {
    }

    open fun portButton(inp: HeaderBlockIOField, btn: Boolean) {
    }

    fun Pos2D.screenPos(): ImVec2 {
        return ImVec2(x.toFloat(), y.toFloat()) * scale.toFloat() + screenPosWithScroll
    }

    fun Pos2D.screenSize(): ImVec2 {
        return ImVec2(x.toFloat(), y.toFloat()) * scale.toFloat()
    }

    interface HollowRenderer {

        val minSize: Pos2D
        var size: Pos2D

        fun process()

    }

}