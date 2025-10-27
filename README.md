# TileVision AR

TileVision AR is a professional Android application that leverages cutting-edge augmented reality (AR) technology to provide precise project area measurements, tile sample analysis, and comprehensive job management for construction and renovation projects. This innovative tool streamlines tile installation planning by delivering accurate measurements, intelligent tile calculations, and professional job summaries.

## Features

### 🎯 AR Measurement Tools

#### Project Area Measurement
- **AR-based polygon measurement**: Measure irregular project areas by placing multiple anchor points in 3D space using ARCore
- **Real-time area calculation**: Displays area in square feet with instant feedback
- **Floating instruction overlay**: Animated guidance popup with tap-to-dismiss
- **Skip AR option**: Jump directly to calculator if AR tracking is unavailable
- **Save and continue workflow**: Save measured projects with metadata for future reference

#### Tile Sample Measurement
- **AR tile measurement**: Measure individual tile samples to get accurate width and height
- **Finger trace interface**: Draw around tile edges to capture dimensions
- **Automatic area calculation**: Calculates tile area in square feet from measured dimensions
- **Tile sample storage**: Save tile measurements with timestamps for reuse

### 📊 Tile Calculator

- **Smart calculations**: Automatically calculates tiles needed based on project area and tile dimensions
- **Layout options**: Straight, Staggered, and Herringbone layout styles
- **Grout gap support**: 1/16", 1/8", 3/16", 1/4" grout gap options
- **Waste factor**: Configurable waste/breakage percentages (0-50%) with slider input
- **Integration with measurements**: Seamlessly uses saved project areas and tile samples
- **Two-card interface**: Clean separation of inputs and results
- **Real-time updates**: Calculation updates as you change inputs

### 💼 Job Management

#### Save Job Summaries
- **Comprehensive job records**: Save complete job details including area, tile specs, layout, waste, and notes
- **Project references**: Link to source measurements for traceability
- **Timestamp tracking**: Automatic creation timestamps for job organization
- **Custom naming**: User-defined job names for easy identification

#### Saved Jobs View
- **Browse all jobs**: Grid/list view of all saved job summaries
- **Search and filter**: Quick access to recent jobs
- **Job details**: View complete job specifications
- **Delete with undo**: Safe deletion with undo option

#### PDF Export & Sharing
- **Professional PDF reports**: Generate branded PDF documents with complete job details
- **Share functionality**: Export to email, cloud storage, or messaging apps
- **Print-ready format**: Clean layout suitable for client presentation
- **Branded footer**: Includes TileVision branding and tagline

### 💾 Data Management

#### Persistent Storage
- **Survives app restarts**: All data persists using SharedPreferences and JSON serialization
- **Repository pattern**: Clean architecture with singleton repositories
- **Automatic saving**: Changes saved immediately after modification
- **Data integrity**: Automatic JSON deserialization on app launch

#### Data Types
- **Project Measurements**: Saved polygon measurements with area calculations
- **Tile Samples**: Saved tile dimensions and coverage area
- **Job Summaries**: Complete job specifications with calculation results

#### Organization
- **Grid view**: Visual card-based browsing for projects and tiles
- **Detail views**: Comprehensive information for each saved item
- **Recent measurements**: Quick access to recently measured items
- **Delete options**: Long-press and detail view deletion options

### 🎨 User Interface

#### Glassmorphism Design
- **Modern aesthetic**: Translucent glass cards with subtle borders
- **Gradient backgrounds**: Dynamic color gradients throughout the app
- **Consistent theming**: Unified design language across all screens
- **Smooth animations**: Floating effects and subtle transitions

#### Dark & Light Themes
- **System integration**: Follows system dark/light mode preference
- **Manual override**: Switch themes manually in Settings
- **Persistent choice**: Theme preference saved across sessions
- **Consistent colors**: Accent colors work in both themes

#### Navigation
- **AppHeaderView**: Consistent header with back/gear navigation
- **Dashboard Home**: Main tools grid, library access, and recent items
- **Bottom navigation**: Clear call-to-action buttons
- **Safe area support**: Respects system status bar and notches

### ⚙️ Settings & Preferences

- **Theme selection**: Light, Dark, or Follow System
- **Privacy policy**: Access to privacy information
- **Delete all data**: Complete data reset option with confirmation
- **Contact support**: Easy access to support email
- **App version**: Display current version information
- **About dialog**: App description and copyright information

