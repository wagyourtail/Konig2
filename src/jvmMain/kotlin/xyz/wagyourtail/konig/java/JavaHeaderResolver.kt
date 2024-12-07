package xyz.wagyourtail.konig.java

import kotlinx.serialization.decodeFromString
import xyz.wagyourtail.konig.headers.HeaderResolver
import xyz.wagyourtail.konig.structure.Headers
import xyz.wagyourtail.konig.xmlReader
import java.nio.charset.StandardCharsets

class JavaHeaderResolver : HeaderResolver() {

    override fun resolveIntern(name: String): Headers {
        return JavaHeaderResolver::class.java.getResourceAsStream("/$name.konig")?.use { stream ->
            xmlReader.decodeFromString<Headers>(stream.readBytes().toString(StandardCharsets.UTF_8))
        } ?: error("Could not find intern headers for $name")
    }

    override fun resolveSrc(path: String): Headers {
        TODO("Not yet implemented")
    }

}