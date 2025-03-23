package app.echoirx.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubRelease(
    val id: Long,
    @SerialName("tag_name") val tagName: String,
    val name: String,
    val body: String = "",
    @SerialName("html_url") val htmlUrl: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("published_at") val publishedAt: String,
    @SerialName("prerelease") val isPrerelease: Boolean = false,
    val assets: List<GithubAsset> = emptyList()
)

@Serializable
data class GithubAsset(
    val id: Long,
    val name: String,
    val size: Long,
    @SerialName("browser_download_url") val browserDownloadUrl: String,
    @SerialName("content_type") val contentType: String
)