## Technology Stack

### Core Technologies
- **Android SDK**: Native Android development
- **Kotlin**: Primary programming language (100%)
- **ARCore**: Google's augmented reality platform for 3D tracking
- **Sceneform**: 3D rendering and AR scene management

### Data Persistence
- **SharedPreferences**: Local data storage with JSON serialization
- **Gson**: JSON serialization for complex data objects
- **Repository Pattern**: Clean singleton-based architecture
- **FileProvider**: Secure file sharing for PDF exports

### UI/UX Framework
- **Material Design 3**: Modern Android design system
- **Material Components**: Buttons, Cards, TextFields, Sliders
- **Custom Views**: AppHeaderView, InstructionPopupView
- **RecyclerView**: Efficient list and grid displays
- **RecyclerView Layout Managers**: GridLayoutManager, LinearLayoutManager

### Architecture
- **Activity-based**: Traditional Android navigation
- **Intent-based communication**: ActivityResult API for data passing
- **Singleton repositories**: ProjectRepository, TileSampleRepository, ProjectSummaryRepository
- **Utility classes**: MeasurementUtils, ThemeManager, PdfExporter

### Additional Libraries
- **Material Design Components**: UI component library
- **AndroidX**: Modern Android support libraries

## System Requirements

- **Android 7.0+** (API level 24 or higher)
- **OpenGL ES 3.1** support
- **ARCore-compatible device** (see [Google's supported devices list](https://developers.google.com/ar/devices))
- **Google Play Services for AR** (automatically installed from Google Play Store)

## Installation

The app is available through:
- **Google Play Store**: Available soon
- **GitHub Releases**: [Download APK from GitHub](https://github.com/nwc6624/TileVision/releases)
- **Direct Installation**: Build from source using Android Studio

## Usage Guide

### Measuring a Project Area
1. Launch the app from your home screen
2. Tap "Measure Project Area" from the Main Tools grid
3. Point your device camera at the floor, wall, or surface to measure
4. Wait for AR plane detection (horizontal yellow grid appears)
5. Tap to place anchor points around the perimeter
6. Watch the floating instruction guide, tap to dismiss it
7. Continue adding points until you've outlined the entire area
8. Tap "Confirm" to calculate the area
9. Review the measurement in ft²
10. Choose to:
    - Save and Continue (saves to library, opens calculator)
    - Continue Without Saving (opens calculator only)
    - Undo/Retake (start over)

### Measuring a Tile Sample
1. Tap "Measure Tile Sample" from Main Tools or from the calculator
2. Point your camera at a single tile
3. Wait for AR plane detection
4. Finger trace around the edges of the tile
5. Tap "Done" when complete
6. Review the calculated dimensions (width and height in inches)
7. Choose to save or continue to calculator

### Calculating Tiles Needed
1. Open "Tile Calculator" from Main Tools
2. Enter or select a project area (from AR measurement or manual input)
3. Enter tile dimensions (from AR measurement or manual input)
4. Select layout style (Straight, Staggered, or Herringbone)
5. Choose grout gap size (1/16", 1/8", 3/16", or 1/4")
6. Adjust waste percentage using the slider (default 10%)
7. Optionally add notes (room name, grout color, etc.)
8. View the calculated result: tiles needed, waste included, rounded total
9. Tap "Save Job Summary" to save this calculation
10. Tap "Export / Share PDF" to generate and share a professional report

### Managing Saved Data

#### Viewing Saved Projects
1. Tap "View Saved Projects" in Your Library section
2. Browse projects in a 2-column grid
3. Tap any project to view details
4. Long-press to delete with confirmation

#### Viewing Saved Tile Samples
1. Tap "View Saved Tile Samples" in Your Library section
2. Browse tile samples in a 2-column grid
3. Tap any tile to view details
4. Long-press to delete with confirmation

#### Viewing Saved Jobs
1. Tap "View Saved Jobs" in Your Library section
2. Browse job summaries in a list
3. Each row shows: job name, area, tiles needed, and timestamp
4. Tap a job to view complete details
5. From the detail view:
   - Export / Share PDF for client presentation
   - Delete the job with undo option
   - Open in Calculator to modify and recalculate

#### Working with Job Summaries
1. In the job detail view, review all specifications:
   - Project area and layout style
   - Tile dimensions and coverage
   - Calculation breakdown (raw tiles, waste, final count)
   - Notes and creation date
2. Tap "Export / Share PDF" to generate a professional PDF report
3. Share via email, messaging, or cloud storage
4. Tap "Open in Calculator" to modify and create a new calculation
5. Tap "Delete Job" with confirmation to remove (undo available)

### Settings and Preferences
1. Tap the gear icon (⚙️) in the top right of the home screen
2. **Theme**: Switch between Light, Dark, or Follow System
3. **Privacy Policy**: View our privacy practices
4. **Delete All Data**: Remove all saved projects, tiles, and jobs (confirmation required)
5. **Contact Support**: Send an email to our support team
6. **Version Info**: See the current app version

## Development

### Building from Source
1. Clone the repository:
   ```bash
   git clone https://github.com/nwc6624/TileVision.git
   cd TileVision
   ```

2. Open in Android Studio Arctic Fox or later
3. Ensure you have Android SDK 24+ installed
4. Sync Gradle dependencies
5. Build and run on an ARCore-compatible device

### Project Structure
```
app/src/main/
├── java/de/westnordost/streetmeasure/
│   ├── Activities/
│   │   ├── HomeActivity.kt              # Dashboard and navigation hub
│   │   ├── MeasureActivity.kt           # AR project area measurement
│   │   ├── TileSampleMeasureActivity.kt # AR tile measurement
│   │   ├── TileCalculatorActivity.kt    # Tile calculation interface
│   │   ├── SavedProjectsActivity.kt     # Saved projects grid view
│   │   ├── SavedTileSamplesActivity.kt  # Saved tiles grid view
│   │   ├── SavedSummariesActivity.kt    # Saved jobs list view
│   │   ├── ProjectSummaryDetailActivity.kt # Job detail and PDF export
│   │   └── SettingsActivity.kt          # App settings and preferences
│   ├── Repositories/
│   │   ├── ProjectRepository.kt         # Project measurements storage
│   │   ├── TileSampleRepository.kt      # Tile samples storage
│   │   └── ProjectSummaryRepository.kt  # Job summaries storage
│   ├── Models/
│   │   ├── ProjectMeasurement.kt        # Project data model
│   │   ├── TileSample.kt                # Tile sample data model
│   │   └── ProjectSummary.kt            # Job summary data model
│   ├── Views/
│   │   ├── AppHeaderView.kt             # Reusable header component
│   │   ├── InstructionPopupView.kt      # AR guidance overlay
│   │   └── PolygonPreviewView.kt        # 2D polygon visualization
│   ├── Utils/
│   │   ├── MeasurementUtils.kt          # Measurement calculations
│   │   ├── ThemeManager.kt              # Theme management
│   │   └── PdfExporter.kt               # PDF generation
│   └── Services/
│       └── ExportManager.kt             # Data export utilities
```

### Key Components
- **Activities**: 10 main activities for complete user workflows
- **Repositories**: 3 singleton repositories for data management
- **Models**: 3 data classes with full persistence support
- **Custom Views**: Reusable UI components
- **Utilities**: Helper classes for calculations and formatting

### Gradle Configuration
- **minSdk**: 24 (Android 7.0)
- **targetSdk**: 33 (Android 13)
- **compileSdk**: 33
- **Kotlin**: 1.8.0+
- **Material Components**: Latest stable
- **ARCore**: Google Play Services for AR

## Features Roadmap

### Upcoming Features
- [ ] Cloud sync for cross-device access
- [ ] Advanced floor plans and room templates
- [ ] Material cost estimation
- [ ] Integration with tile manufacturer databases
- [ ] Augmented reality tile preview
- [ ] Batch job management
- [ ] Client sharing with web view

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This software is released under the terms of the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).

## Copyright

© 2025 TileVision. All rights reserved. This is an original work developed independently for professional tile measurement and calculation applications.

## Contact

- **Support Email**: support@tilevision.example
- **GitHub**: [nwc6624/TileVision](https://github.com/nwc6624/TileVision)
- **Issues**: [Report a bug or request a feature](https://github.com/nwc6624/TileVision/issues)

## Acknowledgments

TileVision AR was developed as a comprehensive solution for construction professionals and DIY enthusiasts who need accurate tile measurements and calculations. The application combines advanced AR technology with intuitive user interfaces to deliver professional-grade measurement tools in a mobile format.

Built with ❤️ using ARCore, Material Design 3, and modern Android development practices.