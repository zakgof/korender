package com.zakgof.korender.impl.material

class ShaderDebugInfo(val file: String) {

    private val PATTERN1 = Regex("^(\\d+)\\((\\d+)\\).+$")
    private val PATTERN2 = Regex("^.+: (\\d+):(\\d+):.+$")

    private val fileStack = mutableListOf<String>()
    private val srcLineStack = mutableListOf<Int>()
    private val lines: MutableList<DebugLineEntry> = ArrayList()
    private var srcLine = -1

    fun decorate(log: String): String {
        return log.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .flatMap {
                listOf(it, debug(it))
            }.joinToString("\n") { "       $it" }
    }

    private fun debug(line: String): String {
        var matcher = PATTERN1.find(line)
        if (matcher == null) {
            matcher = PATTERN2.find(line)
        }
        if (matcher != null) {
            // int col = Integer.parseInt(matcher.group(1));
            val row = matcher.groups[2]!!.value.toInt()
            val entry = lines[row - 1]
            val info = "[${entry.file}:${entry.srclineNo}  ${entry.line}]"
            return info
        }
        return ""
    }

    fun start(fname: String) {
        fileStack.add(fname)
        srcLineStack.add(srcLine)
        srcLine = 0
    }

    fun incSourceLine() {
        srcLine++
    }

    fun incDestLine(line: String) {
        lines.add(DebugLineEntry(srcLine, fileStack.peek(), line))
    }

    fun finish(fname: String?) {
        fileStack.pop()
        srcLine = srcLineStack.pop()
    }

    private class DebugLineEntry(
        val srclineNo: Int,
        val file: String,
        val line: String
    )
}
