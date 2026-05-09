package com.zakgof.korender

import androidx.compose.ui.graphics.ImageBitmap
import com.zakgof.korender.math.ColorRGBA

/**
 * Pixel format for 2D images.
 */
enum class PixelFormat(val bytes: Int) {
    /** 24-bit RGB (8-bit per channel) */
    RGB(3),
    /** 32-bit RGBA (8-bit per channel) */
    RGBA(4),
    /** 8-bit grayscale */
    Gray(1),
    /** 16-bit grayscale (high precision) */
    Gray16(2);
}

/**
 * 2D image with pixel access.
 * Images can be loaded from files, created programmatically, or captured from rendering.
 */
interface Image {

    /** Image width in pixels */
    val width: Int

    /** Image height in pixels */
    val height: Int

    /** Pixel format (determines bytes per pixel) */
    val format: PixelFormat

    /**
     * Gets the color at the given pixel coordinates.
     * @param x X coordinate (0 = left)
     * @param y Y coordinate (0 = top)
     * @return RGBA color at the pixel
     */
    fun pixel(x: Int, y: Int): ColorRGBA

    /**
     * Sets the color at the given pixel coordinates.
     * @param x X coordinate (0 = left)
     * @param y Y coordinate (0 = top)
     * @param color RGBA color to set
     */
    fun setPixel(x: Int, y: Int, color: ColorRGBA)

    /**
     * Exports the image as TGA (Targa) format data.
     * @return TGA file data as byte array
     */
    fun toTga(): ByteArray

    /**
     * Exports the image as raw pixel data.
     * @return raw pixel data in the image's pixel format
     */
    fun toRaw(): ByteArray

    /**
     * Converts this image into a Compose [ImageBitmap].
     */
    fun toCompose(): ImageBitmap
}

/**
 * 3D volumetric image (voxel grid).
 * Used for volume rendering and 3D texture data.
 */
interface Image3D {

    /** Image width in pixels */
    val width: Int

    /** Image height in pixels */
    val height: Int

    /** Image depth in pixels/voxels */
    val depth: Int

    /** Pixel format (determines bytes per voxel) */
    val format: PixelFormat

    /**
     * Gets the color at the given voxel coordinates.
     * @param x X coordinate (0 = left)
     * @param y Y coordinate (0 = top)
     * @param z Z coordinate (0 = front)
     * @return RGBA color at the voxel
     */
    fun pixel(x: Int, y: Int, z: Int): ColorRGBA

    /**
     * Sets the color at the given voxel coordinates.
     * @param x X coordinate (0 = left)
     * @param y Y coordinate (0 = top)
     * @param z Z coordinate (0 = front)
     * @param color RGBA color to set
     */
    fun setPixel(x: Int, y: Int, z: Int, color: ColorRGBA)

    /**
     * Exports the image as raw voxel data.
     * @return raw voxel data in the image's pixel format
     */
    fun toRaw(): ByteArray
}
