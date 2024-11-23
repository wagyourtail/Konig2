package xyz.wagyourtail.konig.editor.gui.settings

import imgui.internal.ImGui
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import xyz.wagyourtail.konig.editor.lang.L10N
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class SettingsGroup(
    val key: String,
) {
    abstract val root: RootSettingGroup
    private val translateValue = L10N.translate(key)
    @PublishedApi
    internal val settings = mutableMapOf<String, Setting<*>>()
    private val subGroups: MutableList<SettingsGroup> = mutableListOf()

    fun renderSubtree() {
        val open = ImGui.treeNode(key)
        if (ImGui.isItemClicked()) {

        }
    }

    open fun renderContent() {

    }

    fun render() {

    }

    fun apply() {
        for (value in settings.values) {
            value.apply()
        }
        for (subGroup in subGroups) {
            subGroup.apply()
        }
    }

    inline fun <reified T> setting(name: String, default: T, transient: Boolean = false, noinline render: (L10N.TranslatedString) -> Unit): Setting<T> {
        return Setting(
            name,
            default,
            transient,
            render,
            { Json.decodeFromJsonElement<T>(it) },
            { Json.encodeToJsonElement(it) },
        ).also {
            settings[it.key] = it
        }
    }

    inline fun

}

value class Group(val settingGroup: SettingsGroup) {
    
}

data class Setting<T>(
    val key: String,
    val default: T,
    val transient: Boolean,
    val render: (L10N.TranslatedString) -> Unit,
    val decodeFunction: (JsonElement) -> T,
    val encodeJson: (T) -> JsonElement,
) : ReadWriteProperty<Any?, T> {
    var value: T = default
    var temp: T = default

    val translateValue = L10N.translate(key)

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        temp = value
    }

    fun render() {
        render(translateValue)
    }

    fun apply() {
        value = temp
    }

    fun readJson(json: JsonElement) {
        temp = decodeFunction(json)
    }

    fun writeJson(): JsonElement {
        return encodeJson(value)
    }

}