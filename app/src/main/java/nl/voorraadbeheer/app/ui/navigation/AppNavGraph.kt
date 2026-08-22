package nl.voorraadbeheer.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import nl.voorraadbeheer.app.R
import nl.voorraadbeheer.app.ui.addproduct.AddEditProductScreen
import nl.voorraadbeheer.app.ui.auth.AuthViewModel
import nl.voorraadbeheer.app.ui.auth.LoginScreen
import nl.voorraadbeheer.app.ui.dashboard.DashboardScreen
import nl.voorraadbeheer.app.ui.inventory.InventoryListScreen
import nl.voorraadbeheer.app.ui.locations.LocationsScreen
import nl.voorraadbeheer.app.ui.scan.ScanScreen
import nl.voorraadbeheer.app.ui.settings.SettingsScreen
import nl.voorraadbeheer.app.ui.shoppinglist.ShoppingListScreen

private data class BottomTab(
    val graphRoute: String,
    val navigateRoute: String,
    val labelRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

private val bottomTabs = listOf(
    BottomTab(Routes.DASHBOARD, Routes.DASHBOARD, R.string.nav_dashboard, Icons.Filled.Home),
    BottomTab(Routes.INVENTORY_ALL, Routes.inventoryForLocation(null), R.string.nav_inventory, Icons.Filled.Kitchen),
    BottomTab(Routes.SHOPPING_LIST, Routes.SHOPPING_LIST, R.string.nav_shopping_list, Icons.Filled.ShoppingCart),
    BottomTab(Routes.LOCATIONS, Routes.LOCATIONS, R.string.nav_locations, Icons.Filled.List),
    BottomTab(Routes.SETTINGS, Routes.SETTINGS, R.string.nav_settings, Icons.Filled.Settings),
)

@Composable
fun AppRoot() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val user by authViewModel.currentUser.collectAsState()

    if (user == null) {
        LoginScreen()
    } else {
        MainScaffold()
    }
}

@Composable
private fun MainScaffold() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = backStackEntry?.destination
                bottomTabs.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any { it.route == tab.graphRoute } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.navigateRoute) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = stringResource(tab.labelRes)) },
                        label = { Text(stringResource(tab.labelRes)) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(onLocationClick = { locationId ->
                    navController.navigate(Routes.inventoryForLocation(locationId))
                })
            }
            composable(
                route = Routes.INVENTORY_ALL,
                arguments = listOf(navArgument(Routes.INVENTORY_ARG_LOCATION_ID) {
                    defaultValue = ""
                }),
            ) {
                InventoryListScreen(
                    onScanClick = { navController.navigate(Routes.SCAN) },
                    onAddManuallyClick = { navController.navigate(Routes.addProduct(null)) },
                )
            }
            composable(Routes.SHOPPING_LIST) { ShoppingListScreen() }
            composable(Routes.LOCATIONS) { LocationsScreen() }
            composable(Routes.SETTINGS) { SettingsScreen() }
            composable(Routes.SCAN) {
                ScanScreen(onBarcodeScanned = { barcode ->
                    navController.navigate(Routes.addProduct(barcode)) {
                        popUpTo(Routes.SCAN) { inclusive = true }
                    }
                })
            }
            composable(
                route = Routes.ADD_PRODUCT_ROUTE,
                arguments = listOf(navArgument(Routes.ADD_PRODUCT_ARG_BARCODE) {
                    defaultValue = ""
                }),
            ) {
                AddEditProductScreen(onSaved = { navController.popBackStack() })
            }
        }
    }
}
