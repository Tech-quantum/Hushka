# PromptVault

Native Android prompt manager built with Kotlin, Jetpack Compose, Material 3 and Room.

## Features
- Prompt library with smart (Persian/English, typo-tolerant) search
- Categories, providers, collections, favorites, pinned and recently used prompts
- Add / edit / delete your own prompts
- Built-in catalog (`app/src/main/assets/prompts_catalog_v1.2.0.json`) plus online library update from prompts.chat
- Persian, English, Arabic and Turkish UI with RTL support, light/dark theme
- Voice search through the system speech recognizer

## Build
The repository has no Gradle wrapper. Either open it in Android Studio, or run
`gradle wrapper --gradle-version 8.7` once and then `./gradlew assembleDebug`.
The GitHub Actions workflow builds and tests a **debug** APK only; release signing is not configured.
