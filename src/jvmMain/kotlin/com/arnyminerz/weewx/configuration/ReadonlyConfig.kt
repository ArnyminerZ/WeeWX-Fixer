package com.arnyminerz.weewx.configuration

open class ReadonlyConfig(private val rawData: String) {
    private lateinit var data: Map<String, String>

    init {
        load()
    }

    /**
     * Loads the data from [rawData] and updates [data].
     */
    private fun load() {
        data = rawData
            .split(System.lineSeparator())
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

    operator fun get(key: String): String? = data[key]

    fun getValue(key: String): String = data.getValue(key)

    fun get(key: String, default: String): String = data[key] ?: default

    fun has(key: String): Boolean = data.containsKey(key)
}