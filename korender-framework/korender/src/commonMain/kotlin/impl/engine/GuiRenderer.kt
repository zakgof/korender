package com.zakgof.korender.impl.engine

import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.TextureWrap
import com.zakgof.korender.impl.context.Direction
import com.zakgof.korender.impl.font.InternalFontDeclaration
import com.zakgof.korender.impl.geometry.FontMesh
import com.zakgof.korender.impl.geometry.ImageQuad
import com.zakgof.korender.impl.glgpu.ColorRGBAGetter
import com.zakgof.korender.impl.glgpu.GlGpuTexture
import com.zakgof.korender.impl.glgpu.TextureGetter
import com.zakgof.korender.impl.glgpu.Vec2Getter
import com.zakgof.korender.impl.material.InternalMaterial
import com.zakgof.korender.impl.material.InternalResourceTextureDeclaration
import com.zakgof.korender.math.ColorRGBA
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec2
import kotlin.math.max

internal class GuiRenderer(
    private val inventory: Inventory,
    private val width: Float,
    private val height: Float,
    container: ElementDeclaration.Container,
) {

    val renderableDeclarations = mutableListOf<RenderableDeclaration>()
    val touchBoxes = mutableListOf<TouchBox>()

    init {
        val sizes = mutableMapOf<ElementDeclaration, Size>()
        sizeEm(Direction.Stack, container, sizes)
        layoutContainer(sizes, 0f, 0f, width, height, container)
    }

    private fun layoutContainer(
        sizes: MutableMap<ElementDeclaration, Size>,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        container: ElementDeclaration.Container,
    ): Unit = when (container.direction) {
        Direction.Vertical -> {
            val fillers = container.elements.count { sizes[it]!!.height < 0f }
            val normalsHeight =
                container.elements.map { sizes[it]!!.height }.filter { it >= 0f }.sum()
            val fillerHeight = if (fillers == 0) 0f else (height - normalsHeight) / fillers
            var currY = y + container.paddingTop
            for (child in container.elements) {
                val declSize = sizes[child]!!
                val childWidth = if (declSize.width < 0f) width else declSize.width
                val childHeight = if (declSize.height < 0f) fillerHeight else declSize.height
                when (child) {
                    is ElementDeclaration.Text -> createText(child, x + container.paddingLeft, currY, childWidth)
                    is ElementDeclaration.Image -> createImage(child, x + container.paddingLeft, currY)
                    is ElementDeclaration.Container -> layoutContainer(sizes, x + container.paddingLeft, currY, childWidth, childHeight, child)
                    is ElementDeclaration.Filler -> {}
                }
                currY += childHeight
            }
        }

        Direction.Horizontal -> {
            val fillers = container.elements.count { sizes[it]!!.width < 0f }
            val normalsWidths =
                container.elements.map { sizes[it]!!.width }.filter { it >= 0f }.sum()
            val fillerWidth = if (fillers == 0) 0f else (width - normalsWidths) / fillers
            var currX = x + container.paddingLeft
            for (child in container.elements) {
                val declSize = sizes[child]!!
                val childWidth = if (declSize.width < 0f) fillerWidth else declSize.width
                val childHeight = if (declSize.height < 0f) height else declSize.height
                when (child) {
                    is ElementDeclaration.Text -> createText(child, currX, y + container.paddingTop, childWidth)
                    is ElementDeclaration.Image -> createImage(child, currX, y + container.paddingTop)
                    is ElementDeclaration.Container -> layoutContainer(sizes, currX, y + container.paddingTop, childWidth, childHeight, child)
                    is ElementDeclaration.Filler -> {}
                }
                currX += childWidth
            }
        }

        Direction.Stack -> {
            for (child in container.elements) {
                val declSize = sizes[child]!!
                val w = if (declSize.width < 0f) width else declSize.width
                val h = if (declSize.height < 0f) height else declSize.height
                val xPos = x + container.paddingLeft
                val yPos = y + container.paddingTop
                when (child) {
                    is ElementDeclaration.Text -> createText(child, xPos, yPos, w)
                    is ElementDeclaration.Image -> createImage(child, xPos, yPos)
                    is ElementDeclaration.Container -> layoutContainer(sizes, xPos, yPos, w, h, child)
                    is ElementDeclaration.Filler -> {}
                }
            }
        }
    }

    private fun createImage(declaration: ElementDeclaration.Image, x: Float, y: Float) {

        val imageMaterial = ImageMaterial(
            pos = Vec2(
                x / width,
                1.0f - (y + declaration.height) / height
            ),
            size = Vec2(
                declaration.width / width,
                declaration.height / height
            ),
            imageTexture = InternalResourceTextureDeclaration(declaration.imageResource, nodeContext = declaration.nodeContext, wrap = TextureWrap.MirroredRepeat)
        )
        renderableDeclarations += RenderableDeclaration(
            imageMaterial,
            ImageQuad(declaration.nodeContext),
            Transform.IDENTITY,
            true,
            declaration.nodeContext
        )

        touchBoxes.add(
            TouchBox(
                x,
                y,
                declaration.width,
                declaration.height,
                declaration.id,
                declaration.onTouch,
            )
        )
    }

    private fun createText(declaration: ElementDeclaration.Text, xxx: Float, yyy: Float, w: Float) {
        val font = inventory.font(InternalFontDeclaration(declaration.fontResource, declaration.nodeContext))
        if (w > 0f && font != null) {
            renderableDeclarations += RenderableDeclaration(
                FontMaterial(declaration.color, font.gpuTexture),
                FontMesh(declaration.id, 256, declaration, width, height, xxx, yyy, font),
                Transform.IDENTITY,
                true,
                declaration.nodeContext
            )
            touchBoxes.add(TouchBox(xxx, yyy, w, declaration.height, declaration.id, declaration.onTouch))
        }
    }

    private fun sizeEm(
        parentDirection: Direction,
        element: ElementDeclaration,
        sizes: MutableMap<ElementDeclaration, Size>,
    ): Size {
        val size = when (element) {
            is ElementDeclaration.Text -> textSize(element)
            is ElementDeclaration.Image -> Size(element.width, element.height)
            is ElementDeclaration.Filler -> {
                if (parentDirection == Direction.Vertical) Size(0f, -1f) else Size(-1f, 0f)
            }

            is ElementDeclaration.Container ->
                when (element.direction) {
                    Direction.Vertical -> {
                        var w = 0f
                        var h = 0f
                        for (child in element.elements) {
                            val childSize = sizeEm(element.direction, child, sizes)
                            if (w >= 0f) {
                                w = if (childSize.width < 0f) -1f else max(w, childSize.width)
                            }
                            if (h >= 0f) {
                                if (childSize.height < 0f) {
                                    h = -1f
                                } else {
                                    h += childSize.height
                                }
                            }
                        }
                        Size(w, h).withPadding(element)
                    }

                    Direction.Horizontal -> {
                        var w = 0f
                        var h = 0f
                        for (child in element.elements) {
                            val childSize = sizeEm(element.direction, child, sizes)
                            if (h >= 0f) {
                                h = if (childSize.height < 0f) -1f else max(h, childSize.height)
                            }
                            if (w >= 0f) {
                                if (childSize.width < 0f) {
                                    w = -1f
                                } else {
                                    w += childSize.width
                                }
                            }
                        }
                        Size(w, h).withPadding(element)
                    }

                    Direction.Stack -> {
                        var w = 0f
                        var h = 0f
                        for (child in element.elements) {
                            val childSize = sizeEm(element.direction, child, sizes)
                            w = if (childSize.width < 0f || w < 0f) -1f else max(childSize.width, w)
                            h = if (childSize.height < 0f || h < 0f) -1f else max(childSize.height, h)
                        }
                        Size(w, h).withPadding(element)
                    }
                }
        }

        sizes[element] = size
        return size
    }

    private fun textSize(textDeclaration: ElementDeclaration.Text): Size {
        val font = inventory.font(InternalFontDeclaration(textDeclaration.fontResource, textDeclaration.nodeContext))
        return Size(
            font?.textWidth(textDeclaration.height, textDeclaration.text) ?: 0f,
            textDeclaration.height
        )
    }

    class Size(val width: Float, val height: Float) {
        fun withPadding(container: ElementDeclaration.Container): Size =
            Size(
                if (width < 0) width else width + container.paddingLeft + container.paddingRight,
                if (height < 0) height else height + container.paddingTop + container.paddingBottom
            )
    }
}

private class ImageMaterial(
    val pos: Vec2,
    val size: Vec2,
    val imageTexture: TextureDeclaration,
) : InternalMaterial(
    "!shader/gui/image.vert", "!shader/gui/image.frag",
    "pos" to Vec2Getter<ImageMaterial> { it.pos },
    "size" to Vec2Getter<ImageMaterial> { it.size },
    "imageTexture" to TextureGetter<ImageMaterial> { it.imageTexture },
)

private class FontMaterial(
    val color: ColorRGBA,
    val fontTexture: GlGpuTexture,
) : InternalMaterial(
    "!shader/gui/font.vert", "!shader/gui/font.frag",
    "color" to ColorRGBAGetter<FontMaterial> { it.color },
    "fontTexture" to TextureGetter<FontMaterial> { it.fontTexture },
)
