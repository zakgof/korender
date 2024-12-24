package com.zakgof.korender.buffer

expect class Byter : BufferData<Byte> {
    fun put(values: ByteArray)
    fun toFloater() : Floater
}