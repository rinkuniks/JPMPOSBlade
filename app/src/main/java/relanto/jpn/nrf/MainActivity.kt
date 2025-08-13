package relanto.jpn.nrf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import relanto.jpn.nrf.navigation.NavGraph
import relanto.jpn.nrf.ui.theme.JPMPOSBladeTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent {
			JPMPOSBladeTheme {
				val navController = rememberNavController()
				NavGraph(navController = navController)
			}
		}
	}
}
