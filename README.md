# JPMPOS Blade 🚀

A modern Android Point of Sale (POS) application built with Jetpack Compose, Material3, and MVVM architecture. Features real-time WebSocket communication for seamless device-to-device coordination in POS environments.

## ✨ Features

- **🎨 Modern UI**: Built with Jetpack Compose and Material3 design system
- **🏗️ Clean Architecture**: MVVM pattern with Repository layer and Dependency Injection
- **🌐 WebSocket Communication**: Dual-mode server/client functionality for real-time data exchange
- **📱 Responsive Design**: Edge-to-edge UI with Material3 theming
- **🔄 Real-time Sync**: Instant message delivery and status updates
- **🔌 Local Network**: Optimized for local network POS operations
- **📊 Connection Monitoring**: Live connection status and client count tracking

## 🏗️ Architecture

The application follows the **MVVM (Model-View-ViewModel)** architecture pattern:

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│     View        │    │    ViewModel     │    │     Model       │
│  (Compose UI)   │◄──►│   (State/Event)  │◄──►│  (Repository)   │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

### Key Components

- **BaseViewModel**: Abstract base class providing common ViewModel functionality
- **Repository Pattern**: Data layer abstraction for clean data management
- **Navigation**: Compose Navigation with sealed class route definitions
- **Dependency Injection**: Hilt modules for clean dependency management
- **WebSocket Service**: Unified service handling both server and client operations

## 🛠️ Tech Stack

### Core Technologies
- **Android SDK**: 36 (Android 14)
- **Kotlin**: 2.0.21
- **Jetpack Compose**: 2024.09.00 BOM
- **Material3**: Latest Material Design components

### Architecture & Dependencies
- **MVVM**: ViewModel, LiveData, StateFlow
- **Navigation**: Compose Navigation 2.7.0
- **Dependency Injection**: Hilt 2.48
- **Asynchronous**: Kotlin Coroutines 1.7.3
- **Networking**: OkHttp 4.12.0, Java-WebSocket 1.5.4

## 📱 Screens

### 1. WebSocket Setup Screen
- **Default Start Screen**: Application begins here
- **Mode Selection**: Choose between Server or Client mode
- **Connection Management**: Start server, connect to clients
- **Real-time Status**: Live connection monitoring

### 2. Home Screen
- **Main Interface**: Primary application functionality
- **Material3 Design**: Navigation drawer and modern UI
- **MVVM Implementation**: Clean state management

### 3. Splash Screen
- **Branding Display**: Application introduction
- **Auto-navigation**: Automatic transition to main flow

## 🌐 WebSocket Communication

### Server Mode
- **Automatic Setup**: Detects local IP and starts server
- **Port Configuration**: Default port 8080 (configurable)
- **Multi-client Support**: Handles multiple concurrent connections
- **Message Echo**: All received messages are echoed back
- **Status Monitoring**: Real-time client count and connection status

### Client Mode
- **Flexible Connection**: Connect to any WebSocket server
- **Real-time Messaging**: Send and receive messages instantly
- **Connection Health**: PING/PONG functionality for connection monitoring
- **Auto-reconnection**: Handles connection drops gracefully

### Supported Messages
- **PING** → Responds with **PONG**
- **STATUS** → Returns server information
- **Custom Messages** → Echoed back with "Echo: " prefix

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Hedgehog (2023.1.1) or later
- **Android SDK**: 36 (Android 14)
- **Kotlin**: 2.0.21
- **Java**: JDK 11 or later

### Installation

1. **Clone the Repository**
   ```bash
   git clone https://github.com/yourusername/JPMPOSBlade.git
   cd JPMPOSBlade
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory
   - Click "OK"

3. **Sync Project**
   - Wait for Gradle sync to complete
   - Resolve any dependency issues if they arise

4. **Build and Run**
   - Connect an Android device or start an emulator
   - Click the "Run" button (▶️) or press Shift+F10

### First Run Setup

1. **Grant Permissions**: Allow network and notification permissions when prompted
2. **WebSocket Setup**: The app starts at the WebSocket Setup screen
3. **Choose Mode**: Select Server or Client mode based on your needs
4. **Start Communication**: Begin WebSocket operations

## 🔧 Configuration

### Network Setup
- **Local Network**: Both devices must be on the same WiFi network
- **Port Configuration**: Default port 8080 (configurable in code)
- **Firewall**: Ensure port 8080 is not blocked
- **Permissions**: Grant network access permissions

### Hilt Configuration
The application uses Hilt for dependency injection:
```kotlin
@HiltAndroidApp
class JPMPOSBladeApplication : Application()
```

### Navigation Setup
Routes are defined in `Screen.kt`:
```kotlin
sealed class Screen(val route: String) {
    object WebSocketSetup : Screen("websocket_setup")
    object Splash : Screen("splash")
    object Home : Screen("home")
}
```

## 📱 Usage Examples

### Setting Up as Server
1. Open the app on Device 1
2. Navigate to WebSocket Setup screen
3. Enable "Server Mode"
4. Click "Start Server"
5. Note the displayed IP address (e.g., 192.168.1.100)

### Connecting as Client
1. Open the app on Device 2
2. Navigate to WebSocket Setup screen
3. Enable "Client Mode"
4. Enter server URL: `ws://192.168.1.100:8080`
5. Click "Connect"

