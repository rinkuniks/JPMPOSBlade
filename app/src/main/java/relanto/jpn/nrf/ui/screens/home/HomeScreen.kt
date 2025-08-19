package relanto.jpn.nrf.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.*
import androidx.lifecycle.compose.*
import androidx.navigation.*
import relanto.jpn.nrf.ui.components.LoadingScreen
import relanto.jpn.nrf.ui.components.ErrorScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
	val viewModel: HomeViewModel = hiltViewModel()
	val state by viewModel.uiState.collectAsStateWithLifecycle()
	val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
	val error by viewModel.error.collectAsStateWithLifecycle()
	
	var drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
	val scope = rememberCoroutineScope()
	
	when {
		isLoading -> LoadingScreen()
		error != null -> ErrorScreen(message = error ?: "Unknown error")
		else -> ModalNavigationDrawer(
			drawerState = drawerState,
			drawerContent = {
				ModalDrawerSheet {
					Spacer(modifier = Modifier.height(12.dp))
					Text(
						state.title,
						modifier = Modifier.padding(16.dp),
						style = MaterialTheme.typography.titleLarge,
						fontWeight = FontWeight.Bold
					)
					Divider(modifier = Modifier.padding(horizontal = 16.dp))
					Spacer(modifier = Modifier.height(12.dp))
					
					NavigationDrawerItem(
						icon = { Icon(Icons.Default.Home, contentDescription = null) },
						label = { Text("Home") },
						selected = true,
						onClick = { /* Navigate to home */ }
					)
				}
			}
		) {
			Scaffold(
				topBar = {
					TopAppBar(
						title = { Text(state.title) },
						navigationIcon = {
							IconButton(
								onClick = {
									// Open drawer
								}
							) {
								Icon(Icons.Default.Menu, contentDescription = "Menu")
							}
						}
					)
				}
			) { innerPadding ->
				Column(
					modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
				) {
					// Main Content
					Box(
						modifier = Modifier
                .fillMaxSize()
                .weight(1f),
						contentAlignment = Alignment.Center
					) {
						Column(
							horizontalAlignment = Alignment.CenterHorizontally,
							verticalArrangement = Arrangement.Center
						) {
							Icon(
								Icons.Default.Home,
								contentDescription = null,
								modifier = Modifier.size(64.dp),
								tint = MaterialTheme.colorScheme.primary
							)
							Spacer(modifier = Modifier.height(16.dp))
							Text(
								text = "Welcome to ${state.title}",
								style = MaterialTheme.typography.headlineMedium,
								fontWeight = FontWeight.Bold
							)
							Spacer(modifier = Modifier.height(8.dp))
							Text(
								text = state.subtitle,
								style = MaterialTheme.typography.bodyLarge,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
					}
				}
			}
		}
	}
}
