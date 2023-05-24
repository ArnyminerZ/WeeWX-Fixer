package com.arnyminerz.weewx.configuration

import com.arnyminerz.weewx.remote.Client
import java.io.File

class Instance(file: File): Config(file) {
    val client: Client by lazy {
        Client(
            getValue("hostname"),
            getValue("username"),
            getValue("password"),
            getValue("port").toInt()
        )
    }

    val instanceDirectory: File get() = file.parentFile

    suspend fun downloadDatabase(target: File) {
        if (target.exists()) target.delete()

        println("Downloading database for $name into $target")
        client.use {
            download(getValue("database")).use { stream ->
                stream.copyTo(target.outputStream())
            }
        }
        println("Database downloaded.")
    }
}