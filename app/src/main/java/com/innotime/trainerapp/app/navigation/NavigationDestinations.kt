package com.innotime.trainerapp.app.navigation

sealed class NavigationDestinations(val route: String) {
    data object Training : NavigationDestinations("training")
    data object Athletes : NavigationDestinations("athletes")
    data object Groups : NavigationDestinations("groups")
    data object Results : NavigationDestinations("results")
}
