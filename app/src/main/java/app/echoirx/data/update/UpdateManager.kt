package app.echoirx.data.update

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import app.echoirx.BuildConfig
import app.echoirx.data.remote.api.GithubApiService
import app.echoirx.data.remote.model.GithubRelease
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateManager @Inject constructor(
    private val githubApiService: GithubApiService,
    private val context: Context
) {
    companion object {
        private const val TAG = "UpdateManager"
    }

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    private val _downloadStatus = MutableStateFlow<DownloadStatus>(DownloadStatus.None)
    val downloadStatus: StateFlow<DownloadStatus> = _downloadStatus.asStateFlow()

    private var latestRelease: GithubRelease? = null
    private var downloadCancelled = false

    sealed class DownloadStatus {
        object None : DownloadStatus()
        data class Progress(val progress: Float) : DownloadStatus()
        data class Complete(val file: File) : DownloadStatus()
        data class Error(val message: String) : DownloadStatus()
    }

    suspend fun checkForUpdates(): UpdateResult {
        return try {
            latestRelease = githubApiService.getLatestRelease()
            val latest = latestRelease
            if (latest == null) {
                UpdateResult.Error("Could not fetch release information")
            } else {
                val latestVersion = latest.tagName.removePrefix("v")
                val currentVersion = BuildConfig.VERSION_NAME
                if (isNewerVersion(latestVersion, currentVersion)) {
                    UpdateResult.UpdateAvailable(
                        currentVersion = currentVersion,
                        latestVersion = latestVersion,
                        releaseNotes = latest.body,
                        downloadUrl = getAppropriateApkDownloadUrl(latest)
                    )
                } else {
                    UpdateResult.UpToDate(
                        currentVersion = currentVersion,
                        latestVersion = latestVersion
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for updates", e)
            UpdateResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    private fun getAppropriateApkDownloadUrl(release: GithubRelease): String? {
        val currentAbi = getCurrentAbi()
        Log.d(TAG, "Current device ABI: $currentAbi")

        val specificAbiMatch = release.assets.find { asset ->
            asset.name.endsWith(".apk") &&
                    !asset.name.contains("debug") &&
                    asset.name.contains("-$currentAbi-")
        }

        if (specificAbiMatch != null) {
            Log.d(TAG, "Found ABI-specific APK: ${specificAbiMatch.name}")
            return specificAbiMatch.browserDownloadUrl
        }

        val universalApk = release.assets.find { asset ->
            asset.name.endsWith(".apk") &&
                    !asset.name.contains("debug") &&
                    asset.name.contains("-universal-")
        }

        if (universalApk != null) {
            Log.d(TAG, "Found universal APK: ${universalApk.name}")
            return universalApk.browserDownloadUrl
        }

        val anyReleaseApk = release.assets.find { asset ->
            asset.name.endsWith(".apk") && !asset.name.contains("debug")
        }

        Log.d(TAG, "Falling back to: ${anyReleaseApk?.name ?: "No APK found"}")
        return anyReleaseApk?.browserDownloadUrl
    }

    private fun getCurrentAbi(): String {
        return Build.SUPPORTED_ABIS.firstOrNull() ?: run {
            // Fallback logic
            when {
                Build.CPU_ABI.contains("arm64") -> "arm64-v8a"
                Build.CPU_ABI.contains("armeabi") -> "armeabi-v7a"
                Build.CPU_ABI.contains("x86_64") -> "x86_64"
                Build.CPU_ABI.contains("x86") -> "x86"
                else -> "universal"
            }
        }
    }

    suspend fun downloadUpdate(downloadUrl: String) = withContext(Dispatchers.IO) {
        try {
            _downloadStatus.value = DownloadStatus.None
            _downloadProgress.value = 0f
            downloadCancelled = false

            val apkFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                "echoirx-update.apk"
            )

            if (apkFile.exists()) {
                apkFile.delete()
            }

            try {
                apkFile.parentFile?.mkdirs()

                apkFile.outputStream().use { outputStream ->
                    val result = githubApiService.downloadFile(
                        url = downloadUrl,
                        outputStream = outputStream
                    ) { progress ->
                        if (downloadCancelled) {
                            throw InterruptedException("Download cancelled")
                        }
                        _downloadProgress.value = progress
                        _downloadStatus.value = DownloadStatus.Progress(progress)
                    }

                    if (result) {
                        _downloadProgress.value = 1f
                        _downloadStatus.value = DownloadStatus.Complete(apkFile)
                    } else {
                        throw Exception("Download failed")
                    }
                }
            } catch (e: Exception) {
                if (apkFile.exists()) {
                    apkFile.delete()
                }
                throw e
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading update", e)
            _downloadStatus.value = DownloadStatus.Error(e.message ?: "Unknown error occurred")
        }
    }

    fun cancelDownload() {
        downloadCancelled = true
        _downloadStatus.value = DownloadStatus.None
    }

    fun installUpdate(apkFile: File) {
        try {
            val uri =
                FileProvider.getUriForFile(context, "${context.packageName}.provider", apkFile)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error installing update", e)
            _downloadStatus.value = DownloadStatus.Error(e.message ?: "Error installing update")
        }
    }

    private fun isNewerVersion(latestVersion: String, currentVersion: String): Boolean {
        // Simple version comparison - splits by dots and compares each segment as an integer
        // Format: x.y.z
        val latest = latestVersion.split('.').map { it.toIntOrNull() ?: 0 }
        val current = currentVersion.split('.').map { it.toIntOrNull() ?: 0 }

        for (i in 0 until maxOf(latest.size, current.size)) {
            val latestSegment = latest.getOrNull(i) ?: 0
            val currentSegment = current.getOrNull(i) ?: 0

            when {
                latestSegment > currentSegment -> return true
                latestSegment < currentSegment -> return false
            }
        }

        return false // Versions are equal
    }

    sealed class UpdateResult {
        data class UpdateAvailable(
            val currentVersion: String,
            val latestVersion: String,
            val releaseNotes: String,
            val downloadUrl: String?
        ) : UpdateResult()

        data class UpToDate(
            val currentVersion: String,
            val latestVersion: String
        ) : UpdateResult()

        data class Error(val message: String) : UpdateResult()
    }
}