package com.innotime.trainerapp.presentation.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.innotime.trainerapp.R
import com.innotime.trainerapp.app.navigation.NavigationDestinations

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun BottomNavBar(
    navController: NavController
) {
    val items = listOf(
        BottomNavItem(
            label = stringResource(R.string.nav_training),
            icon = Icons.Default.Timer,
            route = NavigationDestinations.Training.route
        ),
        BottomNavItem(
            label = stringResource(R.string.nav_athletes),
            icon = Icons.Default.Person,
            route = NavigationDestinations.Athletes.route
        ),
        BottomNavItem(
            label = stringResource(R.string.nav_groups),
            icon = Icons.Default.Group,
            route = NavigationDestinations.Groups.route
        ),
        BottomNavItem(
            label = stringResource(R.string.nav_results),
            icon = Icons.Default.EmojiEvents,
            route = NavigationDestinations.Results.route
        )
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
