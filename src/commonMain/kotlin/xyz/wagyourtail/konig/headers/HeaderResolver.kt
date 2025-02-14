package xyz.wagyourtail.konig.headers

import xyz.wagyourtail.commonskt.collection.defaultedMapOf
import xyz.wagyourtail.konig.structure.HeaderBlock
import xyz.wagyourtail.konig.structure.Headers
import xyz.wagyourtail.konig.structure.Include

abstract class HeaderResolver {

    private val _byName = mutableMapOf<String, HeaderBlock>()
    private val _byGroup = defaultedMapOf<String, MutableMap<String, HeaderBlock>> { mutableMapOf() }

    private val internsRead = mutableSetOf<String>()

    val byName: Map<String, HeaderBlock> get() = _byName
    val byGroup: Map<String, Map<String, HeaderBlock>> get() = _byGroup

    fun readHeaders(headers: Headers) {
        val version = headers.version
        for (header in headers.headers) {
            when (header) {
                is HeaderBlock -> {
                    _byName[header.name] = header
                    _byGroup[header.group][header.name] = header
                }
                is Include -> {
                    when {
                        header.intern != null -> {
                            if (!internsRead.contains(header.intern)) {
                                readHeaders(resolveIntern(header.intern))
                                internsRead.add(header.intern)
                            }
                        }
                        header.src != null -> readHeaders(resolveSrc(header.src))
                        else -> throw IllegalStateException("expected include to have `src` or `intern` set")
                    }
                }
            }
        }
    }

    fun getValue(name: String): HeaderBlock {
        return byName.getValue(name)
    }

    operator fun get(name: String): HeaderBlock? {
        return byName[name]
    }

    abstract fun availableIntern(): List<String>

    abstract fun resolveIntern(name: String): Headers

    abstract fun resolveSrc(path: String): Headers

    fun clear() {
        _byName.clear()
        _byGroup.clear()
        internsRead.clear()
    }

}