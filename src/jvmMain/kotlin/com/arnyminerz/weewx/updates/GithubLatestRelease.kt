package com.arnyminerz.weewx.updates

import com.arnyminerz.weewx.serialization.JsonSerializer
import org.json.JSONObject
import java.time.ZonedDateTime

data class GithubLatestRelease(
    val url: String,
    val assetsUrl: String,
    val uploadUrl: String,
    val htmlUrl: String,
    val id: Long,
    val author: Author,
    val nodeId: String,
    val tagName: String,
    val targetCommitish: String,
    val name: String,
    val draft: Boolean,
    val prerelease: Boolean,
    val createdAt: ZonedDateTime,
    val publishedAt: ZonedDateTime,
    val assets: List<Asset>,
    val tarballUrl: String,
    val zipballUrl: String,
    val body: String
) {
    companion object: JsonSerializer<GithubLatestRelease> {
        override fun fromJson(json: JSONObject): GithubLatestRelease = GithubLatestRelease(
            json.getString("url"),
            json.getString("assets_url"),
            json.getString("upload_url"),
            json.getString("html_url"),
            json.getLong("id"),
            json.getJSONObject("author").let { Author.fromJson(it) },
            json.getString("node_id"),
            json.getString("tag_name"),
            json.getString("target_commitish"),
            json.getString("name"),
            json.getBoolean("draft"),
            json.getBoolean("prerelease"),
            json.getString("created_at").let { ZonedDateTime.parse(it) },
            json.getString("published_at").let { ZonedDateTime.parse(it) },
            json.getJSONArray("assets").let { j -> (0 until j.length()).map { j.getJSONObject(it) } }.map { Asset.fromJson(it) },
            json.getString("tarball_url"),
            json.getString("zipball_url"),
            json.getString("body")
        )
    }


    data class Author(
        val login: String,
        val id: Long,
        val nodeId: String,
        val avatarUrl: String,
        val gravatarId: String,
        val url: String,
        val htmlUrl: String,
        val followersUrl: String,
        val followingUrl: String,
        val gistsUrl: String,
        val starredUrl: String,
        val subscriptionsUrl: String,
        val organizationsUrl: String,
        val reposUrl: String,
        val eventsUrl: String,
        val receivedEventsUrl: String,
        val type: String,
        val admin: Boolean
    ) {
        companion object: JsonSerializer<Author> {
            override fun fromJson(json: JSONObject): Author = Author(
                json.getString("login"),
                json.getLong("id"),
                json.getString("node_id"),
                json.getString("avatar_url"),
                json.getString("gravatar_id"),
                json.getString("url"),
                json.getString("html_url"),
                json.getString("followers_url"),
                json.getString("following_url"),
                json.getString("gists_url"),
                json.getString("starred_url"),
                json.getString("subscriptions_url"),
                json.getString("organizations_url"),
                json.getString("repos_url"),
                json.getString("events_url"),
                json.getString("received_events_url"),
                json.getString("type"),
                json.getBoolean("site_admin")
            )
        }
    }

    data class Asset(
        val url: String,
        val id: Long,
        val nodeId: String,
        val name: String,
        val label: String,
        val uploader: Author,
        val contentType: String,
        val state: String,
        val size: Long,
        val downloadCount: Long,
        val createdAt: ZonedDateTime,
        val updatedAt: ZonedDateTime,
        val browserDownloadUrl: String
    ) {
        companion object: JsonSerializer<Asset> {
            override fun fromJson(json: JSONObject): Asset = Asset(
                json.getString("url"),
                json.getLong("id"),
                json.getString("node_id"),
                json.getString("name"),
                json.getString("label"),
                json.getJSONObject("uploader").let { Author.fromJson(it) },
                json.getString("content_type"),
                json.getString("state"),
                json.getLong("size"),
                json.getLong("download_count"),
                json.getString("created_at").let { ZonedDateTime.parse(it) },
                json.getString("updated_at").let { ZonedDateTime.parse(it) },
                json.getString("browser_download_url")
            )
        }
    }
}
