package com.zakgof.korender.examples

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.PixelFormat
import com.zakgof.korender.math.ColorRGB
import com.zakgof.korender.math.Quaternion
import com.zakgof.korender.math.Transform.Companion.rotate
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.z
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.camera.CAMERA
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.PermissionsControllerFactory
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import java.util.concurrent.Executors

@Composable
fun CameraExample() = Box {

    val analysis = ImageAnalysis.Builder()
        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
        .setBackpressureStrategy(
            ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
        )
        .build()

    var camWidth by remember { mutableIntStateOf(128) }
    var camHeight by remember { mutableIntStateOf(128) }
    var lastFrame by remember { mutableStateOf<ByteArray?>(null) }

    analysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
        camWidth = imageProxy.width
        camHeight = imageProxy.height
        val buffer = imageProxy.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        lastFrame = bytes
        imageProxy.close()
    }

    val factory: PermissionsControllerFactory = rememberPermissionsControllerFactory()
    val controller: PermissionsController = remember(factory) { factory.createPermissionsController() }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(null) {
        controller.providePermission(Permission.CAMERA)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, analysis)
        }, ContextCompat.getMainExecutor(context))
    }

    BindEffect(controller)

    Korender(resourceLoader = { Res.readBytes("files/$it") }) {


        Frame {

            AmbientLight(ColorRGB.White)
            DirectionalLight(-1.z, ColorRGB.white(3f))
            Renderable(
                base {
                    colorTexture = texture("cam-$camWidth-$camHeight", {
                        createImage(camWidth, camHeight, PixelFormat.RGBA, lastFrame)
                    })
                },
                mesh = cube(2f),
                transform = rotate(Quaternion.fromAxisAngle(Vec3(1f, 1f, 1f).normalize(), frameInfo.time))
            )

            Gui {
                Column {
                    Filler()
                    Text(id = "fps", text = "FPS ${frameInfo.avgFps.toInt()}")
                }
            }
        }
    }
}


