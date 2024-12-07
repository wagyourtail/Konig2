package xyz.wagyourtail.konig

import okio.BufferedSource

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object ResourceLoader {

    fun resolveSrc(path: String): BufferedSource?

    fun resolveOnlyInternal(path: String): BufferedSource?

    fun resolveOnlyExternal(path: String): BufferedSource?

}