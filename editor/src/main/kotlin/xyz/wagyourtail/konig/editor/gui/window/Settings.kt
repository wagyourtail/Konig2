package xyz.wagyourtail.konig.editor.gui.window

import imgui.ImGui
import imgui.type.ImFloat
import imgui.type.ImInt
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import xyz.wagyourtail.konig.editor.gui.KonigEditor
import xyz.wagyourtail.konig.editor.gui.settings.RootSettingGroup
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

@OptIn(ExperimentalSerializationApi::class)
object Settings : RootSettingGroup() {
    private val configLocation = Paths.get("./konigeditor.conf")

    fun save() {
        configLocation.outputStream().use {
            Json.encodeToStream<JsonElement>(toJson(), it)
        }
    }

    var resetUI: Boolean by setting(true, immediateApply = true)

    var recentOpened: List<Path> by setting(listOf())

    object General : Group("general") {

        object Appearance : Group("appearance") {
            var scale: Float by setting(1f) { key, temp ->
                val value = ImFloat(temp)
                ImGui.inputFloat(key.imguiString(), value, .5f)
                if (value.get() < .5f) value.set(.5f)
                scale = value.get()
            }

            private val availableFonts = (KonigEditor.availableFonts.keys + "default").sorted().toTypedArray()
            var font: String by setting("default") { key, temp ->
                val currentFont = availableFonts.binarySearch(temp)
                val value = ImInt(currentFont)
                ImGui.combo(key.imguiString(), value, availableFonts)
                if (currentFont != value.get()) {
                    font = availableFonts[value.get()]
                }
            }

            var fontSize: Float by setting(13f) { key, temp ->
                val fontSizeValue = ImFloat(temp)
                ImGui.inputFloat(key.imguiString(), fontSizeValue, 1f)
                if (fontSizeValue.get() < 6f) fontSizeValue.set(6f)
                fontSize = fontSizeValue.get()
            }
        }
    }

    object Editor : Group("editor") {

        object Canvas : Group("canvas") {

            var snapToGrid: Boolean by setting(true) { key, temp ->
                if (ImGui.checkbox(key.imguiString(), temp)) {
                    snapToGrid = !temp
                }
            }

            var gridSize: Float by setting(.1f) { key, temp ->
                val value = IntArray(1)
                value[0] = (temp * 50).toInt()
                ImGui.sliderInt(key.imguiString(), value, 1, 50, "%.2f".format(temp))
                if (value[0] == 0) value[0] = 1
                gridSize = value[0] / 50f
            }

        }

    }

    fun load() {
        ensureInit()
        if (configLocation.exists()) {
            configLocation.inputStream().use {
                fromJson(Json.decodeFromStream<JsonElement>(it))
            }
        }
    }

}