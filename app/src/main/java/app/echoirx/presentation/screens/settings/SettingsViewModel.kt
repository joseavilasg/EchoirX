package app.echoirx.presentation.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import app.echoirx.BuildConfig
import app.echoirx.data.local.dao.DownloadDao
import app.echoirx.data.update.UpdateManager
import app.echoirx.domain.model.FileNamingFormat
import app.echoirx.domain.usecase.SettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsUseCase: SettingsUseCase,
    private val workManager: WorkManager,
    private val downloadDao: DownloadDao,
    private val updateManager: UpdateManager,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val dir = settingsUseCase.getOutputDirectory()
            val format = settingsUseCase.getFileNamingFormat()
            val region = settingsUseCase.getRegion()
            val serverUrl = settingsUseCase.getServerUrl()

            _state.update {
                it.copy(
                    outputDirectory = dir,
                    fileNamingFormat = format,
                    region = region,
                    serverUrl = serverUrl
                )
            }
        }

        // Listen for download status updates
        viewModelScope.launch {
            updateManager.downloadStatus.collectLatest { status ->
                when (status) {
                    is UpdateManager.DownloadStatus.Progress -> {
                        _state.update {
                            it.copy(
                                updateStatus = UpdateStatus.DOWNLOADING,
                                downloadProgress = status.progress
                            )
                        }
                    }

                    is UpdateManager.DownloadStatus.Complete -> {
                        _state.update {
                            it.copy(
                                updateStatus = UpdateStatus.DOWNLOAD_COMPLETE,
                                updateFile = status.file
                            )
                        }
                    }

                    is UpdateManager.DownloadStatus.Error -> {
                        _state.update {
                            it.copy(
                                updateStatus = UpdateStatus.ERROR,
                                updateError = status.message
                            )
                        }
                    }

                    else -> { /* No action needed */
                    }
                }
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
            val serverUrl = settingsUseCase.getServerUrl()
            _state.update {
                it.copy(
                    serverUrl = serverUrl
                )
            }
        }
    }

    fun clearData() {
        viewModelScope.launch {
            workManager.cancelAllWork()
            downloadDao.deleteAll()
            context.cacheDir.deleteRecursively()
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            settingsUseCase.setOutputDirectory(null)
            settingsUseCase.setFileNamingFormat(FileNamingFormat.TITLE_ONLY)
            settingsUseCase.setRegion("BR")
            settingsUseCase.resetServerSettings()

            val serverUrl = settingsUseCase.getServerUrl()
            _state.update {
                it.copy(
                    outputDirectory = null,
                    fileNamingFormat = FileNamingFormat.TITLE_ONLY,
                    region = "BR",
                    serverUrl = serverUrl
                )
            }
        }
    }

    // Update related functions
    fun checkForUpdates() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    updateStatus = UpdateStatus.CHECKING,
                    updateError = null
                )
            }

            when (val result = updateManager.checkForUpdates()) {
                is UpdateManager.UpdateResult.UpdateAvailable -> {
                    _state.update {
                        it.copy(
                            updateStatus = UpdateStatus.UPDATE_AVAILABLE,
                            latestVersion = result.latestVersion,
                            releaseNotes = result.releaseNotes,
                            downloadUrl = result.downloadUrl
                        )
                    }
                }

                is UpdateManager.UpdateResult.UpToDate -> {
                    _state.update {
                        it.copy(
                            updateStatus = UpdateStatus.UP_TO_DATE,
                            latestVersion = result.latestVersion
                        )
                    }
                }

                is UpdateManager.UpdateResult.Error -> {
                    _state.update {
                        it.copy(
                            updateStatus = UpdateStatus.ERROR,
                            updateError = result.message
                        )
                    }
                }
            }
        }
    }

    fun downloadUpdate() {
        viewModelScope.launch {
            val downloadUrl = state.value.downloadUrl
            if (downloadUrl.isNullOrEmpty()) {
                _state.update {
                    it.copy(
                        updateStatus = UpdateStatus.ERROR,
                        updateError = "Download URL is missing"
                    )
                }
                return@launch
            }

            _state.update {
                it.copy(
                    updateStatus = UpdateStatus.DOWNLOADING,
                    downloadProgress = 0f
                )
            }

            updateManager.downloadUpdate(downloadUrl)
        }
    }

    fun installUpdate() {
        viewModelScope.launch {
            val updateFile = state.value.updateFile
            if (updateFile == null) {
                _state.update {
                    it.copy(
                        updateStatus = UpdateStatus.ERROR,
                        updateError = "Update file is missing"
                    )
                }
                return@launch
            }

            _state.update {
                it.copy(
                    updateStatus = UpdateStatus.INSTALLING
                )
            }

            updateManager.installUpdate(updateFile)
        }
    }

    fun getCurrentVersion(): String {
        return BuildConfig.VERSION_NAME
    }
}