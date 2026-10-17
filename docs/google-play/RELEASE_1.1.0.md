# Nota Bene 1.1.0 — build 50 (local test candidate)

Prepared 17 October 2026. Not submitted to Play.

- CALENDAR collection type: make separate Work, Family, Friends or other calendars.
- Date/time pickers, title, details and Never/Daily/Weekly/Monthly/Yearly repeat.
- Upcoming agenda, with past one-off events available through Show Past Events.
- Edit/delete whole events or recurring series; confirmation before deletion.
- Draft survives activity recreation. Date/time is local wall-clock time, not tied to an account or timezone.
- Real month/year arithmetic, anchored to the original date (month-end/leap-day clamping without drift).
- Room 10→11 migration adds fields without dropping existing records.
- XLSX export includes event date/time and recurrence. Existing importer limitation remains: it accepts only the legacy five-sheet format. Configurable collections, including calendars, cannot yet be restored through Import.
- No new permissions, account integration, sharing, alerts, attendee lists or individual-occurrence exceptions.

Device acceptance required: create two calendars, add/edit/delete events, switch moods, rotate with a draft open, restart, check recurrence around month-end and inspect XLSX output. Do not uninstall a personal-data installation to test this build.

Verification: all 46 JVM tests passed; debug APK and Android test APK built; lint passed. No phone is connected, so device tests (including migration/UI checks) were compiled but not executed.
