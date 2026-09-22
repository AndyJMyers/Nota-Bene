# Nota Bene testing

The suite is intentionally small and risk-led. It protects stored data, reminder decisions, import/export integrity, style cycling and the UI paths that have previously proved easy to regress. Some tests still exercise the older five-tab data format because existing exports and database migrations remain relevant.

## Fast suite — run routinely

```powershell
.\gradlew.bat testDebugUnitTest
```

The local JVM tests cover:

- older dose-count colour boundaries;
- stock arithmetic, including exhaustion without negative display;
- receipt merchant/amount extraction;
- strict dose-time parsing;
- due, evening, duplicate and recorded-dose reminder decisions;
- compatibility with older five-sheet XLSX workbooks;
- repeated same-day dose export;
- retention of all record types and completion states; and
- spreadsheet XML escaping and illegal-character removal; and
- cycling every visual style and handling a corrupt saved style.

These tests need no phone or emulator and add nothing to the installed APK. Gradle skips them during an ordinary `assembleDebug`; run them deliberately during development or with the combined release gate below.

## Android regression suite — run before a release or after tricky Android work

Compile it without a device:

```powershell
.\gradlew.bat assembleDebugAndroidTest
```

Run it on a connected phone or emulator:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

The Android tests cover:

- two same-day dose rows in a real Room database;
- persistent stock correction and halted state;
- erasure of all five instruments and dose history;
- erasure of reminder-state preferences;
- persistence of both MEDS reminder-toggle states;
- migration from database version 5 to 6, including preserved history and removal of the one-dose-per-day constraint;
- first-use guidance in the generic TODO collection;
- the `*` settings cabinet, visible export/import controls, field guide and support actions;
- creating a user-named LOG collection; and
- preservation of unfinished TODO text through Android activity recreation (the mechanism used during rotation).

This layer is opt-in because installing and driving an Android test APK is inherently slower than the JVM suite. Merely compiling it catches dependency and source drift without requiring a device.

Do not run `InterfaceSmokeTest` against a personal-data installation: its setup clears the app database. Use a disposable emulator or dedicated test installation. Checking the installed version with `adb shell dumpsys package <application-id>` is read-only; legacy `com.notabene.app`, release `com.andyjmyers.notabene`, and development `com.andyjmyers.notabene.dev` are separate installations with separate records.

## Practical release gate

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug lintDebug
```

Before a public candidate, also run `connectedDebugAndroidTest` and the relevant manual phone checks in [`TODO.md`](TODO.md).

## Deliberately manual

The suite does not pretend to replace judgement on a real phone. These remain manual because they depend heavily on Android, hardware or an external provider:

- speech recognition quality and cancellation;
- OCR accuracy against varied receipt photographs;
- notification delivery under manufacturer battery policies;
- lock-screen presentation under the user’s notification settings;
- keyboard behaviour, rotation and small-screen feel;
- save-picker destinations; and
- the settings cabinet on narrow phones and across all visual styles;
- empty-state illustrations and the brief completion flourish across light and dark styles, without layout jumps;
- the Android email/share chooser and Play-page link; and
- opening the exported workbook in Excel, Google Sheets and LibreOffice.

When a real defect recurs or proves subtle, add the smallest regression test that reproduces it. Test behaviour and data promises, not implementation trivia.
