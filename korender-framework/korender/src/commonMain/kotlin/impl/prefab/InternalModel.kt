package com.zakgof.korender.impl.prefab

import com.zakgof.korender.impl.engine.Loader
import com.zakgof.korender.impl.engine.ModelDeclaration

internal interface InternalModel : AutoCloseable {

}

object ModelFactory {

    fun load(modelDeclaration: ModelDeclaration, loader: Loader): InternalModel? {
        when(modelDeclaration.resource.)
    }

}