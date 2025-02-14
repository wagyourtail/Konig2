package xyz.wagyourtail.konig.structure

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlValue

const val VERSION = "1.0"
val XML = XML { recommended() }

@Serializable
@SerialName("code")
class CodeFile(
    val name: String,
    val version: String = VERSION,
    val headers: Headers,
    val main: Main
) {

    override fun toString(): String {
        return XML.encodeToString(this)
    }

}


@Serializable
@SerialName("headers")
class Headers(
    val version: String = VERSION,
    val headers: MutableList<HeaderChild>
) {

    constructor(version: String, vararg headers: HeaderChild): this(version, headers.toMutableList())

    constructor(vararg headers: HeaderChild): this(VERSION, headers.toMutableList())

    override fun toString(): String {
        return XML.encodeToString(this)
    }

}

@Serializable
sealed class HeaderChild

@Serializable
@SerialName("block")
class HeaderBlock(
    val name: String,
    val group: String,
    val generics: Generics? = null,
    val io: HeaderBlockIO,
    val hollow: List<Hollow>,
    val dynamicHollow: List<DynamicHollow>,
    val image: Image? = null,
) : HeaderChild() {

    override fun toString(): String {
        return XML.encodeToString(this)
    }

}

@Serializable
@SerialName("generics")
class Generics(
    val generics: List<Generic>
) {

    override fun toString(): String {
        return XML.encodeToString(this)
    }

}

@Serializable
@SerialName("generic")
class Generic(
    val name: String,
    val extends: String? = null,
) {

    override fun toString(): String {
        return XML.encodeToString(this)
    }

}

@Serializable
@SerialName("hollow")
class Hollow(
    val name: String,
    val group: String? = null,
    val io: List<HeaderBlockIOField>
) {

    val groupId
        get() = group ?: "ungrouped|$name"

    val byName by lazy {
        io.associateBy { it.name }
    }

    override fun toString(): String {
        return XML.encodeToString(this)
    }

}

@Serializable
@SerialName("dynamichollow")
class DynamicHollow(
    val group: String,
    val io: List<HeaderBlockIOField>,
) {

    val byName by lazy {
        io.associateBy { it.name }
    }

    override fun toString(): String {
        return XML.encodeToString(this)
    }

}

@Serializable
@SerialName("io")
class HeaderBlockIO(
    val ports: List<HeaderBlockIOField>,
) {
    val byName by lazy {
        ports.associateBy { it.name }
    }

    override fun toString(): String {
        return XML.encodeToString(this)
    }
}

enum class Side {
    @SerialName("top")
    TOP,
    @SerialName("bottom")
    BOTTOM,
    @SerialName("left")
    LEFT,
    @SerialName("right")
    RIGHT
    ;
}

enum class Justify {
    @SerialName("left")
    LEFT,
    @SerialName("top")
    TOP,
    @SerialName("center")
    CENTER,
    @SerialName("bottom")
    BOTTOM,
    @SerialName("right")
    RIGHT
    ;

    companion object {
        val bySide = buildMap {
            for (side in Side.entries) {
                put(
                    side, when (side) {
                        Side.TOP, Side.BOTTOM -> listOf(LEFT, CENTER, RIGHT)
                        Side.LEFT, Side.RIGHT -> listOf(TOP, CENTER, BOTTOM)
                    }
                )
            }
        }

        operator fun get(side: Side): List<Justify> {
            return bySide.getValue(side)
        }
    }

}

@Serializable
sealed class HeaderBlockIOField {
    abstract val name: String
    abstract val side: Side
    abstract val justify: Justify
    abstract val type: String
}

@Serializable
@SerialName("input")
class HeaderBlockInput(
    override val name: String,
    @XmlElement(false)
    override val side: Side,
    @XmlElement(false)
    override val justify: Justify,
    override val type: String,
    val optional: Boolean = false
) : HeaderBlockIOField() {

    override fun toString(): String {
        return XML.encodeToString(this)
    }

}

@Serializable
@SerialName("output")
class HeaderBlockOutput(
    override val name: String,
    @XmlElement(false)
    override val side: Side,
    @XmlElement(false)
    override val justify: Justify,
    override val type: String,
) : HeaderBlockIOField() {

    override fun toString(): String {
        return XML.encodeToString(this)
    }

}

@Serializable
@SerialName("image")
class Image(
    val src: String
) {

    override fun toString(): String {
        return XML.encodeToString(this)
    }

}

@Serializable
@SerialName("include")
class Include(
    val src: String? = null,
    val intern: String? = null
) : HeaderChild() {

    override fun toString(): String {
        return XML.encodeToString(this)
    }

}

sealed class Code {
    abstract val wires: Wires
    abstract val blocks: Blocks
}

@Serializable
@SerialName("main")
class Main(
    override val wires: Wires,
    override val blocks: Blocks
) : Code() {

    override fun toString(): String {
        return XML.encodeToString(this)
    }

}

