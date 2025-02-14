package xyz.wagyourtail.konig

import kotlinx.serialization.encodeToString
import xyz.wagyourtail.konig.structure.*

fun main() {
    println(XML.encodeToString(Examples.countToTen))
}