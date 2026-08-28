# Minimal Hello

A production-grade ultra-minimalist Hello World application with live CSS variable token theming. Built entirely with Jetpack Compose (no XML layouts).

## Features

- 12 curated design themes (6 palette families × Light/Dark) with animated hot-swapping
- CSS custom property token model with `:root` export and live `--primary` override
- Typewriter boot splash screen (replayable)
- Push-canvas sidebar drawer, 4-tab navigation
- Typography Studio, Design Tokens panel, CSS Variables Inspector

## Tech Stack

- AGP 9.x, Kotlin 2.2, Jetpack Compose (Material 3)
- minSdk 24 / targetSdk 36

## Run Locally

**Prerequisites:** [Android Studio](https://developer.android.com/studio) with Android SDK 36.1 installed.

1. Open Android Studio, select **Open**, and choose this project directory.
2. Allow Android Studio to finish Gradle sync.
3. Run the app on an emulator or physical device.

> **Note:** The `debug` build type uses a custom signing config pointing to `debug.keystore` in the project root. If you don't have that file, either generate one, or remove the line
> `debug { signingConfig = signingConfigs.getByName("debugConfig") }` from `app/build.gradle.kts`
> to fall back to the default debug signing.

## Release Build

Release signing is configured via environment variables:

- `KEYSTORE_PATH` (defaults to `my-upload-key.jks` in the project root)
- `STORE_PASSWORD`
- `KEY_PASSWORD`
