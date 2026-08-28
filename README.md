# Minimal Hello

A production-grade ultra-minimalist Hello World application with live CSS variable token theming. Built entirely with Jetpack Compose (no XML layouts), structured as a multi-module MVVM project.

## Architecture

```
minimal-hello/
├── app/                        # App shell: single Activity + navigation assembly + theme
├── core/
│   ├── data/                   # Data layer (Hilt): GreetingRepository, UserPreferencesRepository
│   ├── database/               # Room: persists theme & typography preferences
│   ├── navigation/             # Navigation 3 infrastructure (AppNavigator / NavigationState)
│   ├── network/                # Retrofit / OkHttp (Hilt-provided, placeholder service)
│   └── ui/                     # Design tokens, MinimalTheme, shared utilities
├── feature/
│   └── greeting/
│       ├── api/                # Navigation contract (GreetingNavKey)
│       └── impl/               # UI + GreetingViewModel (MVVM)
└── gradle/libs.versions.toml   # Version catalog
```

- **MVVM**: `GreetingViewModel` owns all feature state (theme, typography, tab, sidebar, inspector, greeting content) as `StateFlow`s; user preferences are persisted through Room via the data layer.
- **Navigation 3**: destinations are declared as a serializable `NavKey` contract in `feature:greeting:api`; the app shell assembles them through `NavDisplay` + `entryProvider`.
- **DI**: Hilt 2.x wires the database, network, data and ViewModel layers.

## Tech Stack

| Item | Version |
| :--- | :--- |
| AGP | 9.1.1 (compileSdk 37) |
| Kotlin | 2.2.10 |
| Compose BOM | 2026.08.00 (Material 3) |
| Navigation 3 | 1.1.4 |
| Hilt | 2.60.1 |
| Room | 2.7.0 |
| minSdk / targetSdk | 24 / 36 |
| JDK | 21 (required by Robolectric SDK 36) |

## Run Locally

**Prerequisites:** JDK 21, Android SDK (platform 37 + build-tools), and Android Studio or command-line Gradle 9.7+.

1. Open the project in Android Studio (or run any Gradle task from the CLI).
2. Allow Gradle sync to finish.
3. Run the `app` configuration on an emulator or physical device.

> **Debug signing:** the `debug` build type uses a signing config pointing to `debug.keystore` in the project root (git-ignored). If you don't have it, generate a standard one:
>
> ```bash
> keytool -genkeypair -v -keystore debug.keystore -alias androiddebugkey \
>   -storepass android -keypass android -keyalg RSA -keysize 2048 \
>   -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
> ```
>
> …or remove `debug { signingConfig = signingConfigs.getByName("debugConfig") }` from `app/build.gradle.kts` to fall back to default debug signing.

## Testing

```bash
gradle :app:testDebugUnitTest
```

- Robolectric tests run against **SDK 36** (see `app/src/test/resources/robolectric.properties`), which requires Java 21.
- `GreetingScreenshotTest` renders the migrated `TokensScreen` via Roborazzi. To (re)generate the golden image, run once with `-Proborazzi.test.record=true`.

## Release Build

Release signing is configured via environment variables:

- `KEYSTORE_PATH` (path to your upload keystore)
- `STORE_PASSWORD`
- `KEY_PASSWORD`
