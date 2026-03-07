package com.zakgof.korender.impl.geometry

import com.zakgof.korender.math.Vec2
import com.zakgof.korender.math.Vec3

internal object MeshAttributes {
    val POS = InternalMeshAttribute<Vec3>("pos", 3, AttributeType.Float, 0, Vec3BufferAccessor)
    val NORMAL = InternalMeshAttribute<Vec3>("normal", 3, AttributeType.Float, 1, Vec3BufferAccessor)
    val TEX = InternalMeshAttribute<Vec2>("tex", 2, AttributeType.Float, 2, Vec2BufferAccessor)
    val JOINTS_BYTE = InternalMeshAttribute<ByteArray>("joints", 4, AttributeType.Byte, 3, Byte4BufferAccessor)
    val JOINTS_SHORT = InternalMeshAttribute<ShortArray>("joints", 4, AttributeType.Short, 3, Short4BufferAccessor)
    val JOINTS_INT = InternalMeshAttribute<IntArray>("joints", 4, AttributeType.Int, 3, Int4BufferAccessor)
    val WEIGHTS = InternalMeshAttribute<FloatArray>("weights", 4, AttributeType.Float, 4, Float4BufferAccessor)
    val SCALE = InternalMeshAttribute<Vec2>("scale", 2, AttributeType.Float, 5, Vec2BufferAccessor)
    val B1 = InternalMeshAttribute<Byte>("b1", 1, AttributeType.SignedByte, 8, ByteBufferAccessor)
    val B2 = InternalMeshAttribute<Byte>("b2", 1, AttributeType.SignedByte, 9, ByteBufferAccessor)
    val B3 = InternalMeshAttribute<Byte>("b3", 1, AttributeType.SignedByte, 10, ByteBufferAccessor)
    val MODEL0 = InternalMeshAttribute<FloatArray>("instanceModel0", 4, AttributeType.Float, 11, Float4BufferAccessor, true)
    val MODEL1 = InternalMeshAttribute<FloatArray>("instanceModel1", 4, AttributeType.Float, 12, Float4BufferAccessor, true)
    val MODEL2 = InternalMeshAttribute<FloatArray>("instanceModel2", 4, AttributeType.Float, 13, Float4BufferAccessor, true)
    val MODEL3 = InternalMeshAttribute<FloatArray>("instanceModel3", 4, AttributeType.Float, 14, Float4BufferAccessor, true)
    val INSTPOS = InternalMeshAttribute<Vec3>("instpos", 3, AttributeType.Float, 11, Vec3BufferAccessor, true)
    val INSTSCALE = InternalMeshAttribute<Vec2>("instscale", 2, AttributeType.Float, 12, Vec2BufferAccessor, true)
    val INSTROT = InternalMeshAttribute<Float>("instrot", 1, AttributeType.Float, 13, FloatBufferAccessor, true)
    val INSTTEX = InternalMeshAttribute<FloatArray>("insttexrect", 4, AttributeType.Float, 11, Float4BufferAccessor, true)
    val INSTSCREEN = InternalMeshAttribute<FloatArray>("instscreentect", 4, AttributeType.Float, 12, Float4BufferAccessor, true)
}