# TileVision

TileVision is an Android application that uses augmented reality (AR) technology to measure project areas and tile samples for tile installation planning. The app helps users calculate the number of tiles needed for their projects by providing accurate area measurements and tile calculations.

## Features

### Project Area Measurement
- **AR-based polygon measurement**: Users can measure irregular project areas by placing multiple anchor points in 3D space
- **Real-time area calculation**: Displays area in both square feet and square meters
- **Save and continue workflow**: Users can save measured projects with screenshots for future reference
- **Project management**: View, organize, and reuse previously measured projects

### Tile Sample Measurement
- **AR tile measurement**: Measure individual tile samples to get accurate dimensions
- **Automatic area calculation**: Calculates tile area from measured dimensions
- **Tile sample storage**: Save tile measurements with photos for reuse in calculations

### Tile Calculator
- **Smart calculations**: Automatically calculates tiles needed based on project area and tile dimensions
- **Waste factor support**: Includes configurable waste/breakage percentages
- **Box coverage calculation**: Determines number of tile boxes needed
- **Integration with measurements**: Seamlessly uses saved project areas and tile samples

### Data Management
- **Persistent storage**: All measurements are saved locally and survive app restarts
- **Visual grid interface**: Browse saved projects and tile samples in an organized card layout
- **Project reuse**: Select previously measured areas and tile samples for new calculations
- **Data export**: Share measurement results and calculations

## Technology Stack

### Core Technologies
- **Android SDK**: Native Android development
- **Kotlin**: Primary programming language
- **ARCore**: Google's augmented reality platform for 3D tracking and measurement
- **Sceneform**: 3D rendering and AR scene management

### Data Persistence
- **SharedPreferences**: Local data storage for user preferences and settings
- **Gson**: JSON serialization for complex data objects
- **Repository Pattern**: Clean architecture for data management

### UI/UX
- **Material Design 3**: Modern Android design system
- **RecyclerView**: Efficient list and grid displays
- **CardView**: Material card components for data presentation
- **ConstraintLayout**: Flexible and responsive layouts

### Architecture
- **MVVM Pattern**: Model-View-ViewModel architecture
- **Activity-based navigation**: Traditional Android navigation with Intent-based communication
- **Singleton repositories**: Centralized data management
- **Utility classes**: Reusable measurement and formatting functions

## System Requirements

- **Android 7.0+** (API level 24 or higher)
- **OpenGL ES 3.1** support
- **ARCore-compatible device** (see [Google's supported devices list](https://developers.google.com/ar/devices))
- **Google Play Services for AR** (automatically installed from Google Play Store)

## Installation

The app is available through:
- **Google Play Store**: [Download from Play Store](https://play.google.com/store/apps/details?id=de.westnordost.streetmeasure)
- **GitHub Releases**: [Download APK from GitHub](https://github.com/streetcomplete/StreetMeasure/releases/)
- **F-Droid**: Available through IzzyOnDroid repository

## Usage

### Measuring a Project Area
1. Launch the app and select "Measure Project Area"
2. Point your device camera at the area to be measured
3. Tap to place anchor points around the perimeter
4. Confirm the measurement when you have 3+ points
5. Choose to save the project or continue without saving
6. The measured area will be passed to the tile calculator

### Measuring a Tile Sample
1. Select "Measure Tile Sample" from the home screen or tile calculator
2. Point your camera at a single tile
3. Tap to capture the tile dimensions
4. Confirm the measurement
5. Choose to save the tile sample or continue without saving
6. The tile dimensions will be passed to the tile calculator

### Calculating Tiles Needed
1. Use a measured project area or enter area manually
2. Use a measured tile sample or enter tile dimensions manually
3. Set waste/breakage percentage if needed
4. Enter box coverage information
5. Tap "Calculate" to see tiles and boxes needed

### Managing Saved Data
1. Access "View Saved Projects" to see all measured project areas
2. Access "View Saved Tile Samples" to see all measured tile samples
3. Tap any saved item to view details
4. Use "Use This Project" or "Use This Tile" to apply to current calculation
5. Delete unwanted measurements with long-press or detail view

## Development

### Building from Source
1. Clone the repository
2. Open in Android Studio
3. Ensure you have Android SDK 24+ installed
4. Build and run on an ARCore-compatible device

### Key Components
- **MeasureActivity**: Main AR measurement interface for project areas
- **TileSampleMeasureActivity**: AR measurement interface for tile samples
- **TileCalculatorActivity**: Calculation interface and results display
- **SavedProjectsActivity**: Grid view of saved project measurements
- **SavedTileSamplesActivity**: Grid view of saved tile samples
- **ProjectRepository**: Data management for project measurements
- **TileSampleRepository**: Data management for tile samples

## License

This software is released under the terms of the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).

## History

This project evolved from StreetMeasure, which was originally a component within StreetComplete but was separated due to ARCore licensing constraints. The app has been enhanced with tile-specific features and improved user workflows for tile installation planning.