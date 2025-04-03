package app.echoirx.presentation.screens.settings

import app.echoirx.domain.model.AlbumFolderFormat
import app.echoirx.domain.model.FileNamingFormat

data class SettingsState(
    val outputDirectory: String? = null,
    val fileNamingFormat: FileNamingFormat = FileNamingFormat.TITLE_ONLY,
    val albumFolderFormat: AlbumFolderFormat = AlbumFolderFormat.ALBUM_ONLY,
    val region: String = "BR",
    val serverUrl: String = "https://echoir.vercel.app/api"
)