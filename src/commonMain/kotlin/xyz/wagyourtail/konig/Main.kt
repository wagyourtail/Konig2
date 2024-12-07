package xyz.wagyourtail.konig

import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import xyz.wagyourtail.konig.structure.*

val xmlReader = XML {
    autoPolymorphic = true
}

val helloWorld: CodeFile = xmlReader.decodeFromString<CodeFile>("""
    <code name="hello world example" version="1.0">
        <headers version="1.0">
            <include intern="stdlib" />
            <block name="test" group="debug">
                <io>
                    <input side="top" justify="center" name="name" type="boolean" optional="true" />
                    <output side="bottom" justify="center" name="name" type="int"/>
                </io>
                <image src="path/to/img.png" />
            </block>
        </headers>
        <main>
            <wires>
                <wire id="0">
                    <end x="1.0" y="0.5" block="0" port="out"/>
                    <end x="2.0" y="0.5" block="1" port="in"/>
                </wire>
            </wires>
            <blocks>
                <block type="const" id="0" x="0" y="0" flipH="false" flipV="false" rotate="0" scaleX="1" scaleY="1">
                    <io>
                        <output name="out" wire="0"/>
                    </io>
                    <value type="String">Hello World!</value>
                </block>
                <block type="print" id="1" x="2" y="0" flipH="false" flipV="false" rotate="0" scaleX="1" scaleY="1">
                    <io>
                        <input name="in" wire="0"/>
                    </io>
                </block>
            </blocks>
        </main>
    </code>
    """.trimIndent())

val countToTen: CodeFile = CodeFile(
    name = "Count To Ten Example",
    version = "1.0",
    headers = Headers(
        headers = mutableListOf(
            Include(intern = "stdlib")
        )
    ),
    main = Main(
        Wires(mutableListOf(
            Wire(0, mutableListOf(
                WireEnd(1f, 0f, 0, "out"),
                WireEnd(2f, 0f, 1, "virtual\$forName\$inner\$0")
            ))
        )),
        Blocks(mutableListOf(
            Block(
                "const", 0,
                0f, -.5f,
                BlockIO(
                    outputs = mutableListOf(BlockOutput("out", 0))
                ),
                value = Value("Number", "0")
            ),
            Block(
                "loop", 1,
                2f, -2f,
                BlockIO(),
                scaleX = 6f,
                scaleY = 4f,
                virtual = mutableListOf(Virtual(
                    mutableListOf(
                        Port(
                            Direction.IN,
                            0,
                            "virtual\$forName\$inner\$0",
                            Side.LEFT,
                            Justify.CENTER,
                            "Number",
                            0,
                            0,
                            true,
                            1
                        )
                    ),
                    forName = "inner",
                )),
                innercode = mutableListOf(
                    InnerCode(
                        "inner",
                        BlockIO(
                            inputs = mutableListOf(
                                BlockInput("continue", 2)
                            ),
                        ),
                        Wires(mutableListOf(
                            Wire(0, mutableListOf(
                                WireEnd(0f, 1.9f, 0, "virtual\$forName\$inner\$0"),
                                WireSegment(.25f, 1.9f),
                                WireSegment(.25f, .9f),
                                WireEnd(2f, .9f, 1, "in1")
                            )),
                            Wire(1, mutableListOf(
                                WireEnd(1.5f, 1.5f, 2, "out"),
                                WireSegment(1.75f, 1.5f),
                                WireSegment(1.75f, 1.1f),
                                WireEnd(2f, 1.1f, 1, "in2")
                            )),
                            Wire(2, mutableListOf(
                                WireEnd(3f, 1f, 1, "out"),
                                WireBranch(3.5f, 1f, mutableListOf(
                                    WireBranch(3.5f, 2f, mutableListOf(
                                        WireEnd(0f, 2f, 0, "virtual\$forName\$inner\$0\$loopback"),
                                    )),
                                    WireSegment(3.5f, 2.4f),
                                    WireEnd(4f, 2.4f, 4, "in1")
                                )),
                                WireEnd(4f, 1f, 3, "in"),
                            )),
                            Wire(3, mutableListOf(
                                WireEnd(3f, 3f, 5, "out"),
                                WireSegment(3.5f, 3f),
                                WireSegment(3.5f, 2.6f),
                                WireEnd(4f, 2.6f, 4, "in2")
                            )),
                            Wire(4, mutableListOf(
                                WireEnd(5f, 2.5f, 4, "out"),
                                WireSegment(5.5f, 2.5f),
                                WireSegment(5.5f, 1.9f),
                                WireEnd(5.8f, 1.9f, 0, "continue")
                            ))
                        )),
                        Blocks(mutableListOf(
                            Block(
                                "add", 1,
                                2f, .5f,
                                BlockIO(
                                    inputs = mutableListOf(
                                        BlockInput(name = "in1", wire = 0),
                                        BlockInput(name = "in2", wire = 1)
                                    ),
                                    outputs = mutableListOf(
                                        BlockOutput(name = "out", wire = 2)
                                    )
                                )
                            ),
                            Block(
                                "const", 2,
                                .5f, 1f,
                                BlockIO(
                                    outputs = mutableListOf(
                                        BlockOutput(
                                            name = "out",
                                            wire = 1
                                        )
                                    )
                                ),
                                value = Value("Number", "1")
                            ),
                            Block(
                                "print", 3,
                                4f, .5f,
                                BlockIO(
                                    inputs = mutableListOf(
                                        BlockInput(name = "in", wire = 2)
                                    )
                                )
                            ),
                            Block(
                                "lt", 4,
                                4f, 2f,
                                BlockIO(
                                    inputs = mutableListOf(
                                        BlockInput(name = "in1", wire = 2),
                                        BlockInput(name = "in2", wire = 3)
                                    ),
                                    outputs = mutableListOf(
                                        BlockOutput(name = "out", wire = 4)
                                    )
                                )
                            ),
                            Block(
                                "const", 5,
                                2f, 2.5f,
                                BlockIO(
                                    outputs = mutableListOf(
                                        BlockOutput(name = "out", wire = 3)
                                    )
                                ),
                                value = Value("Number", "10")
                            )
                        ))
                    )
                )
            )
        ))
    )
)

fun main() {
    println(helloWorld)
}