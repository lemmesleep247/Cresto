package com.nevoit.cresto.feature.screenextract

import android.content.Context
import android.content.pm.PackageManager
import com.nevoit.cresto.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

class ShizukuScreenshotCapturer(private val context: Context) {
    suspend fun collapsePanelsAndCapturePng(delayMillis: Long = DEFAULT_CAPTURE_DELAY_MS): ByteArray =
        withContext(Dispatchers.IO) {
            ensurePermission()
            runShellCommand("cmd statusbar collapse")
            Thread.sleep(delayMillis)
            runShellCommand("screencap -p")
        }

    suspend fun capturePng(): ByteArray = withContext(Dispatchers.IO) {
        ensurePermission()
        runShellCommand("screencap -p")
    }

    fun hasPermission(): Boolean {
        return runCatching {
            Shizuku.pingBinder() &&
                (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED)
        }.getOrDefault(defaultValue = false)
    }

    fun requestPermission(requestCode: Int = REQUEST_CODE) {
        if (!Shizuku.pingBinder()) {
            throw IllegalStateException(context.getString(R.string.error_shizuku_not_running))
        }
        Shizuku.requestPermission(requestCode)
    }

    private fun ensurePermission() {
        if (!Shizuku.pingBinder()) {
            throw IllegalStateException(context.getString(R.string.error_shizuku_not_running_start))
        }
        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            throw SecurityException(context.getString(R.string.error_shizuku_permission_denied))
        }
    }

    private fun runShellCommand(command: String): ByteArray {
        val process = createRemoteProcess(arrayOf("sh", "-c", command))
        return process.useProcess { shellProcess ->
            val output = shellProcess.inputStream.use { it.readBytes() }
            val error = shellProcess.errorStream.use { it.readBytes().toString(Charsets.UTF_8).trim() }
            val exitCode = shellProcess.waitFor()
            if (exitCode != 0) {
                throw IllegalStateException(
                    error.ifBlank {
                        context.getString(R.string.error_command_failed, exitCode, command)
                    }
                )
            }
            output
        }
    }

    private fun createRemoteProcess(command: Array<String>): Process {
        val method = Shizuku::class.java.getDeclaredMethod(
            "newProcess",
            Array<String>::class.java,
            Array<String>::class.java,
            String::class.java
        ).apply { isAccessible = true }
        return method.invoke(null, command, null, null) as Process
    }

    private inline fun <T> Process.useProcess(block: (Process) -> T): T {
        return try {
            block(this)
        } finally {
            destroy()
        }
    }

    companion object {
        const val REQUEST_CODE = 260517
        private const val DEFAULT_CAPTURE_DELAY_MS = 800L
    }
}
