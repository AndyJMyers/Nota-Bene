# Nota Bene TODO

Updated for **Alpha 35**.

## Current UI baseline — do not drift

- The top-level instruments are, in this order: **SPEND · MEDS · SOMA · TASK · ASK**.
- The five large industrial radio controls remain on one line and use the same interface language throughout.
- The header contains the Nota Bene identity and unlabeled colour slider, with adjacent, visibly framed `MOOD >`, animation and `*` settings buttons on the right.
- Mood and background animation remain atmospheric but clearly visible; the current backgrounds are sky, snow, oil and waves.
- Every instrument panel scrolls vertically and remains usable after phone rotation.
- Tapping outside an input dismisses the keyboard.
- Administrative controls stay one level down under `*` Settings. Do not add export, compliance or configuration clutter to the main instrument row.

## Product-test priorities

- [ ] Test all five instruments repeatedly on the intended phone in portrait and landscape, including small-screen scrolling and keyboard dismissal.
- [ ] Test SPEND speech capture and receipt selection with varied real receipts; confirm every proposed field remains editable before saving.
- [ ] Test the MEDS reminder on/off switch across app closure, phone restart, battery-saving modes and Android notification permission changes.
- [ ] Confirm the locked-screen MEDS notification is generic and medicine details appear only after the phone is unlocked, subject to Android settings.
- [ ] Test repeated same-day logging, the white/green/amber/red `today/usual` indicator, stock correction, reorder state, halted entries and expandable history with realistic fictitious records.
- [ ] Verify **Erase all local data** removes SPEND, MEDS, SOMA, TASK and ASK records plus MEDS reminder state and visible notifications.
- [ ] Open exported five-sheet XLSX files in Excel, Google Sheets and LibreOffice and compare every record type and dose history with the app.

## Small complete circuits still to add

- [ ] ASK: add follow-on notes to an item.
- [ ] ASK: add deliberate item deletion with a suitable confirmation or undo path.
- [ ] TASK: decide whether individual deletion is needed after real use; retain the present done/hide-completed workflow unless deletion proves useful.
- [ ] Review accessibility on a real phone: TalkBack labels, contrast, text scaling, touch-target size and reduced-motion behaviour.

## Google Play preparation

- [ ] Add a public privacy contact email.
- [ ] Publish the privacy policy as stable public HTML and make it reachable from the app and Play listing.
- [ ] Recheck the final App Bundle, merged manifest and every SDK against the Data Safety declaration.
- [ ] Complete the Health Apps declaration while keeping the intended purpose firmly in lifestyle recording and administrative reminders.
- [ ] Produce final Play icon, feature graphic and screenshots using fictitious records.
- [ ] Build and test a signed App Bundle through the required Play testing track.

The detailed publication checklist remains in [`docs/google-play/SUBMISSION_CHECKLIST.md`](docs/google-play/SUBMISSION_CHECKLIST.md). Compliance assumptions and review triggers are in [`docs/google-play/COMPLIANCE_POSITION.md`](docs/google-play/COMPLIANCE_POSITION.md).

## Guardrails

- Personal records remain local unless the user explicitly exports them.
- Android cloud and device-transfer backup remain disabled unless a later privacy review deliberately changes that decision.
- Reminders are helpful aids, never guaranteed delivery or a substitute for attention, prescriptions or professional advice.
- Nota Bene does not diagnose, interpret measurements clinically, calculate doses or recommend treatment.
- Any account, cloud sync, analytics, remote processing, clinician sharing or clinical inference requires a fresh privacy and regulatory review before implementation.
# Visual styles

- [x] Add a persistent `MOOD >` next-style control with a generous touch target.
- [x] Add distinct palettes, type character, tab artwork, panel frames and backdrop ornament for Retro Futurist, Steampunk, Ecclesiastic, Cosmic Funk, Orbital Deco, Art Nouveau and William Morris.
- [x] Glow the selected style name into view and fade it away without interrupting the current task.
- [ ] Refine each treatment on a physical phone after side-by-side use.
- [x] Complete the first production artwork pass for Art Nouveau, including the transparent ginkgo frame and light enamel work surfaces.
- [x] Complete the full-blooded William Morris treatment with saturated textile field, illustrated border and light book-paper work surfaces.
- [x] Complete the Cosmic Funk treatment with lacquer black, chrome, coloured orbital ribbons and a restrained starfield.
- [x] Complete the Ecclesiastic treatment with midnight-blue Gothic architecture, gold tracery, jewel glass and warm ivory work surfaces.
- [x] Complete the Orbital Deco treatment with black enamel, champagne-gold geometry, crimson signal jewels and celestial navigation detail.
- [ ] Bring Steampunk to the same artwork standard.
