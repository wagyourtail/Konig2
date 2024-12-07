package xyz.wagyourtail.konig.editor.helper

class CachedValue<T>(val compute: () -> T) {

    var value: T? = null

    fun invalidate() {
        value = null
    }

    fun get(): T {
        if (value == null) {
            value = compute()
        }
        return value!!
    }

}