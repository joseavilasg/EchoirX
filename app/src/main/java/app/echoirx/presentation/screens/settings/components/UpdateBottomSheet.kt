package app.echoirx.presentation.screens.settings.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.echoirx.R
import app.echoirx.presentation.screens.settings.UpdateStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateBottomSheet(
    currentVersion: String,
    latestVersion: String?,
    updateStatus: UpdateStatus,
    progress: Float,
    onCheckForUpdates: () -> Unit,
    onDownloadUpdate: () -> Unit,
    onInstallUpdate: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.small,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Update,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )

                    Text(
                        text = stringResource(R.string.title_check_for_updates),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Text(
                    text = stringResource(R.string.msg_current_version, currentVersion),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (latestVersion != null && latestVersion != currentVersion) {
                    Text(
                        text = stringResource(R.string.msg_latest_version, latestVersion),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (updateStatus) {
                    UpdateStatus.IDLE -> {
                        // Initial state, nothing to show
                    }

                    UpdateStatus.CHECKING -> {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = stringResource(R.string.msg_checking_for_updates),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }

                    UpdateStatus.UP_TO_DATE -> {
                        UpdateStatusContent(
                            icon = Icons.Outlined.CheckCircle,
                            message = stringResource(R.string.msg_up_to_date)
                        )
                    }

                    UpdateStatus.UPDATE_AVAILABLE -> {
                        UpdateStatusContent(
                            icon = Icons.Outlined.Download,
                            message = stringResource(R.string.msg_update_available)
                        )
                    }

                    UpdateStatus.DOWNLOADING -> {
                        Text(
                            text = stringResource(R.string.msg_downloading_update),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth(),
                            strokeCap = StrokeCap.Round
                        )
                        Text(
                            text = stringResource(
                                R.string.msg_download_progress,
                                (progress * 100).toInt()
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    UpdateStatus.DOWNLOAD_COMPLETE -> {
                        UpdateStatusContent(
                            icon = Icons.Outlined.CheckCircle,
                            message = stringResource(R.string.msg_download_complete)
                        )
                    }

                    UpdateStatus.INSTALLING -> {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = stringResource(R.string.msg_installing_update),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }

                    UpdateStatus.ERROR -> {
                        UpdateStatusContent(
                            icon = Icons.Outlined.Error,
                            message = stringResource(R.string.msg_update_error),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End)
            ) {
                when (updateStatus) {
                    UpdateStatus.IDLE -> {
                        FilledTonalButton(
                            onClick = onDismiss
                        ) {
                            Text(stringResource(R.string.action_cancel))
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = onCheckForUpdates
                        ) {
                            Text(stringResource(R.string.action_check_for_updates))
                        }
                    }

                    UpdateStatus.CHECKING -> {
                        FilledTonalButton(
                            onClick = onDismiss
                        ) {
                            Text(stringResource(R.string.action_cancel))
                        }
                    }

                    UpdateStatus.UP_TO_DATE -> {
                        Button(
                            onClick = onDismiss
                        ) {
                            Text(stringResource(R.string.action_ok))
                        }
                    }

                    UpdateStatus.UPDATE_AVAILABLE -> {
                        FilledTonalButton(
                            onClick = onDismiss
                        ) {
                            Text(stringResource(R.string.action_later))
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = onDownloadUpdate
                        ) {
                            Text(stringResource(R.string.action_download))
                        }
                    }

                    UpdateStatus.DOWNLOADING -> {
                        // Just show progress, no buttons
                    }

                    UpdateStatus.DOWNLOAD_COMPLETE -> {
                        FilledTonalButton(
                            onClick = onDismiss
                        ) {
                            Text(stringResource(R.string.action_later))
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = onInstallUpdate
                        ) {
                            Text(stringResource(R.string.action_install))
                        }
                    }

                    UpdateStatus.INSTALLING -> {
                        // Just show progress, no buttons
                    }

                    UpdateStatus.ERROR -> {
                        FilledTonalButton(
                            onClick = onDismiss
                        ) {
                            Text(stringResource(R.string.action_close))
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = onCheckForUpdates
                        ) {
                            Text(stringResource(R.string.action_retry))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdateStatusContent(
    icon: ImageVector,
    message: String,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}