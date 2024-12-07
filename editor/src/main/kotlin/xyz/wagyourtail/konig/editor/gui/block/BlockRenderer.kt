package xyz.wagyourtail.konig.editor.gui.block

import imgui.ImDrawList
import imgui.ImGui
import imgui.ImVec2
import imgui.flag.ImGuiKey
import xyz.wagyourtail.commonskt.collection.defaultedMapOf
import xyz.wagyourtail.commonskt.utils.mutliAssociate
import xyz.wagyourtail.konig.editor.ABGR
import xyz.wagyourtail.konig.editor.helper.*
import xyz.wagyourtail.konig.structure.*
import kotlin.math.max

open class BlockRenderer(val header: HeaderBlock, val id: String) {

    open val ports = header.io.ports.mutliAssociate { it.side to it }.mapValues { it.value.mutliAssociate { it.justify to it } }

    val bgId = "${header.name}##${id}"

    fun portId(port: HeaderBlockIOField) = "##block-${header.name}#${id}#${port.name}"

    open val scale = 50f
    var screenPos: ImVec2 = ImVec2()

    val hollowsByGroup: Map<String, List<Hollow>> = header.hollow.mutliAssociate { hollow ->
        hollow.groupId to hollow
    }

    val activeHollows = defaultedMapOf<String, Hollow> { group ->
        if (group.startsWith("ungrouped\$")) {
            header.hollow.single { it.groupId == group }
        } else {
            header.hollow.first { it.group == group }
        }
    }

    open val hollowRenderers = defaultedMapOf<Hollow, HollowRenderer> {
        object : HollowRenderer {
            override val minSize: Vec2d = Vec2d(.1, .1)
            override var size: Vec2d = minSize

            override fun process() {
                val draw = ImGui.getWindowDrawList()
                draw.addRectFilled(ImGui.getCursorPos(), ImGui.getCursorPos() + size.screenSize(), ABGR(255, 0, 0, 0).col)
            }
        }
    }

    var hollowLocations: CachedValue<Map<String, Double>> = CachedValue {
        var maxX = 0.0
        var yPos = .1
        val yLocs = mutableMapOf<String, Double>()
        val entries = hollowsByGroup.entries
        for ((index, entry) in entries.withIndex()) {
            val (id, group) = entry
            yLocs[id] = yPos
            var minSize = Vec2d()
            for (hollow in group) {
                minSize = max(hollowRenderers[hollow].minSize, minSize)
            }
            maxX = max(maxX, minSize.x)
            yPos += minSize.y + .1f
        }
        yLocs
    }

    val Hollow.groupId
        get() = if (group == null) { "ungrouped\$${name}" } else { group!! }

