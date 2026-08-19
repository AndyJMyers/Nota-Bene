# Nota Bene

Nota Bene is an offline-first Android personal operations log for payments, medicine, health observations, tasks and research notes.

## Current milestone

The first baseline establishes:

- a native Kotlin/Jetpack Compose application;
- a consistent five-tab interface;
- the retro-futurist instrument-panel visual language;
- a purple–blue–fusion yellow–quinacridone crimson mood control;
- calm star and snow background modes;
- placeholders for typed, voice and photo capture.

The next milestone adds Room persistence and working records, beginning with Payments and Medicine.

## Build

Open the repository in Android Studio, or run:

```powershell
.\gradlew.bat assembleDebug
```

The project targets Android 16 (API 36) and supports Android 8+ (API 26).

## Privacy direction

Personal and medical data will remain on-device by default. Export, backup and any future network features will require deliberate user action.
