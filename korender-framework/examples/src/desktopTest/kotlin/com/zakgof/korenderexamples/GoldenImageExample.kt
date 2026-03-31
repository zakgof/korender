package com.zakgof.korenderexamples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import com.zakgof.korender.examples.TestUtils
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun GoldenImageExample() = Korender(appResourceLoader = { Res.readBytes(it) }) {

    val captured = captureFrame(1024, 1024,
        camera = camera(10.z, -1.z, 1.y),
        projection = projection(2f, 2f, 1f, 20f, frustum())
    ) {

    }

    Frame {
        if (captured.isCompleted) {
            TestUtils.screenshot(captured.getCompleted())
        }
    }
}

