package app.echoirx.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

class TopLevelBackStack<T : Any>(startRoute: T) {

    private var topLevelStacks: LinkedHashMap<T, SnapshotStateList<T>> = linkedMapOf(
        startRoute to mutableStateListOf(startRoute)
    )

    var topLevelRoute by mutableStateOf(startRoute)
        private set

    val backStack = mutableStateListOf(startRoute)

    var shouldFocusSearchBar by mutableStateOf(false)
        private set

    private fun updateBackStack() = backStack.apply {
        clear()
        addAll(topLevelStacks.flatMap { it.value })
    }

    fun addTopLevel(route: T) {
        if (route == SearchRoute && topLevelRoute == SearchRoute) {
            shouldFocusSearchBar = true
            return
        }

        if (topLevelStacks[route] == null) {
            topLevelStacks[route] = mutableStateListOf(route)
        } else {
            topLevelStacks.apply {
                remove(route)?.let {
                    put(route, it)
                }
            }
        }
        topLevelRoute = route
        updateBackStack()
    }

    fun add(route: T) {
        topLevelStacks[topLevelRoute]?.add(route)
        updateBackStack()
    }

    fun removeLast() {
        val removedRoute = topLevelStacks[topLevelRoute]?.removeLastOrNull()
        topLevelStacks.remove(removedRoute)

        if (topLevelStacks.isNotEmpty()) {
            topLevelRoute = topLevelStacks.keys.last()
        }
        updateBackStack()
    }

    fun clearFocusSearchBar() {
        shouldFocusSearchBar = false
    }

    fun canGoBack(): Boolean = backStack.size > 1

    fun isInSearchSection(): Boolean {
        return topLevelStacks[topLevelRoute]?.any { route ->
            route == SearchRoute || route is SearchDetailsRoute
        } == true
    }
}