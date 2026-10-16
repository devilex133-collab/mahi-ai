# Mahi AI (MVP v0.1) — UNBUILT, UNTESTED

This code was written without access to an Android SDK, so it has **not been compiled**.
Expect to fix a few small errors on the first build. Paste them to Claude to fix.

## What works (in code)
Dashboard, chat with Room history, Gemini via secure backend, turn-based voice (STT -> Gemini -> TTS, bn-BD / en-US),
app launcher, YouTube search, command parser + allowlist + confirm card, settings, delete history, unit tests.

## Not built yet
Accessibility automation, coding workspace, memory manager, Firebase Auth (MVP uses a shared app token),
app favorites, Bangla UI strings, encrypted token storage (token is in DataStore, not encrypted).

## 1. Backend (needs Firebase Blaze plan + a Gemini API key)
```
cd backend
npm i -g firebase-tools && firebase login && firebase init   # pick existing project, keep functions/ as is
cd functions && npm install
firebase functions:secrets:set GEMINI_API_KEY
firebase functions:secrets:set APP_TOKEN     # any long random string
npm run deploy
```
Copy the function URL (ends with /api) into the app: Settings -> Backend URL, plus APP_TOKEN. Tap Test connection.

## 2. Build the APK — recommended: AndroidIDE
Open the MahiAI folder in AndroidIDE and run assembleDebug. It bundles JDK + SDK.

## 2b. Build in Termux (fragile)
```
pkg update && pkg install openjdk-17 gradle aapt2 git
export JAVA_HOME=$PREFIX/lib/jvm/java-17-openjdk
cd MahiAI
gradle wrapper --gradle-version 8.7        # creates gradlew + wrapper jar
```
You need an Android SDK (platform 34, build-tools 34.0.0) and a `local.properties` with `sdk.dir=/path/to/sdk`.
Gradle's own aapt2 is x86 and fails on ARM, so add this to `~/.gradle/gradle.properties`:
```
android.aapt2FromMavenOverride=/data/data/com.termux/files/usr/bin/aapt2
```
Then:
```
./gradlew --version
./gradlew assembleDebug
```
APK: `app/build/outputs/apk/debug/app-debug.apk` (copy to /sdcard/Download and install).

## Security
Gemini key lives only in Firebase secrets. Never put it in the app. Backend checks token, model allowlist, size limits, 20 req/min per IP.
