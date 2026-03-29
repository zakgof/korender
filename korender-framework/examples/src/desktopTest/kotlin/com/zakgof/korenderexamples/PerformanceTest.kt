package com.zakgof.korenderexamples

import androidx.compose.ui.awt.ComposeWindow
import com.zakgof.korender.FrameInfo
import com.zakgof.korender.examples.Demo
import com.zakgof.korender.examples.TestUtils
import com.zakgof.korender.examples.pages
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.swing.SwingUtilities
import kotlin.concurrent.atomics.ExperimentalAtomicApi

class PerformanceTest {

    @OptIn(ExperimentalAtomicApi::class)
    @ParameterizedTest
    @MethodSource("demoPages")
    fun testComposeUI(demo: Demo) {

        TestUtils.report(null)

        SwingUtilities.invokeAndWait {
            window.setContent {
                demo.composable()
            }
        }

        val t1 = waitKorender(5f)
        val t2 = waitKorender(10f)

        val frameRate = (t2.frame - t1.frame) / (t2.time - t1.time)

        println("Framerate: ${demo.title} $frameRate")
        recordResult(demo.title, frameRate)

        baselineFrameRateFor(demo.title)?.let { baseline ->
            if (frameRate < baseline) {
                fail<Unit>("Framerate for '${demo.title}' dropped: $frameRate < $baseline")
            }
        }
    }

    @AfterEach
    fun clearContent() {
        SwingUtilities.invokeAndWait {
            window.setContent { }
        }
        Thread.sleep(200)
    }

    @OptIn(ExperimentalAtomicApi::class)
    private fun waitKorender(sec: Float): FrameInfo {
        val start = System.nanoTime()
        while (true) {
            val fi = TestUtils.fi.load()
            if (fi != null && fi.time > sec)
                return fi
            val elapsedSec = (System.nanoTime() - start) / 1_000_000_000.0
            if (elapsedSec > sec + 5f) {
                throw IllegalStateException("Timed out waiting for Korender frameInfo")
            }
            Thread.sleep(200)
        }
    }

    companion object {
        private val resultsFolder = File("D:\\kot\\dev\\assets")
        private val timestampFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
        private val results = mutableMapOf<String, Float>()
        private val baseline = loadBaseline(resultsFolder)
        private lateinit var window: ComposeWindow

        @JvmStatic
        @BeforeAll
        fun setupWindow() {
            SwingUtilities.invokeAndWait {
                window = ComposeWindow()
                window.setSize(800, 600)
                window.isVisible = true
            }
        }

        @JvmStatic
        fun demoPages(): List<Named<Demo>> =
            pages.map { demo -> Named.of(demo.title, demo) }

        @JvmStatic
        @AfterAll
        fun writeResults() {
            if (results.isEmpty()) return
            if (!resultsFolder.exists()) {
                resultsFolder.mkdirs()
            }
            val latestFile = findLatestResultFile(resultsFolder)
            val latest = latestFile?.let { loadResultsFile(it) } ?: emptyMap()
            printComparisonTable(results, baseline, latest)
            val timestamp = LocalDateTime.now().format(timestampFormat)
            val output = File(resultsFolder, "$timestamp.perf.txt")
            val lines = results.entries
                .sortedBy { it.key }
                .joinToString(System.lineSeparator()) { "${it.key}\t${it.value}" }
            output.writeText(lines)
            SwingUtilities.invokeAndWait {
                window.isVisible = false
                window.setContent { }
                window.dispose()
            }
        }

        @Synchronized
        private fun recordResult(title: String, frameRate: Float) {
            results[title] = frameRate
        }

        private fun baselineFrameRateFor(title: String): Float? = baseline[title]

        private fun loadBaseline(folder: File): Map<String, Float> {
            val baselineFile = File(folder, "baseline.perf.txt")
            if (!baselineFile.exists()) return emptyMap()
            return loadResultsFile(baselineFile)
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

        private fun findLatestResultFile(folder: File): File? {
            if (!folder.exists()) return null
            return folder.listFiles()
                ?.filter { it.isFile && it.name.endsWith(".perf.txt") && it.name != "baseline.perf.txt" }
                ?.maxByOrNull { it.lastModified() }
        }

        private fun printComparisonTable(
            current: Map<String, Float>,
            baseline: Map<String, Float>,
            latest: Map<String, Float>
        ) {
            val header = listOf("Test", "Current", "Baseline", "О” vs Base", "Latest", "О” vs Latest")
            val rows = current.keys.sorted().map { title ->
                val cur = current[title]
                val base = baseline[title]
                val lat = latest[title]
                val curStr = cur?.let { formatValue(it) } ?: "вЂ”"
                val baseStr = base?.let { formatValue(it) } ?: "вЂ”"
                val latStr = lat?.let { formatValue(it) } ?: "вЂ”"
                val baseDiff = formatDiffPercent(cur, base)
                val latestDiff = formatDiffPercent(cur, lat)
                listOf(title, curStr, baseStr, baseDiff, latStr, latestDiff)
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

        private fun formatDiffPercent(current: Float?, baseline: Float?): String {
            if (current == null || baseline == null || baseline == 0f) return "—"
            val diff = (current - baseline) / baseline * 100f
            val sign = if (diff >= 0f) "+" else ""
            val text = String.format(Locale.US, "%s%.2f%%", sign, diff)
            return when {
                diff < 0f -> ansiRed(text)
                diff > 0f -> ansiGreen(text)
                else -> text
            }
        }

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


