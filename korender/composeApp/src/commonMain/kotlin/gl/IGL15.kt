package com.zakgof.korender.gl

import java.nio.ByteBuffer

interface IGL15 {
    fun glBindBuffer(target: Int, buffer: Int)

    fun glBufferData(target: Int, data: ByteBuffer, usage: Int)

    fun glGenBuffers(): Int
}
