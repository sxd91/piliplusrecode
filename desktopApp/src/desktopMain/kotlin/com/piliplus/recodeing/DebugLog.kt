package com.piliplus.recodeing

import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.time.Instant

object DebugLog {
    private val logFile: File by lazy {
        val preferred = File("C:/reliqliquid-debug.log")
        runCatching {
            preferred.parentFile?.mkdirs()
            if (!preferred.exists()) preferred.createNewFile()
            preferred
        }.getOrElse {
            val fallback = File(System.getProperty("user.home"), "reliqliquid-debug.log")
            fallback.parentFile?.mkdirs()
            fallback
        }
    }

    fun install() {
        trimIfNeeded()
        write("INFO", "Logger installed; file=${logFile.absolutePath}")
        write(
            "INFO",
            "OS=${System.getProperty("os.name")} ${System.getProperty("os.version")}; " +
                "Java=${System.getProperty("java.version")}; arch=${System.getProperty("os.arch")}",
        )
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            write("FATAL", "Uncaught exception on ${thread.name}", throwable)
            previous?.uncaughtException(thread, throwable)
        }
    }

    fun info(message: String) = write("INFO", message)

    fun error(message: String, throwable: Throwable? = null) = write("ERROR", message, throwable)

    private fun write(level: String, message: String, throwable: Throwable? = null) {
        val text = buildString {
            appendLine("${Instant.now()} [$level] $message")
            throwable?.let {
                val output = StringWriter()
                it.printStackTrace(PrintWriter(output))
                appendLine(output.toString())
            }
        }
        runCatching { logFile.appendText(text) }
        if (level == "ERROR" || level == "FATAL") System.err.print(text) else System.out.print(text)
    }

    private fun trimIfNeeded() {
        if (!logFile.exists() || logFile.length() <= 2L * 1024L * 1024L) return
        runCatching {
            val tail = logFile.readText().takeLast(512 * 1024)
            logFile.writeText("Previous log truncated.\n$tail")
        }
    }
}
