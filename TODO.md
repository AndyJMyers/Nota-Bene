# Nota Bene TODO

## Current UI baseline — do not drift

- Nota Bene opens with one **TODO** collection. The `+` control adds named TODO, LOG, RECORD and REPEAT collections.
- Tapping a tab selects it; long-pressing it edits or deletes that collection.
- REPEAT items visibly retain their day/week/month/year interval. `LOG` records an occurrence; tapping an item shows its history; long-pressing it edits the item.
- The header contains the Nota Bene identity, colour slider, `MOOD >`, animation and `*` settings controls.
- Every panel scrolls vertically and remains usable after phone rotation.
- Tapping outside an input dismisses the keyboard.
- Administrative controls, including XLSX import/export and full local erasure, stay under `*` Settings.

## Product-test priorities

- [ ] Test creating, renaming and deleting every collection type on the intended phone.
- [ ] Test typed, speech and image-text capture; confirm captured text remains editable before saving.
- [ ] Test repeat-event logging, visible history, item editing, reminders, notification permission and phone restart behaviour.
- [ ] Test XLSX export for collections, repeat definitions and recorded repeat events in Excel, Google Sheets and LibreOffice.
- [ ] Test import/export and complete local erasure with a real backup-and-restore cycle.
- [ ] Review accessibility on a real phone: contrast, text scaling, TalkBack labels, touch-target size and reduced-motion behaviour.

## Small complete circuits still to add

- [ ] Update the import format for the generic collection workbook.
- [ ] Reconfirm reminder timing and battery-saving behaviour on physical devices.
- [ ] Refine each visual treatment side-by-side on a physical phone while preserving the common operational layout.

## Guardrails

- Personal records remain local unless the user explicitly exports an XLSX workbook.
- Android cloud and device-transfer backup remain disabled unless a later privacy review deliberately changes that decision.
- Reminders are optional aids, not guaranteed delivery and not a substitute for attention.
- Nota Bene offers neutral recording tools. It does not assign meaning to the user’s collections or make recommendations from their contents.
- Any account, cloud sync, analytics, remote processing or external-service integration requires a fresh privacy review before implementation.

## Visual styles

- [x] Add a persistent `MOOD >` next-style control with a generous touch target.
- [x] Add distinct palettes, type character, tab artwork, panel frames and backdrop ornament for Retro Futurist, Steampunk, Ecclesiastic, Cosmic Funk, Orbital Deco, Art Nouveau and William Morris.
- [x] Glow the selected style name into view and fade it away without interrupting the current task.
- [ ] Refine all seven treatments side-by-side on a physical phone, keeping their operational layout and control language consistent.
