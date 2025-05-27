package app.echoirx.presentation.navigation.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.echoirx.presentation.navigation.SearchRoute
import app.echoirx.presentation.navigation.TOP_LEVEL_ROUTES
import app.echoirx.presentation.navigation.TopLevelBackStack

@Composable
fun EchoirBottomNav(
    topLevelBackStack: TopLevelBackStack<Any>
) {
    NavigationBar {
        TOP_LEVEL_ROUTES.forEach { topLevelRoute ->
            val isSelected = when (topLevelRoute) {
                SearchRoute -> topLevelBackStack.isInSearchSection()
                else -> topLevelBackStack.topLevelRoute == topLevelRoute
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    topLevelBackStack.addTopLevel(topLevelRoute)
                },
                icon = {
                    Icon(
                        painter = painterResource(topLevelRoute.icon),
                        contentDescription = stringResource(topLevelRoute.label)
                    )
                },
                label = {
                    Text(
                        text = stringResource(topLevelRoute.label),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            )
        }
    }
}