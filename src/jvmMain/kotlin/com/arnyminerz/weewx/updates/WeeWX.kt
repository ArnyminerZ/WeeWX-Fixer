package com.arnyminerz.weewx.updates

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.arnyminerz.weewx.updates.data.weewx.FileSize
import com.arnyminerz.weewx.updates.data.weewx.ReleaseRow
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.time.ZoneId

object WeeWX {
    private const val RELEASED_VERSIONS_URL = "https://weewx.com/downloads/released_versions/"

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm")

    private val latestReleaseMutable = mutableStateOf<ReleaseRow?>(null)
    val latestRelease: State<ReleaseRow?> get() = latestReleaseMutable

    suspend fun getReleasedVersions() {
        val doc = Jsoup.connect(RELEASED_VERSIONS_URL).get()
        val listBody = doc.selectFirst("#list")?.selectFirst("tbody")
        val rowElements = listBody?.select("tr")?.toList() ?: return
        val versionRows = rowElements
            // Filter all rows that are not versions
            .filter { it.getElementsByTag("a").text().contains("weewx", true) }
            // Map the versions
            .map { element ->
                val data = element.getElementsByTag("td")
                ReleaseRow(
                    filename = data[0].text(),
                    size = FileSize(data[1].text()),
                    date = dateFormatter.parse(data[2].text()).toInstant().atZone(ZoneId.of("UTC")),
                )
            }
            // Sort by date
            .sortedBy { it.date }

        val latestRelease = versionRows.last()
        latestReleaseMutable.value = latestRelease

        println("Latest WeeWX release: ${latestRelease.version}")
    }
}