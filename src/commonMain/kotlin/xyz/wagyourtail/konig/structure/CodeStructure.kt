package xyz.wagyourtail.konig.structure

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlValue

const val VERSION = "1.0"

@Serializable
@SerialName("code")
data class CodeFile(
    val name: String,
    val version: String = VERSION,
    val headers: Headers,
    val main: Main
)

@Serializable
@SerialName("headers")
data class Headers(
    val version: String = VERSION,
    val headers: MutableList<HeaderChild>
)

@Serializable
sealed class HeaderChild

@Serializable
@SerialName("block")
data class HeaderBlock(
    val name: String,
    val group: String,
    val generics: Generics? = null,
    val io: HeaderBlockIO,
    val hollow: List<Hollow>,
    val dynamicHollow: List<DynamicHollow>,
    val image: Image? = null,
) : HeaderChild()

@Serializable
@SerialName("generics")
data class Generics(
    val generics: List<Generic>
)

@Serializable
@SerialName("generic")
data class Generic(
    val name: String,
    val extends: String? = null,
)

@Serializable
@SerialName("hollow")
data class Hollow(
    val name: String,
    val group: String? = null,
    val inputs: List<HeaderBlockInput>,
    val outputs: List<HeaderBlockOutput>
)

@Serializable
@SerialName("dynamichollow")
data class DynamicHollow(
    val group: String,
    val inputs: List<HeaderBlockInput>,
    val outputs: List<HeaderBlockOutput>
)

@Serializable
@SerialName("io")
data class HeaderBlockIO(
    val ports: List<HeaderBlockIOField>,
)

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
            return bySide[side]!!
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
data class HeaderBlockInput(
    override val name: String,
    @XmlElement(false)
    override val side: Side,
    @XmlElement(false)
    override val justify: Justify,
    override val type: String,
    val optional: Boolean = false
) : HeaderBlockIOField()

@Serializable
@SerialName("output")
data class HeaderBlockOutput(
    override val name: String,
    @XmlElement(false)
    override val side: Side,
    @XmlElement(false)
    override val justify: Justify,
    override val type: String,
) : HeaderBlockIOField()

@Serializable
@SerialName("image")
data class Image(
    val src: String
)

@Serializable
@SerialName("include")
data class Include(
    val src: String? = null,
    val intern: String? = null
) : HeaderChild()

sealed class Code {
    abstract val wires: Wires
    abstract val blocks: Blocks
}

@Serializable
@SerialName("main")
data class Main(
    override val wires: Wires,
    override val blocks: Blocks
) : Code()

@Serializable
@SerialName("wires")
data class Wires(
    @XmlElement(true)
    val wires: MutableList<Wire>,
)

@Serializable
@SerialName("blocks")
data class Blocks(
    val blocks: MutableList<Block>
)

@Serializable
@SerialName("wire")
data class Wire(
    val id: Int,
    val connections: MutableList<WireConnection>
)

@Serializable
sealed class WireConnection {
    abstract val x: Float
    abstract val y: Float
}

@Serializable
@SerialName("end")
data class WireEnd(
    override val x: Float,
    override val y: Float,
    val block: Int,
    val port: String
) : WireConnection()

@Serializable
@SerialName("branch")
data class WireBranch(
    override val x: Float,
    override val y: Float,
    val connections: MutableList<WireConnection>
) : WireConnection()

@Serializable
@SerialName("segment")
data class WireSegment(
    override val x: Float,
    override val y: Float,
) : WireConnection()

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
data class Block(
    val type: String,
    val id: Int,
    var x: Float,
    var y: Float,
    val io: BlockIO,
    @XmlElement(false)
    var rotate: Rotate = Rotate.ROT_0,
    var flipH: Boolean = false,
    var flipV: Boolean = false,
    var scaleX: Float = 1f,
    var scaleY: Float = 1f,
    @XmlElement(true)
    val value: Value? = null,
    val virtual: MutableList<Virtual> = mutableListOf(),
    val innercode: MutableList<InnerCode> = mutableListOf(),
)

@Serializable
@SerialName("io")
data class BlockIO(
    val inputs: MutableList<BlockInput> = mutableListOf(),
    val outputs: MutableList<BlockOutput> = mutableListOf()
)

@Serializable
sealed class BlockIOField {
    abstract val name: String
    abstract val wire: Int
}

@Serializable
@SerialName("input")
data class BlockInput(
    override val name: String,
    override val wire: Int,
) : BlockIOField()

@Serializable
@SerialName("output")
data class BlockOutput(
    override val name: String,
    override val wire: Int,
): BlockIOField()

@Serializable
@SerialName("value")
data class Value(
    val type: String,
    @XmlValue(true)
    val value: String
)

@Serializable
@SerialName("virtual")
data class Virtual(
    val ports: MutableList<Port>,
    val forName: String? = null,
    val forGroup: String? = null,
)

enum class Direction {
    @SerialName("in")
    IN,
    @SerialName("out")
    OUT
}

@Serializable
@SerialName("port")
data class Port(
    @XmlElement(false)
    val direction: Direction,
    val id: Int,
    override val name: String,
    @XmlElement(false)
    override val side: Side,
    @XmlElement(false)
    override val justify: Justify,
    override val type: String,
    val innerWire: Int?,
    val outerWire: Int?,
    val loopback: Boolean = false,
    val loopbackWire: Int? = null
) : HeaderBlockIOField()

@Serializable
@SerialName("innercode")
data class InnerCode(
    val name: String,
    val io: BlockIO,
    override val wires: Wires,
    override val blocks: Blocks
) : Code()