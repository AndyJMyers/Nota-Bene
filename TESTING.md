# Nota Bene testing

Build 50 adds seven JVM calendar recurrence tests (one-off boundaries, future anchors, daily/weekly progression, month-end, leap-day and old anchors). Android coverage adds isolated calendar CRUD/export and a rotation-safe event draft; the 9→10 migration check now also applies 10→11 and checks preserved data/defaults. Device tests must run only against disposable data.

Build 49 adds six fast display-order tests: unchanged defaults, missing/duplicate IDs, new items, movement boundaries, stale positions and completed-group separation. Phone checks should cover Save versus Cancel, persistence after restart, and the cabinet timestamp updating only after a successful export.

The suite is intentionally small and risk-led. It protects stored data, reminder decisions, import/export integrity, style cycling and the UI paths that have previously proved easy to regress. Some tests still exercise the older five-tab data format because existing exports and database migrations remain relevant.

## Fast suite — run routinely

```powershell
.\gradlew.bat testDebugUnitTest
```

The local JVM tests cover:

- repeat-history local times, future-time rejection and both daylight-saving transitions;
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

- repeat-history backdating, editing, exact-event Undo, deletion of the last occurrence, unchanged stock and entry isolation;
- clearing only the corrected item's reminder markers;
- the repeat-history toggle, rotation, Undo and add/edit/delete history flow;
- deletion of completed tasks without touching open tasks or other collections, including removal of associated event history;
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
- repeat-item Show History button: muted when history is hidden, highlighted with an inverted background when displayed, in light and dark moods; no +/− or on/off recording labels; tap again to hide logs, long-press to edit, and retained expansion after rotation; the separate STOCK − control only reduces stock;
- save-picker destinations; and
- the settings cabinet on narrow phones and across all visual styles;
- empty-state illustrations and the brief completion flourish across light and dark styles, without layout jumps;
- the Android email/share chooser and Play-page link; and
- opening the exported workbook in Excel, Google Sheets and LibreOffice.

When a real defect recurs or proves subtle, add the smallest regression test that reproduces it. Test behaviour and data promises, not implementation trivia.
