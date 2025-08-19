package relanto.jpn.nrf.ui.screens.websocket

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import relanto.jpn.nrf.ui.components.PermissionStatusComponent
import relanto.jpn.nrf.websocket.UnifiedWebSocketService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebSocketSetupScreen(navController: NavController) {
    val viewModel: WebSocketSetupViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val clientState by viewModel.clientState.collectAsStateWithLifecycle()
    val isConnected by remember { derivedStateOf { viewModel.isConnected } }
    val serverState by viewModel.serverState.collectAsStateWithLifecycle()
    val serverConnectedClients by viewModel.serverConnectedClients.collectAsStateWithLifecycle()
    val serverIp by viewModel.serverIp.collectAsStateWithLifecycle()
    val localIpAddress by viewModel.localIpAddress.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WebSocket Setup") },
                actions = {
                    IconButton(
                        onClick = { viewModel.onEvent(WebSocketSetupEvent.RefreshConnection) }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            
            // Mode Selection Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "WebSocket Modes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = state.enableServerMode,
                                onCheckedChange = { viewModel.onEvent(WebSocketSetupEvent.ToggleServerMode) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Enable Server Mode",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = state.enableClientMode,
                                onCheckedChange = { viewModel.onEvent(WebSocketSetupEvent.ToggleClientMode) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Enable Client Mode",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Disconnect All Button
                        Button(
                            onClick = { viewModel.onEvent(WebSocketSetupEvent.DisconnectAll) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Disconnect All & Clear Session")
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Go to Home Button (always visible)
                        Button(
                            onClick = { 
                                navController.navigate("home") {
                                    popUpTo("websocket_setup") { inclusive = true }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.Home, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Go to Home")
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // WebSocket Server Section (only show if server mode is enabled)
            if (state.enableServerMode) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "WebSocket Server",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            when (serverState) {
                                is UnifiedWebSocketService.ServerState.Running -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Server Running",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    if (state.serverIp.isNotBlank()) {
                                        Text("Server IP: ${state.serverIp}")
                                    }
                                    
                                    // Client Connection Status
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (state.connectedClients > 0) 
                                                MaterialTheme.colorScheme.primaryContainer 
                                            else 
                                                MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (state.connectedClients > 0) 
                                                        Icons.Default.CheckCircle 
                                                    else 
                                                        Icons.Default.Info,
                                                    contentDescription = null,
                                                    tint = if (state.connectedClients > 0) 
                                                        MaterialTheme.colorScheme.primary 
                                                    else 
                                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Client Connections",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                            
                                            Spacer(modifier = Modifier.height(4.dp))
                                            
                                            Text(
                                                text = if (state.connectedClients > 0) 
                                                    "${state.connectedClients} client(s) connected" 
                                                else 
                                                    "No clients connected",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (state.connectedClients > 0) 
                                                    MaterialTheme.colorScheme.primary 
                                                else 
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    // Chat Box (show when server has clients)
                                    if (state.connectedClients > 0) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(16.dp)
                                            ) {
                                                Text(
                                                    text = "💬 Server Chat",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                
                                                Spacer(modifier = Modifier.height(8.dp))
                                                
                                                Text(
                                                    text = "Send message to all connected clients",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                                
                                                Spacer(modifier = Modifier.height(16.dp))
                                                
                                                // Message Input and Send Button
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    OutlinedTextField(
                                                        value = state.messageText,
                                                        onValueChange = { viewModel.onEvent(WebSocketSetupEvent.UpdateMessageText(it)) },
                                                        label = { Text("Type your message...") },
                                                        placeholder = { Text("Hello from Server!") },
                                                        modifier = Modifier.weight(1f),
                                                        singleLine = true,
                                                        maxLines = 3
                                                    )
                                                    
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    
                                                    Button(
                                                        onClick = { 
                                                            if (state.messageText.isNotBlank()) {
                                                                viewModel.onEvent(WebSocketSetupEvent.SendMessage)
                                                            }
                                                        },
                                                        enabled = state.messageText.isNotBlank(),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = MaterialTheme.colorScheme.primary,
                                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                                        )
                                                    ) {
                                                        Icon(Icons.Default.Send, contentDescription = "Send")
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Send")
                                                    }
                                                }
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                    
                                    Button(
                                        onClick = { viewModel.onEvent(WebSocketSetupEvent.StopServer) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer,
                                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Stop Server")
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    // Go to Home Button
                                    Button(
                                        onClick = { 
                                            navController.navigate("home") {
                                                popUpTo("websocket_setup") { inclusive = true }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    ) {
                                        Icon(Icons.Default.Home, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Go to Home")
                                    }
                                }
                                is UnifiedWebSocketService.ServerState.Starting -> {
                                    Text("Status: Starting...")
                                }
                                is UnifiedWebSocketService.ServerState.Error -> {
                                    Text("Status: Error")
                                    Text(
                                        text = (serverState as UnifiedWebSocketService.ServerState.Error).message,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                else -> {
                                    Text("Status: Stopped")
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Button(
                                        onClick = { viewModel.onEvent(WebSocketSetupEvent.StartServer) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Start Server")
                                    }
                                }
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
            
            // Connection Status Card (only show if client mode is enabled)
            if (state.enableClientMode) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = when (clientState) {
                                is UnifiedWebSocketService.ClientState.Connected -> MaterialTheme.colorScheme.primaryContainer
                                is UnifiedWebSocketService.ClientState.Connecting -> MaterialTheme.colorScheme.secondaryContainer
                                is UnifiedWebSocketService.ClientState.Error -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = when (clientState) {
                                    is UnifiedWebSocketService.ClientState.Connected -> Icons.Default.CheckCircle
                                    is UnifiedWebSocketService.ClientState.Connecting -> Icons.Default.Refresh
                                    is UnifiedWebSocketService.ClientState.Error -> Icons.Default.Warning
                                    else -> Icons.Default.Info
                                },
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = when (clientState) {
                                    is UnifiedWebSocketService.ClientState.Connected -> MaterialTheme.colorScheme.primary
                                    is UnifiedWebSocketService.ClientState.Connecting -> MaterialTheme.colorScheme.secondary
                                    is UnifiedWebSocketService.ClientState.Error -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = when (clientState) {
                                    is UnifiedWebSocketService.ClientState.Connected -> "Connected to Server"
                                    is UnifiedWebSocketService.ClientState.Connecting -> "Connecting to Server..."
                                    is UnifiedWebSocketService.ClientState.Error -> "Connection Error"
                                    else -> "Disconnected from Server"
                                },
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Show server IP when connected
                            if (clientState is UnifiedWebSocketService.ClientState.Connected && state.serverIp.isNotBlank()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Connected to Server",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        Text(
                                            text = "Server IP: ${state.serverIp}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            
                            if (clientState is UnifiedWebSocketService.ClientState.Error) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = (clientState as UnifiedWebSocketService.ClientState.Error).message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // Connection Form (only show if client mode is enabled)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Client Configuration",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // IP Address Input Field
                            OutlinedTextField(
                                value = state.serverIp,
                                onValueChange = { viewModel.onEvent(WebSocketSetupEvent.UpdateServerIp(it)) },
                                label = { Text("Server IP Address") },
                                placeholder = { Text("192.168.1.100") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                                singleLine = true
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Display the formatted URL
                            if (state.serverUrl.isNotBlank()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Text(
                                            text = "Server URL:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Text(
                                            text = state.serverUrl,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                            
                            if (state.isServerRunning && state.serverIp.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "✓ Auto-populated from local server",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.onEvent(WebSocketSetupEvent.Connect) },
                                    modifier = Modifier.weight(1f),
                                    enabled = state.serverIp.isNotBlank() && 
                                             !state.isConnecting && !state.isClientConnected
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Connect")
                                }
                                
                                Button(
                                    onClick = { viewModel.onEvent(WebSocketSetupEvent.Disconnect) },
                                    modifier = Modifier.weight(1f),
                                    enabled = state.isClientConnected,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Disconnect")
                                }
                            }
                            
                            // Test connection button for debugging
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { 
                                    // Test with a hardcoded localhost URL
                                    viewModel.testConnection("ws://192.168.1.100:8080")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !state.isConnecting && !state.isClientConnected
                            ) {
                                Icon(Icons.Default.Build, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Test Connection (Debug)")
                            }
                            
                            if (clientState is UnifiedWebSocketService.ClientState.Error) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.onEvent(WebSocketSetupEvent.Reconnect) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Reconnect")
                                }
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // Connection Info (only show if client mode is enabled and connected)
                if (state.isClientConnected) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Connection Info",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text("Server IP: ${state.serverIp}")
                                Text("Server URL: ${state.serverUrl}")
                                Text("Status: Connected")
                            }
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Client Chat Box (show when client is connected)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "💬 Client Chat",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Send message to server",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Message Input and Send Button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = state.messageText,
                                        onValueChange = { viewModel.onEvent(WebSocketSetupEvent.UpdateMessageText(it)) },
                                        label = { Text("Type your message...") },
                                        placeholder = { Text("Hello from Client!") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        maxLines = 3
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Button(
                                        onClick = { 
                                            if (state.messageText.isNotBlank()) {
                                                viewModel.onEvent(WebSocketSetupEvent.SendMessage)
                                            }
                                        },
                                        enabled = state.messageText.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = "Send")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Send")
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Go to Home Button
                                Button(
                                    onClick = { 
                                        navController.navigate("home") {
                                            popUpTo("websocket_setup") { inclusive = true }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(Icons.Default.Home, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Go to Home")
                                }
                            }
                        }
                    }
                }
            }
            
            // Add extra padding at the bottom for better scrolling experience
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
