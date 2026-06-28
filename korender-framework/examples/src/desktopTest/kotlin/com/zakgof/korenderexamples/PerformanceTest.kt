package com.zakgof.korenderexamples

import androidx.compose.ui.awt.ComposeWindow
import com.zakgof.korender.FrameInfo
import com.zakgof.korender.KorenderBuild
import com.zakgof.korender.examples.Case
import com.zakgof.korender.examples.GltfCrowdExample
import com.zakgof.korender.examples.HeightmapTerrainExample
import com.zakgof.korender.examples.ProcTerrainExample
import com.zakgof.korender.examples.TestExchange
import com.zakgof.korender.examples.infcity.InfiniteCity
import com.zakgof.korenderexamples.perf.InstancedRenderables
import com.zakgof.korenderexamples.perf.MultipleRenderables
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.time.LocalDateTime
import java.util.Locale
import javax.swing.SwingUtilities
import kotlin.concurrent.atomics.ExperimentalAtomicApi

class PerformanceTest {

    @OptIn(ExperimentalAtomicApi::class)
    @ParameterizedTest
    @MethodSource("performanceCases")
    fun performance(demo: Case) {

        TestExchange.report(null)

        SwingUtilities.invokeAndWait {
            window.setContent {
                demo.composable()
            }
        }

        val t1 = waitKorender(10f)
        val t2 = waitKorender(40f)

        val frameRate = (t2.frame - t1.frame) / (t2.time - t1.time)

        println("Framerate: ${demo.title} $frameRate")
        recordResult(demo.title, frameRate)

        // Fail if slower than latest release by more than 2%
        val releaseVal = latestRelease[demo.title]
        if (releaseVal != null) {
            if (frameRate < releaseVal * 0.98f) {
                fail<Unit>("Framerate for '${demo.title}' dropped vs release: $frameRate < $releaseVal (more than 2% slower)")
            }
        }
    }

    @AfterEach
    fun clearContent() {
        TestUtil.clearContent(window)
    }

    @OptIn(ExperimentalAtomicApi::class)
    private fun waitKorender(sec: Float): FrameInfo {
        return TestUtil.poll(timeout = sec + 2f) {
            val fi = TestExchange.fi.load()
            if (fi != null && fi.time > sec) fi else null
        }
    }

