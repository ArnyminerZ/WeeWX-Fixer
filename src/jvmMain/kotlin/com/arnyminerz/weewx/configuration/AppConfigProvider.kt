package com.arnyminerz.weewx.configuration

import java.io.File
import java.io.FileFilter

object AppConfigProvider {
    private val userHomeDir = File(System.getProperty("user.home"))
    private val configRootPath = File(userHomeDir, ".weewx-fixer")
    private val configFile = File(configRootPath, "config.properties")

    private val configInstancesPath = File(configRootPath, "instances")

    private val config by lazy { Config(configFile) }

    init {
        if (!configInstancesPath.isDirectory) configInstancesPath.deleteRecursively()
        if (!configInstancesPath.exists()) configInstancesPath.mkdirs()
        if (!configFile.exists()) configFile.createNewFile()

        // Call config so it gets initialized
        config
    }

    /**
     * @throws IllegalArgumentException If the provided config is not valid.
     */
    fun importInstance(config: Config) {
        val hostname = config["hostname"]
            ?: throw IllegalArgumentException("Config file ($config) doesn't contain a \"hostname\" field")
        val username = config["username"]
            ?: throw IllegalArgumentException("Config file ($config) doesn't contain a \"username\" field")
        val password = config["password"]
            ?: throw IllegalArgumentException("Config file ($config) doesn't contain a \"password\" field")
        val database = config["database"]
            ?: throw IllegalArgumentException("Config file ($config) doesn't contain a \"database\" field")
        val portStr =
            config["port"] ?: throw IllegalArgumentException("Config file ($config) doesn't contain a \"port\" field")
        val port = portStr.toIntOrNull() ?: throw IllegalArgumentException("The provided port ($portStr) is not valid.")

        val target = File(configInstancesPath, config.name + ".properties")
        config.file.copyTo(target, overwrite = true)
    }

    fun listInstances(): List<Instance> {
        val files = configInstancesPath
            // Get all the files in the directory
            .listFiles(FileFilter { it.isFile && it.extension == "properties" })!!
        // Load their configs
        return files.map { Instance(it) }
    }
}