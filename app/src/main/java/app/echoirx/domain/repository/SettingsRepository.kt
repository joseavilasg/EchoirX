package app.echoirx.domain.repository

import app.echoirx.domain.model.AlbumFolderFormat
import app.echoirx.domain.model.FileNamingFormat

interface SettingsRepository {
    suspend fun getOutputDirectory(): String?
    suspend fun setOutputDirectory(uri: String?)
    suspend fun getFileNamingFormat(): FileNamingFormat
    suspend fun setFileNamingFormat(format: FileNamingFormat)
    suspend fun getAlbumFolderFormat(): AlbumFolderFormat
    suspend fun setAlbumFolderFormat(format: AlbumFolderFormat)
    suspend fun getRegion(): String
    suspend fun setRegion(region: String)
    suspend fun getServerUrl(): String
    suspend fun setServerUrl(url: String)
}