package xyz.wagyourtail.konig.editor.gui

import imgui.ImFontConfig
import imgui.ImGui.*
import imgui.ImGui.styleColorsDark
import imgui.app.Application
import imgui.app.Configuration
import imgui.flag.ImGuiConfigFlags
import imgui.flag.ImGuiDir
import imgui.flag.ImGuiKey
import imgui.flag.ImGuiMouseButton
import imgui.internal.ImGui
import imgui.type.ImInt
import kotlinx.serialization.decodeFromString
import xyz.wagyourtail.commons.core.OSUtils
import xyz.wagyourtail.konig.Examples
import xyz.wagyourtail.konig.editor.gui.window.BlockOptions
import xyz.wagyourtail.konig.editor.gui.components.BlockRenderer
import xyz.wagyourtail.konig.editor.gui.window.selector.NodeSelector
import xyz.wagyourtail.konig.editor.gui.window.Settings
import xyz.wagyourtail.konig.editor.gui.window.canvas.RootCanvas
import xyz.wagyourtail.konig.java.JavaHeaderResolver
import xyz.wagyourtail.konig.structure.CodeFile
import xyz.wagyourtail.konig.structure.XML
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.*
import kotlin.properties.Delegates

const val DEBUG_NOSAVE = false

object KonigEditor : Application() {
    var currentScale = 1f
    var currentFontSize = 13f
    var currentFont = "default"

    val resolver = JavaHeaderResolver()
    lateinit var canvas: RootCanvas
    val selectors = mutableListOf<NodeSelector>()

    var savePath: Path? = null

    var placingBlock: BlockRenderer<*>? = null

    val availableFonts by lazy {
        if (OSUtils.getOsId() == OSUtils.WINDOWS) {
            sequenceOf(
                Paths.get("C:\\Windows\\Fonts")
            )
        } else {
            sequenceOf(
                //TODO: implement per-os
                Paths.get("/usr/share/fonts/TTF"),
                Paths.get("/usr/local/share/fonts/TTF"),
                Paths.get(System.getProperty("user.home")).resolve(".local/share/fonts/TTF")
            )
        }.filter { it.exists() }
            .flatMap { it.walk() }
            .filter { it.extension.lowercase() == "ttf" }
            .sortedBy { it.name }
            .associateBy { it.nameWithoutExtension }
    }

    override fun configure(config: Configuration) {
        super.configure(config)
        Settings.load()
        config.title = "Konig Editor"
    }

    fun setFontGlobal(name: String, size: Float, earlyRun: Boolean = false) {
        val io = getIO()
        val fontConfig = ImFontConfig()

        io.fonts.clear()
        io.fonts.setFreeTypeRenderer(true)

        fontConfig.glyphRanges = io.fonts.glyphRangesDefault
        fontConfig.oversampleH = 2
        fontConfig.oversampleV = 1
        fontConfig.pixelSnapH = true

        var f = io.fonts.addFontDefault(fontConfig)
        f.scale = size / 13f
        if (name != "default" && availableFonts.containsKey(name)) {
            val font = availableFonts.getValue(name)
            f = io.fonts.addFontFromFileTTF(font.absolutePathString(), size, fontConfig)
            f.scale = currentScale * 16f / size
        }
        io.fontDefault = f

        if (!earlyRun) {
            imGuiGl3.destroyFontsTexture()
            io.fonts.build()
            imGuiGl3.createFontsTexture()
        }
    }

    override fun initImGui(config: Configuration) {
        super.initImGui(config)

        val io = getIO()

        if (DEBUG_NOSAVE) {
            io.iniFilename = null
        } else {
            io.iniFilename = "./konigeditor.ini"
        }

        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard)
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable)
//        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable)

        io.configViewportsNoTaskBarIcon = true
        io.wantCaptureKeyboard = true
        io.wantCaptureMouse = true
    }

    override fun preRun() {
        openCode(Examples.countToTen)
        for (group in resolver.byGroup.keys) {
            selectors.add(NodeSelector(group, resolver))
        }
    }

    fun openFile(path: Path) {
        openCode(XML.decodeFromString<CodeFile>(path.readText()))
        savePath = path
    }

    fun openExample(name: String) {
        openCode(Examples.byName[name] ?: error("Unknown example: $name"))
    }

    fun openCode(code: CodeFile) {
        savePath = null
        resolver.clear()
        selectors.clear()
        resolver.readHeaders(code.headers)
        canvas = RootCanvas(code.name, resolver, code.main)
        for (group in resolver.byGroup.keys) {
            selectors.add(NodeSelector(group, resolver))
        }
        Settings.resetUI = true
    }

    var dockId by Delegates.notNull<Int>()

    override fun startFrame() {
        if (currentScale != Settings.General.Appearance.scale) {
            getStyle().scaleAllSizes(Settings.General.Appearance.scale / currentScale)
//            getFont().scale *= config.scale / currentScale
            currentScale = Settings.General.Appearance.scale
        }
        if (currentFont != Settings.General.Appearance.font || currentFontSize != Settings.General.Appearance.fontSize) {
            currentFont = Settings.General.Appearance.font
            setFontGlobal(currentFont, Settings.General.Appearance.fontSize)
        }
        super.startFrame()
    }

    override fun preProcess() {
        styleColorsDark()
        dockId = dockSpaceOverViewport(getMainViewport())
    }

    override fun process() {
        MainMenu.process()
        canvas.process()
        selectors.forEach(NodeSelector::process)
        BlockOptions.process()
        Settings.process()
    }

    override fun postProcess() {
        if (Settings.resetUI) {
            ImGui.dockBuilderRemoveNodeChildNodes(dockId)
            val central = ImInt()
            val lower = ImInt()
            val right = ImInt()
            ImGui.dockBuilderSplitNode(dockId, ImGuiDir.Down, 0.3f, lower, central)
            ImGui.dockBuilderSplitNode(central.get(), ImGuiDir.Right, 0.3f, right, central)
            ImGui.dockBuilderDockWindow(canvas.windowId, central.get())
            for (sel in selectors) {
                ImGui.dockBuilderDockWindow(sel.windowId, lower.get())
            }
            ImGui.dockBuilderDockWindow("blockOptions", right.get())
            Settings.resetUI = false
            Settings.apply()
        }
    }

    override fun postRun() {
        if (!DEBUG_NOSAVE) {
            Settings.save()
        }
    }

}