package relanto.jpn.nrf.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import relanto.jpn.nrf.ui.screens.home.HomeScreen
import relanto.jpn.nrf.ui.screens.splash.SplashScreen
import relanto.jpn.nrf.ui.screens.websocket.WebSocketSetupScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.WebSocketSetup.route
    ) {
        composable(Screen.WebSocketSetup.route) {
            WebSocketSetupScreen(navController)
        }
        
        composable(Screen.Splash.route) {
            SplashScreen(navController)
        }
        
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
    }
}
