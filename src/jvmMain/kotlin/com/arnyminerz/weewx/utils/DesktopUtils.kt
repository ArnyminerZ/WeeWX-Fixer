package com.arnyminerz.weewx.utils

import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.net.URI

object DesktopUtils {
    private val desktop: Desktop by lazy { Desktop.getDesktop() }

    /**
     * Uses the default browser to launch the given URL.
     * @return `true` if the url could be launched, `false` otherwise.
     */
    fun browse(url: URI): Boolean {
        if (!Desktop.isDesktopSupported()) return false
        return try {
            desktop.browse(url)
            true
        } catch (e: IOException) {
            false
        }
    }

    /**
     * Uses the default browser to launch the given URL.
     * @return `true` if the url could be launched, `false` otherwise.
     */
    fun browse(url: String): Boolean = browse(URI.create(url))

    /**
     * Launches the associated application to open the file.
     *
     * If the specified file is a directory, the file manager of the current platform is launched to open it.
     * @return `true` if the file could be opened, `false` otherwise.
     */
    fun open(file: File): Boolean {
        if (!Desktop.isDesktopSupported()) return false
        return try {
            desktop.open(file)
            true
        } catch (e: IOException) {
            false
        }
    }

    /**
     * Launches the associated application to open the file.
     *
     * If the specified file is a directory, the file manager of the current platform is launched to open it.
     * @param otherwise Gets called if the file could not be opened.
     */
    fun open(file: File, otherwise: () -> Unit) {
        if (!open(file)) otherwise()
    }
}