@Serializable
@SerialName("wires")
class Wires(
    @XmlElement(true)
    val wires: MutableList<Wire>,
) {

    constructor(vararg wires: Wire) : this(wires.toMutableList())

    override fun toString(): String {
        return XML.encodeToString(this)
    }

}

@Serializable
@SerialName("blocks")
class Blocks(
    val blocks: MutableList<Block>
) {

    constructor(vararg blocks: Block) : this(blocks.toMutableList())

    override fun toString(): String {
        return XML.encodeToString(this)
    }

}

@Serializable
@SerialName("wire")
class Wire(
    val id: Int,
    val connections: MutableList<WireConnection>
) {

    constructor(id: Int, vararg connections: WireConnection) : this(id, connections.toMutableList())

    override fun toString(): String {
        return XML.encodeToString(this)
    }
}

@Serializable
sealed class WireConnection {
    abstract var x: Float
    abstract var y: Float
}

@Serializable
@SerialName("end")
class WireEnd(
    override var x: Float,
    override var y: Float,
    val block: Int,
    val port: String
) : WireConnection()

@Serializable
@SerialName("branch")
class WireBranch(
    override var x: Float,
    override var y: Float,
    val connections: MutableList<WireConnection>
) : WireConnection() {

    constructor(x: Float, y: Float, vararg connections: WireConnection) : this(x, y, connections.toMutableList())

    override fun toString(): String {
        return XML.encodeToString(this)
    }

}

@Serializable
@SerialName("segment")
class WireSegment(
    override var x: Float,
    override var y: Float,
) : WireConnection() {

    override fun toString(): String {
        return XML.encodeToString(this)
    }

}

enum class Rotate {
    @SerialName("0")
    ROT_0,
    @SerialName("1")
    ROT_90,
    @SerialName("2")
    ROT_180,
    @SerialName("3")
    ROT_270
}

@Serializable
@SerialName("block")
class Block(
    val type: String,
    val id: Int,
    var x: Float,
    var y: Float,
    val io: BlockIO = BlockIO(),
    @XmlElement(false)
    var rotate: Rotate = Rotate.ROT_0,
    var flipH: Boolean = false,
    var flipV: Boolean = false,
    var scaleX: Float = 1f,
    var scaleY: Float = 1f,
    @XmlElement(true)
    val value: Value? = null,
    val virtual: Virtual? = null,
    val innercode: MutableList<InnerCode> = mutableListOf(),
) {


    override fun toString(): String {
        return XML.encodeToString(this)
    }

}

@Serializable
@SerialName("io")
class BlockIO(
    val ports: MutableList<BlockIOField> = mutableListOf(),
) {

    @Deprecated("Use the primary constructor with ports parameter instead.")
    constructor(
        inputs: List<BlockInput> = emptyList(),
        outputs: List<BlockOutput> = emptyList(),
    ) : this((inputs + outputs).toMutableList())

    constructor(vararg ports: BlockIOField) : this(ports.toMutableList())

    override fun toString(): String {
        return XML.encodeToString(this)
    }
}

@Serializable
sealed class BlockIOField {
    abstract val name: String
    abstract val wire: Int
}

@Serializable
@SerialName("input")
class BlockInput(
    override val name: String,
    override val wire: Int,
) : BlockIOField() {

    override fun toString(): String {
        return XML.encodeToString(this)
    }

}

@Serializable
@SerialName("output")
class BlockOutput(
    override val name: String,
    override val wire: Int,
): BlockIOField() {

    override fun toString(): String {
        return XML.encodeToString(this)
    }

}

@Serializable
@SerialName("value")
class Value(
    val type: String,
    @XmlValue(true)
    val value: String
) {

    override fun toString(): String {
        return XML.encodeToString(this)
    }

}

@Serializable
@SerialName("virtual")
class Virtual(
    val ports: MutableList<Port>,
) {

    val byName by lazy {
        ports.associateBy { it.name }
    }

    constructor(vararg ports: Port) : this(ports.toMutableList())

    override fun toString(): String {
        return XML.encodeToString(this)
    }

}

enum class Direction {
    @SerialName("in")
    IN,
    @SerialName("out")
    OUT
}

@Serializable
@SerialName("port")
class Port(
    @XmlElement(false)
    val direction: Direction,
    val id: Int,
    override val name: String,
    @XmlElement(false)
    override val side: Side,
    @XmlElement(false)
    override val justify: Justify,
    override val type: String,
    val hollow: List<String>,
    val loopback: Boolean = false,
) : HeaderBlockIOField() {

    override fun toString(): String {
        return XML.encodeToString(this)
    }

}

@Serializable
@SerialName("innercode")
class InnerCode(
    val name: String,
    val io: BlockIO = BlockIO(),
    override val wires: Wires = Wires(),
    override val blocks: Blocks = Blocks()
) : Code() {

    override fun toString(): String {
        return XML.encodeToString(this)
    }

}