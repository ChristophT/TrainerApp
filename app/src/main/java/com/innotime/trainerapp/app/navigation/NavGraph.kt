package com.innotime.trainerapp.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.ui.Modifier
import com.innotime.trainerapp.presentation.screen.athletes.AthletesScreen
import com.innotime.trainerapp.presentation.screen.groups.GroupsScreen
import com.innotime.trainerapp.presentation.screen.results.ResultsScreen
import com.innotime.trainerapp.presentation.screen.training.TrainingScreen

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
            TrainingScreen(modifier = modifier)
        }

        composable(NavigationDestinations.Athletes.route) {
            AthletesScreen(modifier = modifier)
        }

        composable(NavigationDestinations.Groups.route) {
            GroupsScreen(modifier = modifier)
        }

        composable(NavigationDestinations.Results.route) {
            ResultsScreen(modifier = modifier)
        }
    }
}
