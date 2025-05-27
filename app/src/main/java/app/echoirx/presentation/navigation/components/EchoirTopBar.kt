package app.echoirx.presentation.navigation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.echoirx.R
import app.echoirx.presentation.navigation.HomeRoute
import app.echoirx.presentation.navigation.SearchDetailsRoute
import app.echoirx.presentation.navigation.SearchRoute
import app.echoirx.presentation.navigation.SettingsRoute

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EchoirTopBar(
    currentRoute: Any?,
    canGoBack: Boolean,
    onNavigateBack: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        title = {
            Text(
                text = when (currentRoute) {
                    is HomeRoute -> stringResource(R.string.nav_home)
                    is SettingsRoute -> stringResource(R.string.nav_settings)
                    is SearchRoute -> stringResource(R.string.nav_search)
                    is SearchDetailsRoute -> stringResource(R.string.nav_details)
                    else -> ""
                },
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            if (canGoBack) {
                FilledTonalIconButton(
                    onClick = onNavigateBack,
                    shapes = IconButtonDefaults.shapes()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back_button)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        scrollBehavior = scrollBehavior
    )
}