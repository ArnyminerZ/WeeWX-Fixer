package com.arnyminerz.weewx.configuration

import java.io.File

open class Config(
    val file: File
): ReadonlyConfig(file.readText()) {
    private lateinit var data: Map<String, String>

    val name: String get() = file.nameWithoutExtension

    override fun toString(): String = file.absolutePath

    /**
     * Updates [file] with the contents of [data].
     */
    private fun save() {
        if (file.exists()) file.delete()

        val dataString = data
            // Convert to a list of pairs
            .asSequence()
            // Convert each element from Key-Value to String
            .map { (k, v) -> "$k=$v" }
            // Join using line separator character
            .joinToString(System.lineSeparator())
        file.writeText(dataString)
    }

    operator fun set(key: String, value: String?) {
        data = data.toMutableMap().apply {
            if (value != null)
                set(key, value)
            else
                remove(key)
        }
        save()
    }

}