# GalaxyEnabler

Root-powered Galaxy AI, One UI feature flag, and AICore enabler app built with Jetpack Compose.

## Requirements

- Android 16+ (`API 36+`)
- Root access (`su` via Magisk, KernelSU, APatch, or Superuser) for applying system patches

## Building from Source

### macOS / Linux / Git Bash

```bash
bash android/build_apk.sh
```

### Windows

```bat
android\build_apk.bat
```

### Direct Gradle Wrapper

```bash
cd android
./gradlew :app:assembleRelease
```

The signed universal APK is written to `dist/GalaxyEnabler.apk`.
