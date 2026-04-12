package com.zakgof.korender.baker.editor.model.brush

import editor.model.BoundingBox
import editor.model.brush.Face
import editor.model.brush.Plane

sealed interface CreatorShape {

    fun makeFaces(bb: BoundingBox, materialId: String, fitToFace: Boolean): List<Face>

    object Box: CreatorShape {
        override fun makeFaces(bb: BoundingBox, materialId: String, fitToFace: Boolean) =
            Plane.cube(bb).map { Face(it, materialId, fitToFace) }
    }

    class Cylinder(val sides: Int = 8): CreatorShape {
        override fun makeFaces(bb: BoundingBox, materialId: String, fitToFace: Boolean): List<Face> {
            TODO("Not yet implemented")
        }
    }
}