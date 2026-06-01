# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
./gradlew build                  # full build
./gradlew assembleDebug          # debug APK
./gradlew assembleRelease        # release APK
./gradlew test                   # unit tests (JVM)
./gradlew :app:test              # unit tests for app module only
./gradlew connectedAndroidTest   # instrumented tests (requires device/emulator)
./gradlew lint                   # lint
./gradlew clean                  # clean
```

## Project Structure

Two-module project:

- **`:app`** — Android application. Jetpack Compose UI, Material3 theming, edge-to-edge. Entry point: `MainActivity.kt`. Theme in `app/src/main/kotlin/com/joyner/notebook64/ui/theme/`.
- **`:notebook64`** — Kotlin JVM library module. Currently a stub (`Notebook64.kt`). Intended for shared/business logic.

Package root: `com.joyner.notebook64`

## Tech Stack

- **UI:** 100% Jetpack Compose + Material3. No XML layouts.
- **Language:** Kotlin 2.2.10
- **AGP:** 9.2.1 | **Gradle:** 9.4.1
- **SDK:** minSdk 23 / compileSdk+targetSdk 36
- **Dependencies:** Centralized in `gradle/libs.versions.toml` (version catalog). Add all new deps there.
- **DI / Networking / DB:** None currently. Add Hilt, Retrofit, Room via version catalog when needed.

## Architecture

No architecture pattern enforced yet. When adding features, follow MVVM with unidirectional data flow: ViewModel holds state as `StateFlow`, Compose screens observe via `collectAsStateWithLifecycle()`.

## Reference Document

`Cuaderno64_Junio_2016.pdf` — at project root. Read this for project requirements, design decisions, and feature specifications before implementing anything.

## Key Constraints

- Java 11 source/target compatibility (set in both modules).
- Dynamic color enabled for Android 12+ in `Theme.kt`; light/dark schemes defined there.
- `gradle.properties` sets JVM max heap to 2048m — don't lower this.
