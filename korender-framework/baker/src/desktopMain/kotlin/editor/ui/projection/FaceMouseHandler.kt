package com.zakgof.korender.baker.editor.ui.projection

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerButtons
import androidx.compose.ui.input.pointer.isPrimaryPressed
import editor.model.Model
import editor.state.State
import editor.state.StateHolder
import editor.ui.projection.MouseHandler
import editor.ui.projection.ProjectionMapper

internal class FaceMouseHandler(
    private val mapper: ProjectionMapper,
    private val state: State,
    private val model: Model,
    private val holder: StateHolder,
) : MouseHandler {

    override fun onClick(current: Offset, buttons: PointerButtons, isCtrlDown: Boolean) {
        if (buttons.isPrimaryPressed) {
            val cam = mapper.vToW(current) - mapper.axes.lookAxis * 1e3f
            holder.selectViaRay(cam, mapper.axes.lookAxis, isCtrlDown)
        }
    }
}
