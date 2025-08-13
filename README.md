# JPMPOS Blade

A modern Android POS (Point of Sale) application built with Jetpack Compose, Material3, and MVVM architecture.

## 🚀 Features

- **Modern UI**: Built with Jetpack Compose and Material3
- **MVVM Architecture**: Clean separation of concerns with ViewModels and LiveData
- **Navigation**: Compose Navigation for seamless screen transitions
- **Dependency Injection**: Hilt for clean dependency management
- **Coroutines**: Asynchronous programming with Kotlin Coroutines
- **Material3 Design**: Latest Material Design components and theming

## 🏗️ Architecture

The application follows the MVVM (Model-View-ViewModel) architecture pattern:

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

## 🛠️ Dependencies

### Core Dependencies
- **Jetpack Compose**: Modern UI toolkit
- **Material3**: Latest Material Design components
- **Navigation Compose**: Navigation between screens
- **Hilt**: Dependency injection
- **Coroutines**: Asynchronous programming
- **ViewModel & LiveData**: MVVM architecture support

### Version Information
- **Compile SDK**: 36
- **Min SDK**: 26
- **Target SDK**: 36
- **Kotlin**: 2.0.21
- **Compose BOM**: 2024.09.00

## 📱 Screens

### Splash Screen
- Application branding display
- Automatic navigation to home after 2 seconds

### Home Screen
- Material3 design with navigation drawer
- MVVM implementation with ViewModel
- Repository integration for data management

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK 36
- Kotlin 2.0.21

### Installation
1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Build and run the application

### Project Structure
```
app/src/main/java/relanto/jpn/nrf/
├── base/                    # Base classes
│   └── BaseViewModel.kt    # Abstract ViewModel base
├── data/                   # Data layer
│   └── repository/         # Repository implementations
├── di/                     # Dependency injection
│   └── RepositoryModule.kt # Hilt modules
├── navigation/             # Navigation setup
│   ├── NavGraph.kt        # Navigation graph
│   └── Screen.kt          # Route definitions
├── ui/                     # UI components
│   ├── components/         # Reusable components
│   ├── screens/            # Screen implementations
│   └── theme/              # App theming
└── MainActivity.kt         # Main activity
```

## 🔧 Configuration

### Hilt Setup
The application uses Hilt for dependency injection. The main application class is annotated with `@HiltAndroidApp`.

### Navigation Setup
Navigation is configured using Compose Navigation with a sealed class `Screen` defining all routes.

### Material3 Theming
The application uses Material3 theming with custom color schemes and typography.

## 📚 Usage Examples

### Creating a New Screen
1. Create a new screen in `ui/screens/`
2. Create a corresponding ViewModel extending `BaseViewModel`
3. Add the route to `Screen.kt`
4. Update the navigation graph in `NavGraph.kt`

### Adding Dependencies
1. Update `gradle/libs.versions.toml` with new versions
2. Add library references
3. Update `app/build.gradle.kts` with new dependencies

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For support and questions, please open an issue in the repository.
