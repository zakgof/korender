package com.zakgof.korender.buffer

actual class Shorter : BufferData<Any?> {
    actual fun clear(): Shorter {
        TODO("Not yet implemented")
    }

    actual fun put(value: Short) {
    }

    actual operator fun get(index: Int): Short {
        TODO("Not yet implemented")
    }

    actual open fun rewind(): Shorter {
        TODO("Not yet implemented")
    }
}