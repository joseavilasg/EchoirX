package app.echoirx.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.echoirx.domain.model.AlbumFolderFormat
import app.echoirx.domain.model.FileNamingFormat
import app.echoirx.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {
    private object PreferencesKeys {
        val OUTPUT_DIRECTORY = stringPreferencesKey("output_directory")
        val FILE_NAMING_FORMAT = intPreferencesKey("file_naming_format")
        val ALBUM_FOLDER_FORMAT = intPreferencesKey("album_folder_format")
        val REGION = stringPreferencesKey("region")
        val SERVER_URL = stringPreferencesKey("server_url")
    }

    // Default server URL - use this as the fallback value
    private val DEFAULT_SERVER_URL = "https://example.com/api/echoir"

    override suspend fun getOutputDirectory(): String? {
        return context.dataStore.data.first()[PreferencesKeys.OUTPUT_DIRECTORY]
    }

    override suspend fun setOutputDirectory(uri: String?) {
        context.dataStore.edit { preferences ->
            if (uri == null) {
                preferences.remove(PreferencesKeys.OUTPUT_DIRECTORY)
            } else {
                preferences[PreferencesKeys.OUTPUT_DIRECTORY] = uri
            }
        }
    }

    override suspend fun getFileNamingFormat(): FileNamingFormat {
        val ordinal = context.dataStore.data.first()[PreferencesKeys.FILE_NAMING_FORMAT] ?: 0
        return FileNamingFormat.fromOrdinal(ordinal)
    }

    override suspend fun setFileNamingFormat(format: FileNamingFormat) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FILE_NAMING_FORMAT] = format.ordinal
        }
    }

    override suspend fun getAlbumFolderFormat(): AlbumFolderFormat {
        val ordinal = context.dataStore.data.first()[PreferencesKeys.ALBUM_FOLDER_FORMAT] ?: 0
        return AlbumFolderFormat.fromOrdinal(ordinal)
    }

    override suspend fun setAlbumFolderFormat(format: AlbumFolderFormat) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ALBUM_FOLDER_FORMAT] = format.ordinal
        }
    }

    override suspend fun getRegion(): String {
        return context.dataStore.data.first()[PreferencesKeys.REGION] ?: "BR"
    }

    override suspend fun setRegion(region: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REGION] = region
        }
    }

    override suspend fun getServerUrl(): String {
        return context.dataStore.data.first()[PreferencesKeys.SERVER_URL] ?: DEFAULT_SERVER_URL
    }

    override suspend fun setServerUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SERVER_URL] = url
        }
    }
}