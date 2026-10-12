# Nota Bene 1.0.2 — build 48 (local test candidate)

Prepared 12 October 2026. Not submitted to Play; 1.0.1 remains the previously submitted release.

## Changes

- LOG briefly offers Recorded / UNDO, removing precisely the occurrence just added. The timeout respects Android accessibility preferences.
- Show History contains ADD PAST EVENT and per-occurrence EDIT / DELETE controls. Deleting an occurrence requires confirmation.
- Date and time pickers use the phone's local zone. Future occurrences and nonexistent spring clock-change times are rejected.
- The latest remaining occurrence determines Last logged and the repeat reminder anchor, even when adding or editing older events.
- A discreet Last logged line remains visible with history closed.
- History changes do not alter stock. The separately labelled stock control remains independent.
- A correction resets only that item's reminder suppression markers.
- No database schema or export-format change.

## Verification

Passed all 33 local unit tests, Android test compilation, debug APK build and lint (no errors). New database and UI regression tests cover corrections, event-specific Undo, item isolation and reminder-marker isolation. No phone/emulator is connected, so device execution and owner testing remain outstanding. Never run the UI tests on a personal-data installation: their setup clears records.

The separate reordering and cabinet-export-date suggestions remain a follow-on increment.
