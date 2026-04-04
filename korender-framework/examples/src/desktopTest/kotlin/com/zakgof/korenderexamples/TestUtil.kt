package com.zakgof.korenderexamples

import androidx.compose.ui.awt.ComposeWindow
import java.io.File
import java.time.format.DateTimeFormatter
import javax.swing.SwingUtilities

object TestUtil {

    val resultsFolder = File("D:\\kot\\dev\\assets")
    val timestampFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")

    fun createWindow(): ComposeWindow {
        lateinit var window: ComposeWindow
        SwingUtilities.invokeAndWait {
            window = ComposeWindow()
            window.setSize(800, 600)
            window.isVisible = true
        }
        return window
    }

    fun clearContent(window: ComposeWindow) {
        SwingUtilities.invokeAndWait {
            window.setContent { }
        }
        Thread.sleep(200)
    }

    fun disposeWindow(window: ComposeWindow) {
        SwingUtilities.invokeAndWait {
            window.isVisible = false
            window.setContent { }
            window.dispose()
        }
    }

    fun <T> poll(
        timeout: Float,
        pollMs: Long = 200,
        supplier: () -> T?
    ): T {
        val start = System.nanoTime()
        while (true) {
            val value = supplier()
            if (value != null) {
                return value
            }
            val elapsedSec = (System.nanoTime() - start) / 1_000_000_000.0
            if (elapsedSec > timeout) {
                throw IllegalStateException("Timed out waiting for test condition")
            }
            Thread.sleep(pollMs)
        }
    }
}
