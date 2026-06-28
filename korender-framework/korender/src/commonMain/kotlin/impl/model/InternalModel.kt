package com.zakgof.korender.impl.model

import com.zakgof.korender.KorenderException
import com.zakgof.korender.impl.engine.Loader
import com.zakgof.korender.impl.engine.ModelDeclaration
import com.zakgof.korender.impl.engine.ResultKeeper
import com.zakgof.korender.impl.engine.SceneDeclaration
import com.zakgof.korender.impl.model.gltf.InternalLoadedGltfModel
import com.zakgof.korender.impl.model.kr.KrScene
import com.zakgof.korender.impl.model.obj.ObjScene

internal fun interface InternalModel : AutoCloseable {
    fun build(modelDeclaration: ModelDeclaration, sceneDeclaration: SceneDeclaration, rk: ResultKeeper?)
    override fun close() {}
}

internal object ModelFactory {

    fun load(modelDeclaration: ModelDeclaration, loader: Loader): InternalModel {
        val extension = modelDeclaration.resource.substringAfterLast('.', "").lowercase()
        return when (extension) {
            "glb", "gltf" -> InternalLoadedGltfModel(modelDeclaration)
            "obj" -> ObjScene(modelDeclaration)
            "kr" -> KrScene(modelDeclaration)
            else -> throw KorenderException("Unknown model extension: ${modelDeclaration.resource}")
        }
    }
}
