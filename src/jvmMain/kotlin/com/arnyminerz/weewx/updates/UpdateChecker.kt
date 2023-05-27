package com.arnyminerz.weewx.updates

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.arnyminerz.weewx.utils.semVer
import com.vdurmont.semver4j.Semver
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object UpdateChecker {
    private const val GITHUB_API_URL = "https://api.github.com/repos/ArnyminerZ/WeeWX-Fixer/releases/latest"

    private val newVersionAvailableMutable = mutableStateOf<GithubLatestRelease?>(null)
    val newVersionAvailable: State<GithubLatestRelease?> get() = newVersionAvailableMutable

    /**
     * Returns the version of the app currently installed.
     */
    fun appVersion(): Semver {
        return System.getProperty("jpackage.app-version").semVer
    }

    suspend fun checkForUpdates() {
        val version = System.getProperty("jpackage.app-version").semVer
        println("Checking for updates... Current version: $version")
        val url = URL(GITHUB_API_URL).openConnection() as HttpsURLConnection
        try {
            url.connect()

            val bytes = url.inputStream.readAllBytes()
            val result = String(bytes)
            val json = JSONObject(result)
            val release = GithubLatestRelease.fromJson(json)
            val latestVersion = release.tagName.semVer
            println("Latest version: $latestVersion")

            if (latestVersion > version) {
                println("There's a new version available!")
                newVersionAvailableMutable.value = release
            }
        } finally {
            url.disconnect()
        }
    }
}