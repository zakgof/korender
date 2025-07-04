package com.zakgof.korender.impl.engine

import com.zakgof.korender.impl.context.Direction
import com.zakgof.korender.impl.font.InternalFontDeclaration
import com.zakgof.korender.impl.geometry.FontMesh
import com.zakgof.korender.impl.geometry.ImageQuad
import com.zakgof.korender.impl.material.InternalMaterialModifier
import com.zakgof.korender.impl.material.ResourceTextureDeclaration
import com.zakgof.korender.math.Transform
import com.zakgof.korender.math.Vec2
import kotlin.math.max

internal class GuiRenderer(
    private val inventory: Inventory,
    private val width: Int,
    private val height: Int,
    container: ElementDeclaration.Container
) {

    val renderableDeclarations = mutableListOf<RenderableDeclaration>()
    val touchBoxes = mutableListOf<TouchBox>()

    init {
        val sizes = mutableMapOf<ElementDeclaration, Size>()
        sizeEm(Direction.Stack, container, sizes)
        layoutContainer(sizes, 0, 0, width, height, container)
    }

    private fun layoutContainer(
        sizes: MutableMap<ElementDeclaration, Size>,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        container: ElementDeclaration.Container
    ): Unit = when (container.direction) {
        Direction.Vertical -> {
            val fillers = container.elements.count { sizes[it]!!.height < 0 }
            val normalsHeight =
                container.elements.map { sizes[it]!!.height }.filter { it >= 0 }.sum()
            val fillerHeight = if (fillers == 0) 0 else (height - normalsHeight) / fillers
            var currY = y
            for (child in container.elements) {
                val declSize = sizes[child]!!
                val childWidth = if (declSize.width < 0) width else declSize.width
                val childHeight = if (declSize.height < 0) fillerHeight else declSize.height
                when (child) {
                    is ElementDeclaration.Text -> createText(child, x, currY, childWidth)
                    is ElementDeclaration.Image -> createImage(child, x, currY)
                    is ElementDeclaration.Container -> layoutContainer(sizes, x, currY, childWidth, childHeight, child)
                    is ElementDeclaration.Filler -> {}
                }
                currY += childHeight
            }
        }

        Direction.Horizontal -> {
            val fillers = container.elements.count { sizes[it]!!.width < 0 }
            val normalsWidths =
                container.elements.map { sizes[it]!!.width }.filter { it >= 0 }.sum()
            val fillerWidth = if (fillers == 0) 0 else (width - normalsWidths) / fillers
            var currX = x
            for (child in container.elements) {
                val declSize = sizes[child]!!
                val childWidth = if (declSize.width < 0) fillerWidth else declSize.width
                val childHeight = if (declSize.height < 0) height else declSize.height
                when (child) {
                    is ElementDeclaration.Text -> createText(child, currX, y, childWidth)
                    is ElementDeclaration.Image -> createImage(child, currX, y)
                    is ElementDeclaration.Container -> layoutContainer(sizes, currX, y, childWidth, childHeight, child)
                    is ElementDeclaration.Filler -> {}
                }
                currX += childWidth
            }
        }

        Direction.Stack -> {
            for (child in container.elements) {
                val declSize = sizes[child]!!
                val w = if (declSize.width < 0) width else declSize.width
                val h = if (declSize.height < 0) height else declSize.height
                when (child) {
                    is ElementDeclaration.Text -> createText(child, x, y, w)
                    is ElementDeclaration.Image -> createImage(child, x, y)
                    is ElementDeclaration.Container -> layoutContainer(sizes, x, y, w, h, child)
                    is ElementDeclaration.Filler -> {}
                }
            }
        }
    }

    private fun createImage(declaration: ElementDeclaration.Image, x: Int, y: Int) {

        renderableDeclarations += RenderableDeclaration(
            BaseMaterial.Image,
            listOf(InternalMaterialModifier {
                it.uniforms["pos"] = Vec2(
                    (x.toFloat() + declaration.marginLeft.toFloat()) / width,
                    1.0f - (y.toFloat() + declaration.marginTop.toFloat() + declaration.height.toFloat()) / height
                )
                it.uniforms["size"] = Vec2(
                    declaration.width.toFloat() / width,
                    declaration.height.toFloat() / height
                )
                it.uniforms["imageTexture"] = ResourceTextureDeclaration(declaration.imageResource, retentionPolicy = declaration.retentionPolicy)
            }),
            ImageQuad(declaration.retentionPolicy),
            Transform.IDENTITY,
            declaration.retentionPolicy
        )

        touchBoxes.add(
            TouchBox(
                x + declaration.marginLeft,
                y + declaration.marginTop,
                declaration.width,
                declaration.height,
                declaration.id,
                declaration.onTouch,
            )
        )
    }

    private fun createText(declaration: ElementDeclaration.Text, xxx: Int, yyy: Int, w: Int) {
        val font = inventory.font(InternalFontDeclaration(declaration.fontResource, declaration.retentionPolicy))
        if (w > 0 && font != null) {
            renderableDeclarations += RenderableDeclaration(
                BaseMaterial.Font,
                listOf(InternalMaterialModifier {
                    it.uniforms["color"] = declaration.color
                    it.uniforms["fontTexture"] = font.gpuTexture
                }),
                FontMesh(declaration.id, 256, declaration, width.toFloat(), height.toFloat(), xxx.toFloat(), yyy.toFloat(), font),
                Transform.IDENTITY,
                declaration.retentionPolicy
            )
            touchBoxes.add(TouchBox(xxx, yyy, w, declaration.height, declaration.id, declaration.onTouch))
        }
    }

    private fun sizeEm(
        parentDirection: Direction,
        element: ElementDeclaration,
        sizes: MutableMap<ElementDeclaration, Size>
    ): Size {
        val size = when (element) {
            is ElementDeclaration.Text -> textSize(element)
            is ElementDeclaration.Image -> Size(element.fullWidth, element.fullHeight)
            is ElementDeclaration.Filler -> {
                if (parentDirection == Direction.Vertical) Size(0, -1) else Size(-1, 0)
            }

            is ElementDeclaration.Container ->
                when (element.direction) {
                    Direction.Vertical -> {
                        var w = 0
                        var h = 0
                        for (child in element.elements) {
                            val childSize = sizeEm(element.direction, child, sizes)
                            if (w >= 0) {
                                w = if (childSize.width < 0) -1 else max(w, childSize.width)
                            }
                            if (h >= 0) {
                                if (childSize.height < 0) {
                                    h = -1
                                } else {
                                    h += childSize.height
                                }
                            }
                        }
                        Size(w, h)
                    }

                    Direction.Horizontal -> {
                        var w = 0
                        var h = 0
                        for (child in element.elements) {
                            val childSize = sizeEm(element.direction, child, sizes)
                            if (h >= 0) {
                                h = if (childSize.height < 0) -1 else max(h, childSize.height)
                            }
                            if (w >= 0) {
                                if (childSize.width < 0) {
                                    w = -1
                                } else {
                                    w += childSize.width
                                }
                            }
                        }
                        Size(w, h)
                    }

                    Direction.Stack -> {
                        var w = 0
                        var h = 0
                        for (child in element.elements) {
                            val childSize = sizeEm(element.direction, child, sizes)
                            w = if (childSize.width < 0 || w < 0) -1 else max(childSize.width, w)
                            h = if (childSize.height < 0 || h < 0) -1 else max(childSize.height, h)
                        }
                        Size(w, h)
                    }
                }
        }

        sizes[element] = size
        return size
    }

    private fun textSize(textDeclaration: ElementDeclaration.Text): Size {
        val font = inventory.font(InternalFontDeclaration(textDeclaration.fontResource, textDeclaration.retentionPolicy))
        return Size(
            font?.textWidth(textDeclaration.height, textDeclaration.text) ?: 0,
            textDeclaration.height
        )
    }

    class Size(val width: Int, val height: Int)
}