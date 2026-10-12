# Nota Bene 1.0.1 — build 47

Prepared 12 October 2026 following owner approval of the repeat-history interface.

## Release notes

Repeating items now have a clear Show History button: muted when history is hidden and highlighted when displayed. Tap again to hide it. Recording continues unchanged. The stock-decrease control is now clearly labelled.

## Scope and verification

- No database, export format, reminder, permission or privacy changes.
- Existing production application ID and upload signing key retained.
- Regression test added for showing/hiding repeat history and preserving its state across rotation.
- Device tests must use a disposable installation; their setup clears records.
- Passed 29 JVM unit tests, release lint (0 errors, 11 warnings), signed release bundle build and Android test compilation.
- Signed bundle verified with jarsigner. SHA-256: `32BC7CD1C982D6DB121B9AEAF2D9752C64F085AB058A32FBE5B223F746C97402`.
- Owner approved the revised UI. No phone was connected for rerunning instrumentation tests; the new device regression test is compiled, not device-verified in this release.
- Submission outcome will be recorded below when confirmed by Play.

## Follow-up from Play Console

The current 1.0.0 dashboard reports low DEX obfuscation with a February 2027 deadline, plus an edge-to-edge recommendation. These are separate follow-up work, not changes in this small UI release.
