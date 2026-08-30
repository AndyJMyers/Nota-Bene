# Nota Bene testing

The suite is intentionally small and risk-led. It protects data, repeated MEDS logging, reminder decisions, export integrity and the few UI paths that have previously proved easy to regress. It is not intended to turn a quick personal-app build into an enterprise ceremony.

## Fast suite — run routinely

```powershell
.\gradlew.bat testDebugUnitTest
```

The 25 local JVM tests cover:

- MEDS white/green/amber/red count boundaries;
- stock arithmetic, including exhaustion without negative display;
- receipt merchant/amount extraction;
- strict dose-time parsing;
- due, evening, duplicate and recorded-dose reminder decisions;
- the five gospel XLSX worksheets;
- repeated same-day dose export;
- retention of all record types and completion states; and
- spreadsheet XML escaping and illegal-character removal.

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

The 10 Android tests cover:

- two same-day dose rows in a real Room database;
- persistent stock correction and halted state;
- erasure of all five instruments and dose history;
- erasure of reminder-state preferences;
- persistence of both MEDS reminder-toggle states;
- migration from database version 5 to 6, including preserved history and removal of the one-dose-per-day constraint;
- navigation to all five working instrument panels;
- the `*` Settings boundary and its export/privacy controls;
- two consecutive presses of the live MEDS **LOG TAKEN** control; and
- preservation of unfinished TASK text through Android activity recreation (the mechanism used during rotation).

This layer is opt-in because installing and driving an Android test APK is inherently slower than the JVM suite. Merely compiling it catches dependency and source drift without requiring a device.

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
- opening the exported workbook in Excel, Google Sheets and LibreOffice.

When a real defect recurs or proves subtle, add the smallest regression test that reproduces it. Test behaviour and data promises, not implementation trivia.
