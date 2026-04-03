package com.zakgof.korenderexamples

import androidx.compose.ui.awt.ComposeWindow
import com.zakgof.app.resources.Res
import com.zakgof.korender.Image
import com.zakgof.korender.Korender
import com.zakgof.korender.context.FrameScope
import com.zakgof.korender.context.KorenderScope
import com.zakgof.korender.examples.TestExchange
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import com.zakgof.korenderexamples.golden.allInOne
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.awt.image.BufferedImage
import java.io.File
import java.time.LocalDateTime
import javax.imageio.ImageIO
import javax.swing.SwingUtilities
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.math.roundToInt


class GolderImageCase(
    val title: String,
    val init: KorenderScope.() -> Unit,
    val frame: FrameScope.() -> Unit,
)

class GoldenImageTest {

    @OptIn(ExperimentalAtomicApi::class, ExperimentalCoroutinesApi::class)
    @ParameterizedTest
    @MethodSource("goldenImageCases")
    fun testGoldenImage(case: GolderImageCase) {

        TestExchange.screenshot(null)

        SwingUtilities.invokeAndWait {
            window.setContent {
                Korender(appResourceLoader = { Res.readBytes(it) }) {
                    case.init.invoke(this)
                    val captured = captureFrame(
                        1024, 1024,
                        camera = camera(10.z + 3.y, -1.z, 1.y),
                        projection = projection(2f, 2f, 1f, 200f, frustum())
                    ) {
                        case.frame.invoke(this)
                    }
                    Frame {
                        if (captured.isCompleted) {
                            // TestExchange.screenshot(captured.getCompleted())
                        }
                        camera = camera(10.z + 3.y, -1.z, 1.y)
                        projection = projection(2f, 2f, 1f, 200f, frustum())

                        case.frame.invoke(this)
                    }
                }
            }
        }

        val screenshot = waitScreenshot(30f)
        val actualImage = toBufferedImage(screenshot)
        val timestamp = LocalDateTime.now().format(timestampFormat)
        val safeName = safeFileName(case.title)
        val tempFolder = File(System.getProperty("java.io.tmpdir"), "korender-golden")
        if (!tempFolder.exists()) {
            tempFolder.mkdirs()
        }
        val actualFile = File(tempFolder, "${safeName}_$timestamp.png")
        ImageIO.write(actualImage, "png", actualFile)

        if (!resultsFolder.exists()) {
            resultsFolder.mkdirs()
        }
        val baselineFile = File(resultsFolder, "$safeName.png")
        if (!baselineFile.exists()) {
            ImageIO.write(actualImage, "png", baselineFile)
            println("Baseline created: ${baselineFile.absolutePath}")
            return
        }

        val goldenImage = ImageIO.read(baselineFile)
        if (!imagesMatch(actualImage, goldenImage)) {
            fail<Unit>("Golden image mismatch for '${case.title}' \n" +
                    "Actual: ${actualFile.toURI()}\n" +
                    "Golden: ${baselineFile.toURI()}")
        }
    }

    @AfterEach
    fun clearContent() {
        TestUtil.clearContent(window)
    }

    @OptIn(ExperimentalAtomicApi::class)
    private fun waitScreenshot(timeout: Float): Image {
        return TestUtil.poll(timeout = timeout) {
            TestExchange.screenshotStore.load()
        }
    }

    private fun toBufferedImage(image: Image): BufferedImage {
        val buffered = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB)
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val c = image.pixel(x, y)
                val r = (c.r * 255f).roundToInt().coerceIn(0, 255)
                val g = (c.g * 255f).roundToInt().coerceIn(0, 255)
                val b = (c.b * 255f).roundToInt().coerceIn(0, 255)
                val a = (c.a * 255f).roundToInt().coerceIn(0, 255)
                val argb = (a shl 24) or (r shl 16) or (g shl 8) or b
                buffered.setRGB(x, image.height - 1 - y, argb)
            }
        }
        return buffered
    }

    private fun imagesMatch(actual: BufferedImage, golden: BufferedImage): Boolean {
        if (actual.width != golden.width || actual.height != golden.height) {
            return false
        }
        for (y in 0 until actual.height) {
            for (x in 0 until actual.width) {
                if (actual.getRGB(x, y) != golden.getRGB(x, y)) {
                    return false
                }
            }
        }
        return true
    }

    private fun safeFileName(name: String): String =
        name.replace(Regex("""[<>:"/\\|?*]+"""), "_").trim().ifEmpty { "untitled" }

    companion object {
        private val resultsFolder = TestUtil.resultsFolder
        private val timestampFormat = TestUtil.timestampFormat
        private lateinit var window: ComposeWindow

        @JvmStatic
        @BeforeAll
        fun setupWindow() {
            window = TestUtil.createWindow()
        }

        @JvmStatic
        fun goldenImageCases() = listOf(allInOne).map { Named.of(it.title, it) }

        @JvmStatic
        @AfterAll
        fun disposeWindow() = TestUtil.disposeWindow(window)

    }
}
