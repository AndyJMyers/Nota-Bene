# Nota Bene 1.0.0 — build 46

First production candidate, prepared 30 September 2026.

## Release notes

Your notes, tasks, records and repeating routines, organised your way.
Choose from eleven visual moods, keep records locally and export or restore XLSX workbooks.
This release includes a clearer settings cabinet, completion flourishes, hide/delete completed items, and repeat-history buttons that visibly open and close your logged events.

## Verification

- Repeat-history button change passed owner testing on the connected Samsung phone.
- Production application ID: `com.andyjmyers.notabene`.
- Version: `1.0.0`, version code `46`.
- Release uses the existing upload signing key and keeps Android backup disabled.
- Passed 29 unit tests, release lint (0 errors; 11 warnings), signed bundle build and Android test APK compilation.
- Passed all 14 Android device tests in the separate capture installation: database, both migration tests, workbook import and UI smoke checks. Repaired stale test setup/selectors; production migrations already included the full upgrade chain.
- Do not run the destructive UI smoke-test setup on the owner's personal-data installation.

## Play preparation

The existing Production draft is named `Nota Bene 1.0.0` and originally contained build 44. It now contains build 46. Refreshed cabinet and repeat screenshots use sample gardening records. Store copy now describes all eleven moods, XLSX import and the latest controls. Publication remains subject to Google Play review.

Bundle SHA-256: `D5DB383CC8D08FE2BFFCB2E2580EE09E457B471029A5FB23FADF11A39442DD10`.

Play validation reports two non-blocking warnings: no deobfuscation mapping (release is not obfuscated), and no native debug symbols for bundled native dependencies. The icon, feature graphic and screenshots containing generated artwork are declared as AI-assisted assets.
