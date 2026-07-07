package de.hardtthelen.trainerapp.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.ui.Modifier
import de.hardtthelen.trainerapp.presentation.screen.athletes.AthletesScreen
import de.hardtthelen.trainerapp.presentation.screen.groups.GroupsScreen
import de.hardtthelen.trainerapp.presentation.screen.results.ResultsScreen
import de.hardtthelen.trainerapp.presentation.screen.training.TrainingScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavigationDestinations.Training.route,
        modifier = modifier
    ) {
        composable(NavigationDestinations.Training.route) {
            TrainingScreen(
                onNavigateToAthletes = {
                    navController.navigate(NavigationDestinations.Athletes.route)
                }
            )
        }

        composable(NavigationDestinations.Athletes.route) {
            AthletesScreen()
        }

        composable(NavigationDestinations.Groups.route) {
            GroupsScreen()
        }

        composable(NavigationDestinations.Results.route) {
            ResultsScreen()
        }
    }
}
