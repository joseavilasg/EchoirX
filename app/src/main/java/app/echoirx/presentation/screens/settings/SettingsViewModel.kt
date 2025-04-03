package app.echoirx.presentation.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import app.echoirx.data.local.dao.DownloadDao
import app.echoirx.domain.model.AlbumFolderFormat
import app.echoirx.domain.model.FileNamingFormat
import app.echoirx.domain.repository.SearchHistoryRepository
import app.echoirx.domain.usecase.SettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsUseCase: SettingsUseCase,
    private val workManager: WorkManager,
    private val downloadDao: DownloadDao,
    private val searchHistoryRepository: SearchHistoryRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val defaultServerUrl = "https://example.com/api/echoir"

    init {
        viewModelScope.launch {
            val dir = settingsUseCase.getOutputDirectory()
            val fileFormat = settingsUseCase.getFileNamingFormat()
            val folderFormat = settingsUseCase.getAlbumFolderFormat()
            val region = settingsUseCase.getRegion()
            val serverUrl = settingsUseCase.getServerUrl()

            _state.update {
                it.copy(
                    outputDirectory = dir,
                    fileNamingFormat = fileFormat,
                    albumFolderFormat = folderFormat,
                    region = region,
                    serverUrl = serverUrl
                )
            }
        }
    }

    fun updateOutputDirectory(uri: String) {
        viewModelScope.launch {
            settingsUseCase.setOutputDirectory(uri)
            _state.update {
                it.copy(
                    outputDirectory = uri
                )
            }
        }
    }

    fun updateFileNamingFormat(format: FileNamingFormat) {
        viewModelScope.launch {
            settingsUseCase.setFileNamingFormat(format)
            _state.update {
                it.copy(
                    fileNamingFormat = format
                )
            }
        }
    }

    fun updateAlbumFolderFormat(format: AlbumFolderFormat) {
        viewModelScope.launch {
            settingsUseCase.setAlbumFolderFormat(format)
            _state.update {
                it.copy(
                    albumFolderFormat = format
                )
            }
        }
    }

    fun updateRegion(region: String) {
        viewModelScope.launch {
            settingsUseCase.setRegion(region)
            _state.update {
                it.copy(
                    region = region
                )
            }
        }
    }

    fun updateServerUrl(url: String) {
        if (url.isBlank()) return

        viewModelScope.launch {
            settingsUseCase.setServerUrl(url)
            _state.update {
                it.copy(
                    serverUrl = url
                )
            }
        }
    }

    fun resetServerSettings() {
        viewModelScope.launch {
            settingsUseCase.resetServerSettings()
            _state.update {
                it.copy(
                    serverUrl = defaultServerUrl
                )
            }
        }
    }

    fun clearData() {
        viewModelScope.launch {
            workManager.cancelAllWork()
            downloadDao.deleteAll()
            searchHistoryRepository.clearHistory()
            context.cacheDir.deleteRecursively()
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            searchHistoryRepository.clearHistory()
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            settingsUseCase.setOutputDirectory(null)
            settingsUseCase.setFileNamingFormat(FileNamingFormat.TITLE_ONLY)
            settingsUseCase.setAlbumFolderFormat(AlbumFolderFormat.ALBUM_ONLY)
            settingsUseCase.setRegion("BR")
            settingsUseCase.resetServerSettings()

            _state.update {
                it.copy(
                    outputDirectory = null,
                    fileNamingFormat = FileNamingFormat.TITLE_ONLY,
                    albumFolderFormat = AlbumFolderFormat.ALBUM_ONLY,
                    region = "BR",
                    serverUrl = defaultServerUrl
                )
            }
        }
    }
}