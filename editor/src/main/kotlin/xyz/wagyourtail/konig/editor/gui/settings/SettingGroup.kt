package xyz.wagyourtail.konig.editor.gui.settings

import imgui.internal.ImGui
import kotlinx.serialization.json.*
import xyz.wagyourtail.konig.editor.lang.L10N
import java.lang.invoke.MethodHandles
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

sealed class SettingGroup(
    val key: String,
) {
    companion object {
        private val lookup = MethodHandles.lookup()
    }
    abstract val root: RootSettingGroup

    protected val translateValue = L10N.translate(key)
    @PublishedApi
    internal val settings = mutableMapOf<String, Setting<*>>()
    protected val subGroups = mutableMapOf<String, Group>()

    open val allSettings: Set<Setting<*>> by lazy {
        (subGroups.values.flatMap { it.allSettings } + settings.values).toSet()
    }

    fun ensureInit() {
        this::class.nestedClasses.forEach {
            lookup.ensureInitialized(it.java)
        }
        this.subGroups.values.forEach { it.ensureInit() }
    }

    open fun renderSubtree() {
        subGroups.values.forEach {
            val open = ImGui.treeNode(it.translateValue.imguiString())
            if (ImGui.isItemClicked()) {
                root.active = it
            }
            if (open) {
                it.renderSubtree()
                ImGui.treePop()
            }
        }
    }

    open fun renderContent() {
        for (value in settings.values) {
            value.render()
        }
    }

    fun apply() {
        for (value in settings.values) {
            value.apply()
        }
        for (subGroup in subGroups.values) {
            subGroup.apply()
        }
    }

    fun fromJson(json: JsonElement) {
        if (json !is JsonObject) error("element is not object")
        for ((key, value) in json) {
            if (key in subGroups) {
                subGroups.getValue(key).fromJson(value)
            }
            if (key in settings) {
                settings.getValue(key).readJson(value)
            }
        }
    }

    fun toJson(): JsonElement {
        return buildJsonObject {
            for ((key, value) in subGroups) {
                put(key, value.toJson())
            }
            for ((key, value) in settings) {
                if (value.transient) continue
                put(key, value.writeJson())
            }
        }
    }

    inline fun <reified T> setting(default: T, transient: Boolean = false, immediateApply: Boolean = false, noinline render: (L10N.TranslatedString, T) -> Unit = { _, _ -> }): SettingProvider<T> {
        return SettingProvider(
            this,
            default,
            transient,
            immediateApply,
            render,
            { Json.decodeFromJsonElement<T>(it) },
            { Json.encodeToJsonElement(it) },
        )
    }

    inline fun <reified T> setting(name: String, default: T, transient: Boolean = false, immediateApply: Boolean = false, noinline render: (L10N.TranslatedString, T) -> Unit = { _, _ -> }): Setting<T> {
        return Setting(
            "$key.$name",
            default,
            transient,
            immediateApply,
            render,
            { Json.decodeFromJsonElement<T>(it) },
            { Json.encodeToJsonElement(it) },
        ).also {
            settings[name] = it
        }
    }

    open inner class Group(key: String) : SettingGroup("${this@SettingGroup.key}.$key") {
        override val root: RootSettingGroup
            get() = this@SettingGroup.root

        init {
            this@SettingGroup.subGroups[key] = this
        }
    }

}

class SettingProvider<T>(
    val group: SettingGroup,
    val default: T,
    val transient: Boolean,
    val immediateApply: Boolean,
    private val render: (L10N.TranslatedString, T) -> Unit,
    private val decodeJson: (JsonElement) -> T,
    private val encodeJson: (T) -> JsonElement
) {

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): Setting<T> {
        return Setting(
            "${group.key}.${property.name}",
            default,
            transient,
            immediateApply,
            render,
            decodeJson,
            encodeJson
        ).also {
            group.settings[property.name] = it
        }
    }

}

data class Setting<T>(
    val key: String,
    val default: T,
    val transient: Boolean,
    val immediateApply: Boolean,
    private val render: (L10N.TranslatedString, T) -> Unit,
    private val decodeJson: (JsonElement) -> T,
    private val encodeJson: (T) -> JsonElement,
) : ReadWriteProperty<Any?, T> {
    var value: T = default
    var temp: T = default

    val translateValue = L10N.translate(key)

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        temp = value
        if (immediateApply) {
            apply()
        }
    }

    fun render() {
        render(translateValue, temp)
    }

    fun apply() {
        value = temp
    }

    fun reset() {
        temp = value
    }

    fun readJson(json: JsonElement) {
        temp = decodeJson(json)
    }

    fun writeJson(): JsonElement {
        return encodeJson(value)
    }

}