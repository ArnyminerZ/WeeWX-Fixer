package com.arnyminerz.weewx.utils

import java.io.File
import java.math.BigInteger
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.*
import java.security.MessageDigest
import kotlin.io.path.name

fun Path.watchForChanges(onChange: Path.() -> Unit) {
    // We watch the parent directory for changes
    val path = parent
    val watchService = FileSystems.getDefault().newWatchService()
    doAsync {
        path.register(watchService, ENTRY_MODIFY, ENTRY_DELETE, ENTRY_CREATE)

        var looping = true
        while (looping) {
            val wk = watchService.take()
            for (event in wk.pollEvents()) {
                val changed = event.context() as Path
                if (changed.endsWith(name)) onChange(changed)
            }
            // Reset the key
            if (!wk.reset()) looping = false
        }
    }.invokeOnCompletion { watchService.close() }
}

/**
 * Calculates the SHA-256 checksum for the given file.
 * @return The calculated hash, or `null` if the file doesn't exist or the operation is not supported.
 */
fun File.checksum(): String? {
    if (!exists()) return null

    return try {
        val data = readBytes()
        val hash = MessageDigest.getInstance("SHA-256").digest(data)
        BigInteger(1, hash).toString(16)
    } catch (e: Exception) {
        System.err.println("Could not calculate checksum for $path. Error: ${e.message}")
        null
    }
}
