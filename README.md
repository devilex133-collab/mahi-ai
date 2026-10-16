# Mahi AI — Phase 1 foundation

Versions: AGP 8.5.2 · Gradle 8.7 · Kotlin 1.9.24 · Compose compiler 1.5.14 · Compose BOM 2024.06.00 · JDK 17 · compileSdk/targetSdk 34 · minSdk 26.

## Build the APK from your phone
1. Create a GitHub repo and upload all files, keeping folders (including the hidden `.github` folder).
2. Actions tab → **Android CI** → **Run workflow**.
3. When the run is green, open it and download artifact **mahi-ai-debug-apk**, unzip, install the APK.
4. On failure, read the failed step's log or download **build-reports**.

The workflow installs Gradle 8.7 itself, so no wrapper jar is needed.

## Status
Home, navigation, in-memory settings exist. Chat, Voice, Memory, Coding show a "Planned" screen. No AI, Firebase, or secrets yet.
