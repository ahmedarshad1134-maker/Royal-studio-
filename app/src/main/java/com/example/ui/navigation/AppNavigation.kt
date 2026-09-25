package com.example.ui.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.components.RoyalTopAppBar
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.BookingScreen
import com.example.ui.screens.ContactScreen
import com.example.ui.screens.CustomerAreaScreen
import com.example.ui.screens.CustomerPrivateGalleryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PackageDetailScreen
import com.example.ui.screens.PackagesScreen
import com.example.ui.screens.PlaceholderScreen
import com.example.ui.screens.PortfolioScreen
import com.example.ui.screens.ReviewsScreen
import com.example.ui.screens.ServiceDetailScreen
import com.example.ui.screens.ServicesScreen
import kotlinx.coroutines.launch

@Composable
fun RoyalStudioApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route
    val currentScreen = drawerScreens.find { it.route == currentRoute } ?: Screen.Home

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        if (isWideScreen) {
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = "Royal Studio",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                    drawerScreens.forEach { screen ->
                        NavigationRailItem(
                            label = { Text(text = screen.title) },
                            icon = { },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }

                Scaffold(
                    topBar = {
                        RoyalTopAppBar(
                            title = currentScreen.title,
                            onNavigationIconClick = { }
                        )
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        registerScreens(navController)
                    }
                }
            }
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = "Royal Studio",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider()
                        drawerScreens.forEach { screen ->
                            NavigationDrawerItem(
                                label = { Text(text = screen.title) },
                                selected = currentRoute == screen.route,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            ) {
                Scaffold(
                    topBar = {
                        RoyalTopAppBar(
                            title = currentScreen.title,
                            onNavigationIconClick = { scope.launch { drawerState.open() } }
                        )
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        registerScreens(navController)
                    }
                }
            }
        }
    }
}

fun NavGraphBuilder.registerScreens(navController: NavController) {
    composable(Screen.Home.route) {
        HomeScreen(navController = navController)
    }
    composable(Screen.Portfolio.route) {
        PortfolioScreen(navController = navController)
    }
    composable(Screen.Services.route) {
        ServicesScreen(navController = navController)
    }
    composable(
        route = Screen.ServiceDetail.route,
        arguments = listOf(navArgument("serviceId") { type = NavType.StringType })
    ) { backStackEntry ->
        val serviceId = backStackEntry.arguments?.getString("serviceId")
        if (serviceId != null) {
            ServiceDetailScreen(serviceId = serviceId, navController = navController)
        } else {
            PlaceholderScreen("Service Not Found")
        }
    }
    composable(Screen.Packages.route) {
        PackagesScreen(navController = navController)
    }
    composable(
        route = Screen.PackageDetail.route,
        arguments = listOf(navArgument("packageId") { type = NavType.StringType })
    ) { backStackEntry ->
        val packageId = backStackEntry.arguments?.getString("packageId")
        if (packageId != null) {
            PackageDetailScreen(packageId = packageId, navController = navController)
        } else {
            PlaceholderScreen("Package Not Found")
        }
    }
    composable(Screen.Booking.route) {
        BookingScreen(navController = navController)
    }
    composable(Screen.Reviews.route) {
        ReviewsScreen(navController = navController)
    }
    composable(Screen.About.route) {
        AboutScreen(navController = navController)
    }
    composable(Screen.Contact.route) {
        ContactScreen(navController = navController)
    }
    composable(Screen.CustomerArea.route) {
        CustomerAreaScreen(navController = navController)
    }
    composable(Screen.CustomerPrivateGallery.route) {
        CustomerAreaScreen(navController = navController)
    }
    composable(
        route = Screen.PrivateGallery.route,
        arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
    ) { backStackEntry ->
        val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
        CustomerPrivateGalleryScreen(
            bookingId = bookingId,
            navController = navController
        )
    }
    composable(Screen.Admin.route) {
        AdminScreen(navController = navController)
    }
    composable(Screen.AdminPanel.route) {
        AdminScreen(navController = navController)
    }
}
