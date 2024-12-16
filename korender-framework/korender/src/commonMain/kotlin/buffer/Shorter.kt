package com.zakgof.korender.buffer

expect class Shorter : BufferData<Short> {
    fun put(values: ShortArray)
}