package app.echoirx.presentation.screens.settings

enum class UpdateStatus {
    IDLE,
    CHECKING,
    UP_TO_DATE,
    UPDATE_AVAILABLE,
    DOWNLOADING,
    DOWNLOAD_COMPLETE,
    INSTALLING,
    ERROR
}