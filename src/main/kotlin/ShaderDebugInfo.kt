package com.zakgof.korender

import java.util.*
import java.util.regex.Pattern

class ShaderDebugInfo(val file: String) {

    companion object {
        private val PATTERN1: Pattern = Pattern.compile("^(\\d+)\\((\\d+)\\).+$")
        private val PATTERN2: Pattern = Pattern.compile("^.+: (\\d+):(\\d+):.+$")
    }

    private val fileStack = Stack<String>()
    private val srcLineStack = Stack<Int>()
    private val lines: MutableList<DebugLineEntry> = ArrayList()
    private var srcLine = -1

    fun decorate(log: String): String {
        val sb = StringBuilder()
        Scanner(log).use { scanner ->
            while (scanner.hasNextLine()) {
                val line = scanner.nextLine()
                sb.append(line).append("\n").append(debug(line)).append("\n\n")
            }
        }
        return sb.toString()
    }

    private fun debug(line: String): String {
        var matcher = PATTERN1.matcher(line)
        if (!matcher.matches()) {
            matcher = PATTERN2.matcher(line)
        }
        if (matcher.matches()) {
            // int col = Integer.parseInt(matcher.group(1));
            val row = matcher.group(2).toInt()
            val entry = lines[row - 1]
            val info = entry.file + ":" + entry.srclineNo + "   " + entry.line
            return info
        }
        return "[Error parsing shader log]"
    }

    fun start(fname: String) {
        fileStack.push(fname)
        srcLineStack.push(srcLine)
        srcLine = 0
    }

    fun incSourceLine() {
        srcLine++
    }

    fun incDestLine(line: String?) {
        lines.add(DebugLineEntry(srcLine, fileStack.peek(), line))
        // System.err.println(lines.size() + " " + fileStack.peek() + ":" + srcLine + " " + line);
    }

    fun finish(fname: String?) {
        fileStack.pop()
        srcLine = srcLineStack.pop()
    }

    private class DebugLineEntry(
        internal val srclineNo: Int = 0,
        internal val file: String? = null,
        internal val line: String? = null
    )
}
