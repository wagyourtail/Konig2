package xyz.wagyourtail.konig.editor.lang

import kotlinx.serialization.json.Json
import java.util.*

object I10n {

    val eng = "/lang/en_us.json"
    var currentLang = "/lang/${Locale.getDefault()}.json".lowercase()

    var map: Map<String, String>? = null

    fun readFile(path: String) = I10n::class.java.getResourceAsStream(path)?.use { it.bufferedReader().readText() }

    fun readLangFile(path: String) = readFile(path)?.let { Json.decodeFromString<Map<String, String>>(it) }

    fun initialize() {
        map = buildMap {
            putAll(readLangFile(eng)!!)
            readLangFile(currentLang)?.let { putAll(it) }
        }
    }

    fun translate(key: String): String {
        if (map == null) {
            initialize()
        }
        return map!!.getOrDefault(key, key)
    }

    fun translateImGui(key: String): String {
        return translate(key) + "###" + key
    }

}