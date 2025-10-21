# TileVision

A Kotlin Compose Multiplatform project for AR-based tile measurement and visualization.

## Project Structure

- **:shared** - Common KMP module with business logic, UI, and navigation
- **:androidApp** - Android application with ARCore integration
- **:iosApp** - iOS application with ARKit integration

## Features

- **Cross-platform UI** using Jetpack Compose Multiplatform
- **AR Measurement** capabilities using ARCore (Android) and ARKit (iOS)
- **Navigation** with Compose Navigation
- **Material 3** design system
- **Dependency Injection** with Koin
- **Coroutines** for asynchronous operations
- **Serialization** support with kotlinx.serialization

## Screens

1. **Onboarding** - Welcome screen with app introduction
2. **Home** - Project list and main navigation
3. **Measure** - AR measurement interface
4. **ProjectDetail** - Individual project details and measurements
5. **Settings** - App preferences and configuration

## Technology Stack

### Shared Module
- Kotlin Multiplatform
- Jetpack Compose Multiplatform
- Navigation Compose
- Koin for DI
- Coroutines
- kotlinx.serialization
- Material 3

### Android App
- ARCore for AR functionality
- Camera permissions
- Material 3 theming

### iOS App
- ARKit for AR functionality
- UIKit integration
- Camera permissions

## Getting Started

### Prerequisites
- Android Studio (latest version)
- Xcode (for iOS development)
- Kotlin 2.0.21+
- JDK 8+

### Building the Project

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Build the shared module: `./gradlew :shared:build`
5. Run Android app: `./gradlew :androidApp:installDebug`
6. For iOS: Open `iosApp/TileVision.xcodeproj` in Xcode and build

### Running on Android
```bash
./gradlew :androidApp:installDebug
```

### Running on iOS
1. Open `iosApp/TileVision.xcodeproj` in Xcode
2. Select your target device/simulator
3. Build and run

## Architecture

The project follows a modular architecture:

- **Presentation Layer**: Compose UI screens and navigation
- **Business Logic**: Shared across platforms in the shared module
- **Platform-specific**: AR implementations for Android (ARCore) and iOS (ARKit)

## AR Integration

### Android (ARCore)
- Camera permissions required
- ARCore SDK integration
- World tracking and plane detection

### iOS (ARKit)
- Camera permissions required
- ARKit framework integration
- World tracking and plane detection

## Development Notes

- The shared module contains all common UI and business logic
- Platform-specific AR implementations are in their respective app modules
- Navigation is handled entirely in Compose
- Material 3 theming is applied consistently across platforms

## License

This project is licensed under the MIT License.
