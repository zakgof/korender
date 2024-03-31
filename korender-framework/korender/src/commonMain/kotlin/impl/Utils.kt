package com.zakgof.korender.impl

import com.zakgof.korender.KorenderException
import com.zakgof.korender.impl.material.Texturing
import java.io.InputStream

fun resourceStream(resource: String) : InputStream = Texturing::class.java.getResourceAsStream(resource) ?: throw KorenderException("Resource $resource not found in the classpath")