package com.zakgof.korender

import com.zakgof.korender.math.Vec3

interface ProjectionDeclaration

interface FrustumProjectionDeclaration : ProjectionDeclaration {
    val width: Float
    val height: Float
    val near: Float
    val far: Float
}

interface OrthoProjectionDeclaration : ProjectionDeclaration {
    val width: Float
    val height: Float
    val near: Float
    val far: Float
}

interface CameraDeclaration {
    val position: Vec3
    val direction: Vec3
    val up: Vec3
}