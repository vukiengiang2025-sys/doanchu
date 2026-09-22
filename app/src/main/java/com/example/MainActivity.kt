package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.game.GameViewModel
import com.example.ui.screens.DictionaryScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.InteractiveSolverScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val gameViewModel: GameViewModel = viewModel()
            val settings by gameViewModel.settings.collectAsState()

            val isDark = when (settings.darkMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDark) {
                AppNavigation(viewModel = gameViewModel)
            }
        }
    }
}

private sealed class NavTab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    data object Solver : NavTab("interactive_solver", "Giải Ô Chữ", Icons.Default.Extension)
    data object Game : NavTab("game", "Chơi Game", Icons.Default.SportsEsports)
    data object Dictionary : NavTab("dictionary", "Từ Điển", Icons.Default.MenuBook)
    data object Statistics : NavTab("statistics", "Thống Kê", Icons.Default.BarChart)
    data object Settings : NavTab("settings", "Cài Đặt", Icons.Default.Settings)
}

@Composable
fun AppNavigation(viewModel: GameViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "interactive_solver"

    val tabs = listOf(
        NavTab.Solver,
        NavTab.Game,
        NavTab.Dictionary,
        NavTab.Statistics,
        NavTab.Settings
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(modifier = Modifier.testTag("main_bottom_nav")) {
                tabs.forEach { tab ->
                    val selected = currentRoute == tab.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute != tab.route) {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        modifier = Modifier.testTag("nav_tab_${tab.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "interactive_solver",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("interactive_solver") {
                InteractiveSolverScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        if (navController.previousBackStackEntry != null) {
                            navController.popBackStack()
                        } else {
                            navController.navigate("game") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
            composable("game") {
                GameScreen(
                    viewModel = viewModel,
                    onNavigateToDictionary = { navController.navigate("dictionary") },
                    onNavigateToStatistics = { navController.navigate("statistics") },
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToInteractiveSolver = { navController.navigate("interactive_solver") }
                )
            }
            composable("dictionary") {
                DictionaryScreen(
                    repository = viewModel.dictionaryRepository,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("statistics") {
                StatisticsScreen(
                    statisticsRepository = viewModel.statisticsRepository,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("settings") {
                SettingsScreen(
                    settingsRepository = viewModel.settingsRepository,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
