package app.echoirx.presentation.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import app.echoirx.R
import app.echoirx.domain.model.SearchResult

interface TopLevelRoute {
    @get:DrawableRes
    val icon: Int

    @get:StringRes
    val label: Int
}

data object HomeRoute : TopLevelRoute {
    override val icon = R.drawable.ic_home
    override val label = R.string.nav_home
}

data object SearchRoute : TopLevelRoute {
    override val icon = R.drawable.ic_search
    override val label = R.string.nav_search
}

data object SettingsRoute : TopLevelRoute {
    override val icon = R.drawable.ic_settings
    override val label = R.string.nav_settings
}

data class SearchDetailsRoute(
    val type: String,
    val id: Long,
    val result: SearchResult
)

val TOP_LEVEL_ROUTES = listOf(HomeRoute, SearchRoute, SettingsRoute)