    var minSize: CachedValue<Vec2d> = CachedValue {
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
            val rend = hollowRenderers[hollowsByGroup[loc.key]!!.first()]
            width = max(rend.minSize.x + .2, width)
            height = max(height, loc.value + rend.minSize.y + .1)
        }
        Vec2d(width, height)
    }

    open var size: Vec2d = Vec2d(1f, 1f)
        set(value) {
            portOffsetLocations.invalidate()
            hollowLocations.invalidate()
            minSize.invalidate()
            field = max(minSize.get(), value)
        }

    open fun process() {
        screenPos = ImGui.getCursorPos() + ImVec2(0f, ImGui.getFrameHeight())
        drawBg()
        drawPorts()
        drawHollows()
    }

    fun drawHollows() {
        val pos = Vec2d(.1f, 0f)
        val locs = hollowLocations.get().entries.toList()
        for ((idx, grp) in locs.withIndex()) {
            val (group, loc) = grp
            val p = pos + Vec2d(0.0, loc)
            ImGui.setCursorPos(p.screenPos())
            val active = hollowRenderers[activeHollows[group]]
            active.size = Vec2d(
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

    var portOffsetLocations: CachedValue<Map<HeaderBlockIOField, Vec2d>> = CachedValue {
        buildMap {
            for (side in Side.entries) {
                val begin = when (side) {
                    Side.TOP -> Vec2d(0f, 0f)
                    Side.BOTTOM -> Vec2d(0.0, this@BlockRenderer.size.y)
                    Side.LEFT -> Vec2d(0f, 0f)
                    Side.RIGHT -> Vec2d(this@BlockRenderer.size.x, 0.0)
                }
                for (justify in Justify[side]) {
                    val p = ports[side]?.get(justify)
                    if (p.isNullOrEmpty()) continue
                    when (justify) {
                        Justify.TOP -> {
                            for ((i, port) in p.withIndex()) {
                                val lPos = i * .2f + .1f
                                put(port, Vec2d(0f, lPos) + begin)
                            }
                        }
                        Justify.BOTTOM -> {
                            for ((i, port) in p.reversed().withIndex()) {
                                val lPos = i * .2f + .1f
                                put(port, Vec2d(0.0, this@BlockRenderer.size.y - lPos) + begin)
                            }
                        }
                        Justify.RIGHT -> {
                            for ((i, port) in p.reversed().withIndex()) {
                                val lPos = i * .2f + .1f
                                put(port, Vec2d(this@BlockRenderer.size.x - lPos, 0.0) + begin)
                            }
                        }
                        Justify.LEFT -> {
                            for ((i, port) in p.withIndex()) {
                                val lPos = i * .2f + .1f
                                put(port, Vec2d(lPos, 0f) + begin)
                            }
                        }
                        Justify.CENTER -> {
                            val offset = (p.size - 1) * .1f
                            val firstPos = when (side) {
                                Side.TOP, Side.BOTTOM -> Vec2d(this@BlockRenderer.size.x / 2f - offset, 0.0) + begin
                                Side.LEFT, Side.RIGHT -> Vec2d(0.0, this@BlockRenderer.size.y / 2f - offset) + begin
                            }
                            for ((i, port) in p.withIndex()) {
                                val lPos = i * .2f
                                put(port, when (side) {
                                    Side.TOP, Side.BOTTOM -> Vec2d(lPos, 0f) + firstPos
                                    Side.LEFT, Side.RIGHT -> Vec2d(0f, lPos) + firstPos
                                })
                            }
                        }
                    }
                }
            }
        }
    }

    val portRadius = .05f

    open fun drawPorts() {
        val draw = ImGui.getWindowDrawList()
        val offset = ImVec2(0f, ImGui.getFrameHeight())
        for ((port, portPos) in portOffsetLocations.get()) {
            val centerPos = portPos.screenPos()
            drawPort(port, centerPos, draw)
            ImGui.setCursorPos(Vec2d(-portRadius, -portRadius).screenSize() + centerPos - offset)
            portButton(port, ImGui.invisibleButton(portId(port), Vec2d(portRadius * 2, portRadius * 2).screenSize()))
        }
    }

    open fun drawPort(inp: HeaderBlockIOField, pos: ImVec2, draw: ImDrawList) {
        draw.addCircleFilled(pos, portRadius * scale, 0xFFFFFFFF.toInt())
    }

    open fun drawBg() {
        val draw = ImGui.getWindowDrawList()
        val imageId = header.image?.let { ImageRegistry[it] }
        bgButton(if (imageId != null) {
            ImGui.imageButton(imageId.toLong(), size.screenSize())
        } else {
            draw.addRectFilled(Vec2d().screenPos(), size.screenPos(), 0xFFFF0000.toInt())
            ImGui.invisibleButton(bgId, size.screenSize())
        })
        val lAlt = ImGui.isKeyDown(ImGuiKey.LeftAlt)
        if (lAlt || imageId == null) {
            val textSize = ImGui.calcTextSize(header.name)
            if (!lAlt) {
                ImGui.pushClipRect(Vec2d().screenPos(), size.screenPos(), true)
            }
            draw.addText(size.screenSize() / 2f + screenPos - textSize / 2f, 0xFFFFFFFF.toInt(), header.name)
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

    fun Vec2d.screenPos(): ImVec2 {
        return ImVec2(x.toFloat(), y.toFloat()) * scale + screenPos
    }

    fun Vec2d.screenSize(): ImVec2 {
        return ImVec2(x.toFloat(), y.toFloat()) * scale
    }

    interface HollowRenderer {

        val minSize: Vec2d
        var size: Vec2d

        fun process()

    }

}