    companion object {
        private val resultsFolder = TestUtil.resultsFolder
        private val timestampFormat = TestUtil.timestampFormat
        private val results = mutableMapOf<String, Float>()
        private val latestReleaseFile = findLatestResultFile(resultsFolder, isSnapshot = false)
        private val latestSnapshotFile = findLatestResultFile(resultsFolder, isSnapshot = true)
        private val latestRelease = latestReleaseFile?.let { loadResultsFile(it) } ?: emptyMap()
        private val latestSnapshot = latestSnapshotFile?.let { loadResultsFile(it) } ?: emptyMap()
        private lateinit var window: ComposeWindow

        @JvmStatic
        @BeforeAll
        fun setupWindow() {
            window = TestUtil.createWindow()
        }

        @JvmStatic
        fun performanceCases() = listOf(
            Case("10K renderables", ::MultipleRenderables),
            Case("10K instanced renderables - dynamic", { InstancedRenderables(true) }),
            Case("10K instanced renderables - static", { InstancedRenderables(false) }),
            Case("City Demo", ::InfiniteCity),
            Case("Gltf Crowd", ::GltfCrowdExample),
            Case("Heightmap Terrain", ::HeightmapTerrainExample),
            Case("Procedural Terrain", ::ProcTerrainExample),
        ).map { Named.of(it.title, it) }

        @JvmStatic
        @AfterAll
        fun writeResults() {
            if (results.isEmpty()) return
            if (!resultsFolder.exists()) {
                resultsFolder.mkdirs()
            }

            if (latestSnapshotFile != null) {
                printComparisonTable(results, latestSnapshot, latestSnapshotFile)
            }
            if ( latestReleaseFile != null) {
                printComparisonTable(results, latestRelease, latestReleaseFile)
            }

            val version = KorenderBuild.version
            val timestamp = LocalDateTime.now().format(timestampFormat)
            val output = if (version.endsWith("-SNAPSHOT")) {
                File(resultsFolder, "${version}-$timestamp.perf.txt")
            } else {
                File(resultsFolder, "${version}.perf.txt")
            }

            val lines = results.entries
                .sortedBy { it.key }
                .joinToString(System.lineSeparator()) { "${it.key}\t${it.value}" }
            output.writeText(lines)
            TestUtil.disposeWindow(window)
        }

        @Synchronized
        private fun recordResult(title: String, frameRate: Float) {
            results[title] = frameRate
        }

        private fun loadResultsFile(file: File): Map<String, Float> {
            return file.readLines()
                .mapNotNull { line ->
                    val trimmed = line.trim()
                    if (trimmed.isEmpty()) return@mapNotNull null
                    val tabIndex = trimmed.lastIndexOf('\t')
                    val splitIndex = if (tabIndex >= 0) tabIndex else trimmed.lastIndexOf(' ')
                    if (splitIndex <= 0 || splitIndex >= trimmed.length - 1) return@mapNotNull null
                    val name = trimmed.substring(0, splitIndex).trim()
                    val value = trimmed.substring(splitIndex + 1).trim().toFloatOrNull() ?: return@mapNotNull null
                    if (name.isEmpty()) return@mapNotNull null
                    name to value
                }
                .toMap()
        }

        private fun findLatestResultFile(folder: File, isSnapshot: Boolean): File? {
            if (!folder.exists()) return null
            return folder.listFiles()
                ?.filter { it.isFile && it.name.endsWith(".perf.txt") }
                ?.filter { it.name.contains("-SNAPSHOT") == isSnapshot }
                ?.maxByOrNull { it.lastModified() }
        }

        private fun printComparisonTable(
            current: Map<String, Float>,
            latest: Map<String, Float>,
            latestFile: File,
        ) {
            val header = listOf("Test", "Current", latestFile.nameWithoutExtension, "Change")
            val rows = current.keys.sorted().map { title ->
                val cur = current[title]
                val prev = latest[title]
                val curStr = cur?.let { formatValue(it) } ?: "N/A"
                val prevStr = prev?.let { formatValue(it) } ?: "N/A"
                val changeStr = if (cur != null && prev != null && prev != 0f) {
                    val diff = (cur - prev) / prev * 100f
                    val sign = if (diff >= 0f) "+" else ""
                    val txt = String.format(Locale.US, "%s%.2f%%", sign, diff)
                    when {
                        diff < 0f -> ansiRed(txt)
                        diff > 0f -> ansiGreen(txt)
                        else -> txt
                    }
                } else {
                    "—"
                }
                listOf(title, curStr, prevStr, changeStr)
            }

            val widths = header.indices.map { col ->
                maxOf(
                    header[col].length,
                    rows.maxOfOrNull { visibleLength(it[col]) } ?: 0
                )
            }

            fun line(left: String, mid: String, right: String, fill: Char): String =
                widths.joinToString(mid, prefix = left, postfix = right) { fill.toString().repeat(it + 2) }

            fun row(values: List<String>): String =
                values.mapIndexed { i, v -> " ${padEndVisible(v, widths[i])} " }
                    .joinToString("|", prefix = "|", postfix = "|")

            val table = buildString {
                appendLine(line("┌", "┬", "┐", '─'))
                appendLine(row(header))
                appendLine(line("├", "┼", "┤", '─'))
                rows.forEach { appendLine(row(it)) }
                appendLine(line("└", "┴", "┘", '─'))
            }

            println(table)
        }

        private fun formatValue(value: Float): String =
            String.format(Locale.US, "%.2f", value)

        private fun ansiRed(text: String) = "\u001B[31m$text\u001B[0m"

        private fun ansiGreen(text: String) = "\u001B[32m$text\u001B[0m"

        private val ansiRegex = Regex("\u001B\\[[;\\d]*m")

        private fun visibleLength(value: String): Int = stripAnsi(value).length

        private fun stripAnsi(value: String): String = value.replace(ansiRegex, "")

        private fun padEndVisible(value: String, length: Int): String {
            val visible = visibleLength(value)
            val needed = length - visible
            return if (needed > 0) value + " ".repeat(needed) else value
        }

    }
}

