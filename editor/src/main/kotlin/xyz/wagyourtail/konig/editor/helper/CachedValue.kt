package xyz.wagyourtail.konig.editor.helper

class CachedValue<T>(vararg val alsoInvalidate: CachedValue<*>, val compute: () -> T) {

    var value: T? = null

    fun invalidate() {
        value = null
        for (cachedValue in alsoInvalidate) {
            cachedValue.invalidate()
        }
    }

    fun get(): T {
        if (value == null) {
            value = compute()
        }
        return value!!
    }

}