package xyz.wagyourtail.konig.editor.lang

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.util.*

@OptIn(ExperimentalSerializationApi::class)
object L10N {

    private val eng = "/lang/en_us.json5"
    private var currentLang = "/lang/${Locale.getDefault()}.json5".lowercase()

    private var translationMap: Map<String, String>? = null
    private var translatableCache = mutableMapOf<String, TranslatedString>()

    private val jsonReader = Json {
        isLenient = true
        allowTrailingComma = true
    }

    private fun readLangFile(path: String) = L10N::class.java.getResourceAsStream(path)?.use {
        jsonReader.decodeFromStream<Map<String, String>>(it)
    }

    private fun initialize() {
        translationMap = buildMap {
            putAll(readLangFile(eng)!!)
            readLangFile(currentLang)?.let { putAll(it) }
        }
    }

    fun translate(key: String): TranslatedString {
        if (translationMap == null) {
            initialize()
        }
        return translatableCache.getOrPut(key) {
            TranslatedString(key,)
        }
    }

    class TranslatedString(
        val key: String
    ) {
        private var currentLang: String = "unknown"
        var value: String = translationMap!!.getOrDefault(key, key)
            get() {
                if (currentLang != L10N.currentLang) {
                    field = translationMap!!.getOrDefault(key, key)
                    currentLang = L10N.currentLang
                }
                return field
            }

        fun imguiString() = "$value###$key"

        override fun toString() = value

    }

}