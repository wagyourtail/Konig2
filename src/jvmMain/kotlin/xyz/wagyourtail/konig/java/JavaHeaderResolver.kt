package xyz.wagyourtail.konig.java

import kotlinx.serialization.decodeFromString
import xyz.wagyourtail.konig.headers.HeaderResolver
import xyz.wagyourtail.konig.structure.Headers
import xyz.wagyourtail.konig.structure.XML
import java.nio.charset.StandardCharsets

class JavaHeaderResolver : HeaderResolver() {

    override fun availableIntern(): List<String> {
        return listOf("stdlib")
    }

    override fun resolveIntern(name: String): Headers {
        // TODO: replace with annotation reflection stuff
        return JavaHeaderResolver::class.java.getResourceAsStream("/$name.konig")?.use { stream ->
            XML.decodeFromString<Headers>(stream.readBytes().toString(StandardCharsets.UTF_8))
        } ?: error("Could not find intern headers for $name")
    }

    override fun resolveSrc(path: String): Headers {
        TODO("Not yet implemented")
    }

}