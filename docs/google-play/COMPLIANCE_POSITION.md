# Nota Bene compliance position

Status: engineering and publication record for Alpha 25; review again before Google Play submission and whenever the app’s purpose or data flows change. This is a practical compliance position, not legal advice.

## Intended purpose

Nota Bene is a lifestyle recording and organisation tool. It lets an adult manually record payments, medicine schedules and doses taken, medicine stock, health observations, tasks and research notes. It displays administrative states derived directly from that user-entered information and provides local reminders.

It does not diagnose, monitor or predict a disease; interpret measurements clinically; calculate or recommend doses; recommend treatment; connect to a medical device; provide emergency care; or send records to clinicians. On that intended purpose, it is not presented as medical-device software. Any future clinical inference, dose calculation, treatment recommendation or medical-device connection requires a fresh regulatory assessment before implementation or publication.

## Data-flow record

| Feature | Data and destination |
| --- | --- |
| SPEND, MEDS, SOMA, TASK and ASK | Stored in a Room database on the user’s phone; no developer server or account |
| Reminder state | Stored in local preferences; reminder checks and notifications are local |
| Android backup/migration | Disabled, with all app-data domains also excluded by backup rules |
| XLSX export | Created only on request; saved to a destination explicitly selected by the user |
| Receipt OCR | Image/text processed on-device; receipt image not retained; ML Kit may send limited app/device/performance/installation diagnostics to Google |
| Speech | Audio handled by the installed system recognition provider when the user invokes it; Nota Bene stores only accepted returned text |
| Ads, developer analytics, accounts, cloud sync | None |

## UK GDPR position

The app can contain health and financial information, including special-category health data. Those personal records remain under the user’s control on their own device. The developer neither determines a server-side processing operation for those records nor receives them. Personal use by the user is ordinarily distinct from developer processing and may fall within the personal or household context.

This does not justify a blanket statement that the app has no external data flow. ML Kit diagnostics and any information voluntarily supplied for support must be treated separately and disclosed accurately. If Nota Bene later adds accounts, cloud sync, developer analytics, crash reports containing user content, clinician sharing or remote processing, the controller/processor roles, lawful basis, transparency, retention, security and data-subject-rights arrangements must be assessed before release.

## Privacy and security choices

- No developer account, backend, advertising or behavioural analytics.
- Cloud backup and device-transfer backup are disabled to reduce unintended copies; users accept that unexported records can be lost with the phone.
- Export is deliberate, uses an ordinary XLSX format and leaves destination control with the user.
- Detailed medicine notifications are marked private and have a generic locked-screen version. The phone’s own screen lock is the minimal authentication boundary.
- **Erase all local data** clears the database, reminder-state preferences and displayed notifications. Exported files remain the user’s responsibility.
- The in-app Settings notice describes storage, export, SDK diagnostics, speech, reminder limits, notification privacy, deletion and medical limitations.

## Reminder safety position

The reminder mechanism uses Android inexact alarms and notifications. The operating system can delay or suppress them. Store copy and in-app text must consistently describe reminders as a helpful aid, never as guaranteed delivery or a substitute for prescriptions, attention, carers or clinical advice.

## Review triggers

Reopen this assessment before adding any of the following:

- cloud storage, accounts, sync, backup or remote notifications;
- crash reporting, analytics, advertising or a new third-party SDK;
- remote OCR, conversational AI or server-side speech controlled by the app;
- clinical interpretation, risk scores, alerts based on measurements, dose calculations or treatment advice;
- clinician, carer or family sharing;
- Health Connect, wearable or medical-device integrations; or
- public claims that the app manages, prevents, diagnoses or treats a condition.
