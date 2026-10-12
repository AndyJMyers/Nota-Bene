# Nota Bene 1.0.2 — build 49 (local test candidate)

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

Build 49 passed all 39 local unit tests, Android test compilation, debug APK build and lint (no errors). New database and UI regression tests cover corrections, event-specific Undo, item isolation and reminder-marker isolation. No phone is connected, so device execution and owner testing remain outstanding. Never run the UI tests on a personal-data installation: their setup clears records.

Build 49 completes the follow-on increment: EARLIER / LATER in collection and item edit dialogs, applied on SAVE; completed items remain below unfinished ones. Display order persists on this installation and is not included in XLSX exports. New items follow explicitly ordered items; otherwise existing date order is unchanged.

The cabinet shows the last successful export date recorded on this installation. Cancelled or failed exports leave it unchanged; it does not guarantee that the exported file is still available. Six additional unit tests cover ordering and edge cases.

Phone acceptance: move tabs/items, cancel an edit, save another and restart. Check both ends of the list and completed items. Export successfully, then cancel another export and check that the timestamp remains unchanged.
