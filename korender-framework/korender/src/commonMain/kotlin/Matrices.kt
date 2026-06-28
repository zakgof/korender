package com.zakgof.korender

import com.zakgof.korender.math.Vec3

/**
 * Camera projection declaration (orthographic or perspective).
 * Defines the viewing frustum for rendering.
 */
interface ProjectionDeclaration {
    /**
     * Viewport width in world units.
     * For orthographic projections: width of the viewing area.
     * For perspective projections: affects the aspect ratio.
     */
    val width: Float

    /**
     * Viewport height in world units.
     * For orthographic projections: height of the viewing area.
     * For perspective projections: affects the aspect ratio.
     */
    val height: Float

    /**
     * Near clipping plane distance (minimum rendering distance from camera).
     * Objects closer than this distance are not rendered.
     */
    val near: Float

    /**
     * Far clipping plane distance (maximum rendering distance from camera).
     * Objects farther than this distance are not rendered.
     */
    val far: Float
}

/**
 * Projection mode (orthographic, perspective, etc.).
 * Defines how 3D coordinates are projected to 2D screen space.
 */
interface ProjectionMode

/**
 * Camera declaration (position and orientation).
 * Defines where and how the viewer observes the 3D scene.
 */
interface CameraDeclaration {
    /**
     * Camera position in world space.
     */
    val position: Vec3

    /**
     * Camera forward direction (normalized).
     * Points from the camera toward the scene.
     */
    val direction: Vec3

    /**
     * Camera up direction (normalized).
     * Defines camera's vertical orientation relative to world.
     */
    val up: Vec3
}