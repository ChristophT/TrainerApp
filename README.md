# Slalom Trainer - Android App

A native Android app for timing whitewater slalom training sessions with millisecond precision. Built with Kotlin, Jetpack Compose, and modern Android architecture.

## 🎯 Features

- **High-Precision Timing**: Millisecond-accurate run timing using `SystemClock.elapsedRealtime()`
- **Training Sessions**: Start/end training sessions with multiple athletes
- **Real-time Updates**: 60fps timer updates for all active runs
- **Run Management**: Start/stop individual runs, add notes after completion
- **Athlete Management**: Full CRUD operations for athletes
- **Group Management**: Create groups of athletes for quick session setup
- **Results & Analytics**: View past training sessions and individual athlete runs
- **CSV Export**: Export athlete data for external analysis
- **Internationalization**: German (default) and English support
- **Dark Mode**: Full Material 3 dark theme support
- **Offline-first**: All data stored locally with Room database

## 🏗️ Architecture

### Clean Architecture + MVVM

The app follows Clean Architecture principles with three distinct layers:

```
┌─────────────────────────────────────────┐
│     Presentation Layer                  │
│  (Compose UI + ViewModels)              │
│  - TrainingViewModel (shared)           │
│  - Screens, Components, Theme           │
└─────────────┬───────────────────────────┘
              │
┌─────────────▼───────────────────────────┐
│     Domain Layer                        │
│  (Business Logic)                       │
│  - Models (Athlete, Run, Training...)  │
│  - Repository Interfaces                │
│  - (Use Cases - optional/future)        │
└─────────────┬───────────────────────────┘
              │
┌─────────────▼───────────────────────────┐
│     Data Layer                          │
│  (Persistence)                          │
│  - Room Database (6 entities, 4 DAOs)  │
│  - Repository Implementations           │
│  - Entity-Domain Mappers                │
└─────────────────────────────────────────┘
```

### Key Design Decisions

1. **Single Shared ViewModel**: `TrainingViewModel` manages all app state, mirroring the web app's React Context pattern
2. **Room Database**: Type-safe local persistence with foreign keys and cascade deletes
3. **StateFlow**: Reactive state management with Flow collectors
4. **Hilt DI**: Dependency injection for testability and clean architecture
5. **Material 3**: Modern design system with dynamic theming

## 📁 Project Structure

```
de.hardtthelen.trainerapp/
├── app/
│   ├── MainActivity.kt                 # Single activity entry point
│   ├── TrainerApplication.kt           # Hilt application class
│   └── navigation/                     # Compose navigation
│
├── di/                                 # Hilt DI modules
│   ├── DatabaseModule.kt
│   └── RepositoryModule.kt
│
├── data/                               # Data layer
│   ├── local/
│   │   ├── database/
│   │   │   ├── TrainerDatabase.kt
│   │   │   └── dao/                    # Room DAOs (4 files)
│   │   └── entity/                     # Room entities (6 files)
│   ├── repository/                     # Repository implementations (4 files)
│   └── mapper/                         # Entity ↔ Domain mappers
│
├── domain/                             # Domain layer
│   ├── model/                          # Domain models (5 files)
│   └── repository/                     # Repository interfaces (4 files)
│
└── presentation/                       # Presentation layer
    ├── theme/                          # Material 3 theme
    ├── component/                      # Reusable components
    │   ├── TimerDisplay.kt
    │   ├── AthleteRunCard.kt
    │   └── BottomNavBar.kt
    ├── screen/                         # Screen implementations
    │   ├── training/
    │   │   ├── TrainingScreen.kt
    │   │   └── TrainingViewModel.kt
    │   ├── athletes/AthletesScreen.kt
    │   ├── groups/GroupsScreen.kt
    │   └── results/ResultsScreen.kt
    └── util/                           # Utilities
        ├── TimerManager.kt
        ├── FormatUtil.kt
        └── CSVExporter.kt
```

## 🚀 Building & Running

### Prerequisites

- **Android Studio**: Hedgehog (2023.1.1) or newer
- **JDK**: 17 or newer
- **Android SDK**: API 35 (Android 15)
- **Minimum Android Version**: API 26 (Android 8.0) - 95%+ device coverage

### Build Instructions

1. **Clone the repository**:
   ```bash
   cd /path/to/trainerApp
   ```

2. **Setup Android SDK**:
   - Install Android Studio
   - Open SDK Manager (Tools → SDK Manager)
   - Install Android SDK Platform 35
   - Install Android SDK Build-Tools 35.0.0

