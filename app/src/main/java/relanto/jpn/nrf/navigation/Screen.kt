package relanto.jpn.nrf.navigation

sealed class Screen(val route: String) {
    object WebSocketSetup : Screen("websocket_setup")
    object Splash : Screen("splash")
    object Home : Screen("home")
}
