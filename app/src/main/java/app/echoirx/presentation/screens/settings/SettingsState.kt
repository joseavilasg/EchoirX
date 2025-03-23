package app.echoirx.presentation.screens.settings

import app.echoirx.domain.model.FileNamingFormat
import java.io.File

data class SettingsState(
    val outputDirectory: String? = null,
    val fileNamingFormat: FileNamingFormat = FileNamingFormat.TITLE_ONLY,
    val region: String = "BR",
    val serverUrl: String = "https://example.com/api/echoir",

    // Update related states
    val updateStatus: UpdateStatus = UpdateStatus.IDLE,
    val latestVersion: String? = null,
    val releaseNotes: String? = null,
    val downloadUrl: String? = null,
    val downloadProgress: Float = 0f,
    val updateFile: File? = null,
    val updateError: String? = null
)