### Testing Communication
- **Send PING**: Test connection health
- **Send STATUS**: Get server information
- **Custom Messages**: Type any message to test echo functionality

## 🏗️ Project Structure

```
JPMPOSBlade/
├── app/
│   ├── src/main/java/relanto/jpn/nrf/
│   │   ├── base/                    # Base classes
│   │   │   └── BaseViewModel.kt    # Abstract ViewModel base
│   │   ├── data/                   # Data layer
│   │   │   └── repository/         # Repository implementations
│   │   ├── di/                     # Dependency injection
│   │   │   ├── RepositoryModule.kt # Hilt modules
│   │   │   └── WebSocketModule.kt  # WebSocket dependencies
│   │   ├── navigation/             # Navigation setup
│   │   │   ├── NavGraph.kt        # Navigation graph
│   │   │   └── Screen.kt          # Route definitions
│   │   ├── ui/                     # UI components
│   │   │   ├── components/         # Reusable components
│   │   │   ├── screens/            # Screen implementations
│   │   │   └── theme/              # App theming
│   │   ├── websocket/              # WebSocket services
│   │   │   └── UnifiedWebSocketService.kt
│   │   └── MainActivity.kt         # Main activity
│   ├── build.gradle.kts            # App-level build configuration
│   └── proguard-rules.pro          # ProGuard rules
├── gradle/                          # Gradle configuration
│   └── libs.versions.toml          # Dependency versions
├── build.gradle.kts                 # Project-level build configuration
└── README.md                        # This file
```

## 🔍 Troubleshooting

### Common Issues

#### Server Won't Start
- **Problem**: "Failed to get local IP address" error
- **Solution**: Ensure device is connected to WiFi network
- **Check**: Look at logs for network interface detection

#### Client Can't Connect
- **Problem**: Connection refused or timeout
- **Solution**: 
  - Verify server is running on Device 1
  - Check IP address is correct
  - Ensure both devices are on same WiFi network
  - Check firewall settings

#### Permission Denied
- **Problem**: Network access denied
- **Solution**: 
  - Grant all requested permissions
  - Check Android settings for network permissions
  - Restart app after granting permissions

### Debug Information
Check Logcat with these tags:
- `UnifiedWebSocketService`: All WebSocket-related logs
- `WebSocketSetupViewModel`: UI state logs

## 🔒 Security Considerations

### Current Implementation
- **Local Network Only**: Designed for local network testing
- **No Authentication**: Basic implementation for development
- **Cleartext Communication**: Uses ws:// protocol
- **Development Focus**: Not suitable for production use

### Production Recommendations
- **Secure WebSocket**: Use wss:// with SSL/TLS
- **Authentication**: Implement user authentication
- **Rate Limiting**: Add connection and message rate limits
- **Input Validation**: Validate all incoming messages
- **Logging**: Implement comprehensive security logging

## 🚀 Future Enhancements

- **Secure WebSocket Support**: wss:// protocol implementation
- **Message Encryption**: End-to-end message encryption
- **User Authentication**: Login and user management
- **Connection Pooling**: Optimized connection management
- **Message Queuing**: Reliable message delivery
- **Broadcast Messaging**: One-to-many communication
- **Connection Dashboard**: Advanced monitoring interface
- **Offline Support**: Message queuing when offline
- **Push Notifications**: Real-time alerts and updates

## 🤝 Contributing

We welcome contributions! Here's how you can help:

1. **Fork the Repository**
2. **Create a Feature Branch**
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **Make Your Changes**
4. **Commit Your Changes**
   ```bash
   git commit -m 'Add amazing feature'
   ```
5. **Push to the Branch**
   ```bash
   git push origin feature/amazing-feature
   ```
6. **Open a Pull Request**

### Development Guidelines
- Follow Kotlin coding conventions
- Use meaningful commit messages
- Add tests for new functionality
- Update documentation as needed
- Ensure all tests pass before submitting

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

### Getting Help
- **Issues**: Open an issue in the repository
- **Documentation**: Check the [WEBSOCKET_SETUP.md](WEBSOCKET_SETUP.md) for detailed WebSocket setup
- **Code Examples**: Review the source code for implementation details

### Community
- **Discussions**: Use GitHub Discussions for questions
- **Wiki**: Check the project wiki for additional resources
- **Examples**: Look at the test files for usage examples

## 🙏 Acknowledgments

- **Jetpack Compose Team**: For the amazing UI toolkit
- **Material Design Team**: For the beautiful design system
- **Hilt Team**: For dependency injection framework
- **Java-WebSocket Team**: For the WebSocket implementation

---

**Made with ❤️ for the Android development community**

*JPMPOS Blade - Powering the future of Point of Sale systems*
