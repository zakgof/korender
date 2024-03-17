//package com.zakgof.korender.examples
//
//import androidx.compose.runtime.Composable
//import com.zakgof.korender.Korender
//import com.zakgof.korender.Renderables
//import com.zakgof.korender.camera.DefaultCamera
//import com.zakgof.korender.geometry.Meshes
//import com.zakgof.korender.material.Materials
//import com.zakgof.korender.math.Transform
//import com.zakgof.korender.math.x
//import com.zakgof.korender.math.y
//import com.zakgof.korender.math.z
//import com.zakgof.korender.projection.FrustumProjection
//
//@Composable
//fun TexturingExample(): Unit = Korender {
//
//    camera = DefaultCamera(pos = 20.z, dir = -1.z, up = 1.y)
//    onResize = {
//        projection =
//            FrustumProjection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f)
//    }
//
//    val mesh = Meshes.sphere(2f).build(gpu)
//
//    val uvSphere = Renderables.create(
//        mesh = mesh,
//        material = Materials.standard(gpu) {
//            colorFile = "/sand.jpg"
//        })
//    add(uvSphere)
//
//    val tpSphere = Renderables.create(
//        mesh = mesh,
//        material = Materials.standard(gpu, "TRIPLANAR") {
//            colorFile = "/sand.jpg"
//            triplanarScale = 0.1f
//        })
//    add(tpSphere)
//
//    onFrame = {
//        uvSphere.transform = Transform().rotate(1.y, it.time * 0.1f).translate(-2.1f.x)
//        tpSphere.transform = Transform().rotate(1.y, it.time * 0.1f).translate(2.1f.x)
//    }
//}