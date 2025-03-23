package app.echoirx.data.remote.api

import app.echoirx.data.remote.model.GithubRelease
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.headers
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentLength
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GithubApiService @Inject constructor(
    private val client: HttpClient
) {
    companion object {
        private const val TAG = "GithubApiService"
        private const val API_URL = "https://api.github.com"
        private const val REPO_OWNER = "imjyotiraditya"
        private const val REPO_NAME = "EchoirX"
    }

    /**
     * Fetches the latest release from GitHub
     */
    suspend fun getLatestRelease(): GithubRelease? = withContext(Dispatchers.IO) {
        try {
            client.get("$API_URL/repos/$REPO_OWNER/$REPO_NAME/releases/latest") {
                contentType(ContentType.Application.Json)
                headers {
                    append("Accept", "application/vnd.github.v3+json")
                }
            }.body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getContentLength(url: String): Long = withContext(Dispatchers.IO) {
        try {
            val response = client.head(url)
            if (response.status.isSuccess()) {
                response.contentLength() ?: -1
            } else {
                -1
            }
        } catch (_: Exception) {
            -1
        }
    }

    suspend fun downloadFile(
        url: String,
        outputStream: OutputStream,
        progressCallback: (Float) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            var bytesDownloaded = 0L
            val contentLength = getContentLength(url)

            client.prepareGet(url).execute { response ->
                if (response.status == HttpStatusCode.OK) {
                    val totalBytes = if (contentLength <= 0)
                        response.contentLength() ?: -1L
                    else
                        contentLength

                    val channel = response.bodyAsChannel()
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)

                    while (!channel.isClosedForRead) {
                        val bytesRead = channel.readAvailable(buffer, 0, buffer.size)
                        if (bytesRead < 0) break

                        outputStream.write(buffer, 0, bytesRead)

                        bytesDownloaded += bytesRead

                        if (totalBytes > 0) {
                            val progress = bytesDownloaded.toFloat() / totalBytes.toFloat()
                            progressCallback(progress)
                        }
                    }

                    if (totalBytes <= 0) {
                        progressCallback(1.0f)
                    }

                    true
                } else {
                    false
                }
            }
        } catch (_: Exception) {
            false
        }
    }
}