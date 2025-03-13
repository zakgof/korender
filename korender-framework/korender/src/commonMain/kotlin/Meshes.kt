package com.zakgof.korender

import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

enum class IndexType {
    Byte,
    Short,
    Int
}

enum class AttributeType {
    Byte,
    Short,
    Int,
    SignedByte,
    SignedShort,
    SignedInt,
    Float
}

class MeshAttribute(
    val name: String,
    val structSize: Int,
    val primitiveType: AttributeType,
    val location: Int
)

object Attributes {
    val POS = MeshAttribute("pos", 3, AttributeType.Float, 0)
    val NORMAL = MeshAttribute("normal", 3, AttributeType.Float,1)
    val TEX = MeshAttribute("tex", 2, AttributeType.Float, 2)
    val JOINTS_BYTE = MeshAttribute("joints", 4, AttributeType.Byte,3)
    val JOINTS_SHORT = MeshAttribute("joints", 4, AttributeType.Short,3)
    val JOINTS_INT = MeshAttribute("joints", 4, AttributeType.Int,3)
    val WEIGHTS = MeshAttribute("weights", 4, AttributeType.Float, 4)
    val SCREEN = MeshAttribute("screen", 2, AttributeType.Float, 5)
    val SCALE = MeshAttribute("scale", 2, AttributeType.Float, 6)
    val PHI = MeshAttribute("phi", 1, AttributeType.Float, 7)
    val B1 = MeshAttribute("b1", 1, AttributeType.SignedByte,8)
    val B2 = MeshAttribute("b2", 1, AttributeType.SignedByte,9)
    val B3 = MeshAttribute("b3", 1, AttributeType.SignedByte,10)
}

interface MeshDeclaration

interface MeshInitializer {
    fun attr(attr: MeshAttribute, vararg v: Float): MeshInitializer
    fun attr(attr: MeshAttribute, vararg b: Byte): MeshInitializer
    fun pos(vararg position: Vec3): MeshInitializer
    fun pos(vararg v: Float): MeshInitializer
    fun normal(vararg position: Vec3): MeshInitializer
    fun normal(vararg v: Float): MeshInitializer
    fun tex(vararg position: Vec2): MeshInitializer
    fun tex(vararg v: Float): MeshInitializer
    fun scale(vararg position: Vec2): MeshInitializer
    fun scale(vararg v: Float): MeshInitializer
    fun phi(vararg v: Float): MeshInitializer
    fun index(vararg indices: Int): MeshInitializer
    fun indexBytes(rawBytes: ByteArray): MeshInitializer
    fun attrBytes(attr: MeshAttribute, rawBytes: ByteArray): MeshInitializer
}