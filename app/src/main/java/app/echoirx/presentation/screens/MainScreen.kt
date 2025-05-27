package app.echoirx.presentation.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import app.echoirx.presentation.navigation.HomeRoute
import app.echoirx.presentation.navigation.SearchDetailsRoute
import app.echoirx.presentation.navigation.SearchRoute
import app.echoirx.presentation.navigation.SettingsRoute
import app.echoirx.presentation.navigation.TopLevelBackStack
import app.echoirx.presentation.navigation.TopLevelRoute
import app.echoirx.presentation.navigation.components.EchoirBottomNav
import app.echoirx.presentation.navigation.components.EchoirTopBar
import app.echoirx.presentation.screens.details.DetailsScreen
import app.echoirx.presentation.screens.home.HomeScreen
import app.echoirx.presentation.screens.search.SearchScreen
import app.echoirx.presentation.screens.settings.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val topLevelBackStack = remember { TopLevelBackStack<Any>(HomeRoute) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            EchoirTopBar(
                currentRoute = topLevelBackStack.topLevelRoute,
                canGoBack = topLevelBackStack.canGoBack() && topLevelBackStack.topLevelRoute !is TopLevelRoute,
                onNavigateBack = { topLevelBackStack.removeLast() },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            EchoirBottomNav(
                topLevelBackStack = topLevelBackStack
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = {
                    Snackbar(
                        snackbarData = it,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        NavDisplay(
            backStack = topLevelBackStack.backStack,
            onBack = { topLevelBackStack.removeLast() },
            entryProvider = entryProvider {
                entry<HomeRoute> {
                    HomeScreen(snackbarHostState = snackbarHostState)
                }

                entry<SearchRoute> {
                    SearchScreen(
                        onNavigateToDetails = { type, id, result ->
                            topLevelBackStack.add(SearchDetailsRoute(type, id, result))
                        },
                        shouldFocusSearchBar = topLevelBackStack.shouldFocusSearchBar,
                        onFocusSearchBarHandled = { topLevelBackStack.clearFocusSearchBar() },
                        snackbarHostState = snackbarHostState
                    )
                }

                entry<SearchDetailsRoute> { route ->
                    DetailsScreen(
                        result = route.result,
                        snackbarHostState = snackbarHostState
                    )
                }

                entry<SettingsRoute> {
                    SettingsScreen()
                }
            },
            modifier = Modifier.padding(innerPadding)
        )
    }
}