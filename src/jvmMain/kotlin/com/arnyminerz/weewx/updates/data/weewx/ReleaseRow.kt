package com.arnyminerz.weewx.updates.data.weewx

import com.arnyminerz.weewx.utils.semVer
import com.vdurmont.semver4j.Semver
import java.time.ZonedDateTime

data class ReleaseRow(
    val filename: String,
    val size: FileSize,
    val date: ZonedDateTime
) {
    companion object {
        private val EXTENSIONS = "(\\.tar\\.gz|\\.rhel\\.noarch\\.rpm|\\.suse\\d*\\.noarch\\.rpm|\\.deb|_all|-1|python|python3|-|_|weewx)"
    }

    val version: Semver get() {
        val noExtensionFilename = filename.replace(EXTENSIONS.toRegex(), "")
        val numberIndex = noExtensionFilename.indexOfFirst { it.isDigit() }
        val versionName = noExtensionFilename.substring(numberIndex).substringBefore('-')
        return versionName.semVer
    }
}
