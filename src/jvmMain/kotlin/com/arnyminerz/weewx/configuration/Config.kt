package com.arnyminerz.weewx.configuration

import java.io.File

open class Config(
    val file: File
) {
    private lateinit var data: Map<String, String>

    init {
        load()
    }

    val name: String get() = file.nameWithoutExtension

    override fun toString(): String = file.absolutePath

    /**
     * Loads the data from [file] and updates [data].
     */
    private fun load() {
        if (!file.exists()) throw IllegalArgumentException("file doesn't exist: $file")
        if (!file.isFile) throw IllegalArgumentException("file is not a file: $file")

        data = file.readLines()
            // Convert to sequence for performance
            .asSequence()
            // Trim lines
            .map { it.trimStart() }
            // Ignore lines starting with # (comments)
            .filter { !it.startsWith('#') }
            // Split properties
            .map { it.split('=') }
            // Ignore invalid properties
            .filter { it.size > 1 }
            // Create pairs and convert to map
            .associate { it[0] to it.subList(1, it.size).joinToString("=") }
    }

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

    operator fun get(key: String): String? = data[key]

    fun getValue(key: String): String = data.getValue(key)

    fun get(key: String, default: String): String = data[key] ?: default

    operator fun set(key: String, value: String?) {
        data = data.toMutableMap().apply {
            if (value != null)
                set(key, value)
            else
                remove(key)
        }
        save()
    }

    fun has(key: String): Boolean = data.containsKey(key)
}