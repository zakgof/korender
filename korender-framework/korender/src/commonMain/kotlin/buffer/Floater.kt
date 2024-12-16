package com.zakgof.korender.buffer

expect class Floater : BufferData<Float> {
    fun put(values: FloatArray)
}