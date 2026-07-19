package com.piliplus.recodeing

import android.content.Context
import android.os.Build
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DebugLog {
    private const val Tag = "reliqliquid"
    private lateinit var logFile: File

    fun install(context: Context) {
        val directory = File(context.filesDir, "logs").apply { mkdirs() }
        logFile = File(directory, "reliqliquid-debug.log")
        trimIfNeeded()
        write("INFO", "Logger installed; file=${logFile.absolutePath}")
        write(
            "INFO",
            "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT}); " +
                "device=${Build.MANUFACTURER} ${Build.MODEL}",
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
        val line = "${timestamp()} [$level] $message"
        if (level == "ERROR" || level == "FATAL") Log.e(Tag, line, throwable) else Log.i(Tag, line)
        if (!::logFile.isInitialized) return
        runCatching {
            logFile.appendText(buildString {
                appendLine(line)
                throwable?.let {
                    val output = StringWriter()
                    it.printStackTrace(PrintWriter(output))
                    appendLine(output.toString())
                }
            })
        }
    }

    private fun timestamp(): String = SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
        Locale.US,
    ).format(Date())

    private fun trimIfNeeded() {
        if (!logFile.exists() || logFile.length() <= 2L * 1024L * 1024L) return
        runCatching {
            val tail = logFile.readText().takeLast(512 * 1024)
            logFile.writeText("Previous log truncated.\n$tail")
        }
    }
}
