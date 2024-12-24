package com.zakgof.korender.buffer

expect interface BufferData<E> {
    fun put(value: E)
    fun get(): E
    operator fun get(index: Int): E
    fun position(): Int
    fun position(pos: Int)
    fun remaining(): Int
    fun clear()
    fun rewind()
    fun size() : Int
}