3. **Configure local.properties** (if not auto-generated):
   ```properties
   sdk.dir=/path/to/Android/Sdk
   ```

4. **Build the project**:
   ```bash
   # Debug build
   ./gradlew assembleDebug

   # Release build
   ./gradlew assembleRelease
   ```

5. **Install on device**:
   ```bash
   # Install debug APK
   ./gradlew installDebug

   # Or use Android Studio: Run → Run 'app'
   ```

### Build Outputs

- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release APK**: `app/build/outputs/apk/release/app-release.apk`
- **App Bundle** (for Play Store): `app/build/outputs/bundle/release/app-release.aab`

## 🧪 Testing

### Running Tests

```bash
# Unit tests
./gradlew test

# Integration tests (requires emulator/device)
./gradlew connectedAndroidTest

# Test coverage
./gradlew testDebugUnitTestCoverage
```

### Test Structure

- **Unit Tests**: `app/src/test/` - ViewModels, utilities, business logic
- **Integration Tests**: `app/src/androidTest/` - Repository + Database tests
- **UI Tests**: `app/src/androidTest/` - Compose UI tests

**Note**: Full test implementation is pending (Tasks #16-18). The architecture is test-ready with:
- Repository interfaces for easy mocking
- ViewModel business logic separated from UI
- Room in-memory database for testing

## 🌍 Internationalization

The app supports multiple languages:

- **German** (default): `res/values/strings.xml`
- **English**: `res/values-en/strings.xml`

All user-facing text is externalized to string resources. To add a new language:

1. Create `res/values-{locale}/strings.xml` (e.g., `values-fr` for French)
2. Translate all string entries from `values/strings.xml`

## 🎨 Theming

Material 3 with dynamic theming support:

- **Light Theme**: Default Material 3 color scheme
- **Dark Theme**: Automatic support with system preference
- **Dynamic Color**: Android 12+ can use wallpaper colors

Colors defined in `presentation/theme/Color.kt`:
- Primary: Blue (#0061A4)
- Timer Active: Green (#00C853)
- Timer Stopped: Blue (#0061A4)

## 🔧 Configuration

### Database

- **Name**: `trainer_database`
- **Version**: 1
- **Migration Strategy**: Fallback to destructive migration (development)
- **Location**: App-private storage (auto-backed up)

### Gradle Dependencies

Key libraries (see `gradle/libs.versions.toml`):

- Jetpack Compose BOM 2024.12.01
- Room 2.6.1
- Hilt 2.54
- Navigation Compose 2.8.5
- Kotlin 2.1.0

## 📱 App Permissions

None required! The app:
- Stores data locally (no STORAGE permission needed on Android 10+)
- Uses FileProvider for CSV export (no permission)
- No network, location, or camera access

## 🐛 Known Issues & Limitations

1. **Testing**: Test suite not yet implemented (architecture is test-ready)
2. **Release Signing**: Keystore configuration needed for production builds
3. **Voice Memos**: Mentioned in spec but not implemented (future feature)
4. **Backup/Restore**: No manual backup feature (relies on Android Auto Backup)

## 🚦 Performance

- **Timer Precision**: ±1ms (using `SystemClock.elapsedRealtime()`)
- **Timer Update Rate**: 60fps for smooth UI updates
- **App Size**: ~5-8 MB (varies with compression)
- **Minimum RAM**: 2 GB recommended
- **Battery Impact**: Low (no background services)

## 📊 Database Schema

**Tables**:
1. `athletes` - Athlete records
2. `runs` - Completed runs with times
3. `trainings` - Training sessions
4. `training_participants` - Many-to-many: trainings ↔ athletes
5. `training_groups` - Group definitions
6. `group_members` - Many-to-many: groups ↔ athletes

**Relationships**:
- Foreign keys with CASCADE delete
- Indexed foreign key columns for performance

## 🔄 Migration from Web App

To import data from the web app (localStorage):

1. Export data from web app (browser console):
   ```javascript
   console.log(JSON.stringify(localStorage));
   ```

2. Parse and insert into Room database via custom migration script
   (Script not yet implemented - manual data entry required)

## 📄 License

See project license file.

## 👥 Contributors

- Implementation based on TrainerApp_Lovable React/TypeScript web app
- Android native implementation: 2026

## 📞 Support

For issues or questions:
- Check GitHub Issues
- Review inline code documentation
- Consult Android developer documentation

---

**Built with ❤️ using Kotlin, Jetpack Compose, and Material 3**
