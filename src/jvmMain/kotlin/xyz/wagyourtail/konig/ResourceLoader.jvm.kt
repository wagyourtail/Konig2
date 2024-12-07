package xyz.wagyourtail.konig

import okio.BufferedSource
import okio.buffer
import okio.source
import java.io.InputStream
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.inputStream

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object ResourceLoader {

    actual fun resolveSrc(path: String): BufferedSource? {
        return resolveOnlyInternal(path) ?: resolveOnlyExternal(path)
    }

    actual fun resolveOnlyInternal(path: String): BufferedSource? {
        return ResourceLoader::class.java.classLoader.getResourceAsStream(path)?.source()?.buffer()
    }

    actual fun resolveOnlyExternal(path: String): BufferedSource? {
        val p = Paths.get(path)
        return if (p.exists()) {
            p.inputStream().source().buffer()
        } else {
            null
        }
    }

}