package com.zakgof.korender.baker.editor.ui

import editor.model.BoundingBox
import editor.model.Model
import editor.state.State

fun selectedBrushes(state: State, model: Model) =
    model.brushes.values
        .filter { brush -> state.brushSelection.contains(brush.id) }
        .toSet()

fun selectedEntityInstances(state: State, model: Model) =
    model.entityInstances.values
        .filter { ei -> state.entityInstanceSelection.contains(ei.id) }
        .toSet()

fun selectionBB(state: State, model: Model) =
    (selectedBrushes(state, model) + selectedEntityInstances(state, model))
        .map { it.bb }
        .reduceOrNull(BoundingBox::merge)