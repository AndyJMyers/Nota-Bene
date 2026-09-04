# Google Play Data Safety draft

Status: Answers saved in Play Console on 5 September 2026 for the current Alpha 38 app. All ten App content declarations are actioned. Saved changes have not been sent for review. Recheck these answers whenever dependencies or data handling change.

## Preliminary answers

| Play Console question | Draft answer | Basis |
| --- | --- | --- |
| Does the app collect any required user-data types? | **Yes — limited ML Kit diagnostics and usage telemetry only** | Bundled Google ML Kit may transmit device and app information, performance metrics, a per-installation identifier, feature/configuration and event/error metadata. User-entered Nota Bene records, receipt images and recognised receipt text are not transmitted by ML Kit. |
| Does the app share user data? | **No** | No developer backend or advertising; Google's ML Kit disclosure says its diagnostic data is not transferred to third parties. User-directed exports are distinct from SDK telemetry. |
| Is collected data encrypted in transit? | **Yes** | Google documents ML Kit data as encrypted in transit. Nota Bene itself does not transmit its records. |
| Can users request deletion of collected data? | **No** | Do not promise deletion of Google's remotely collected SDK diagnostics. **Erase all local data**, Android clear-storage and uninstall still provide device-side deletion of the user's records; that is not a remote diagnostic-deletion service. No automatic 90-day deletion claim is made. |
| Does the app provide accounts? | **No** | No registration, login or developer backend. |

## Data types to declare for ML Kit

The saved live form contains these three types:

- **App info and performance > Diagnostics** — performance, device/app technical information, configuration, size and error metadata.
- **App activity > App interactions** — SDK feature usage/event telemetry, not note contents or a developer-operated activity tracker.
- **Device or other IDs** — the bundled SDK's per-installation identifier.

All three are marked collected (not shared), non-ephemeral, required (no verified telemetry opt-out), and used for Analytics. No advertising purpose, crash-log collection, location, health records or receipt contents is declared. Technical device metadata is covered by Diagnostics; there is no separate Device information category in this form.

Evidence: bundled `com.google.mlkit:text-recognition:16.0.1`, current release merged manifest (no Advertising ID permission), [ML Kit disclosure](https://developers.google.com/ml-kit/android-data-disclosure), and [Play data definitions](https://support.google.com/googleplay/android-developer/answer/10787469?hl=en-GB), checked 5 September 2026.

These data are encrypted in transit by ML Kit, are not used for advertising or marketing and are not the user’s Nota Bene records. ML Kit’s published disclosure says this data is not transferred to third parties. Do not submit a blanket **No data collected** answer while this dependency is present.

## Personal records stored locally

Nota Bene stores the following only in its local Room database or local preferences:

- financial records: merchant, amount and note;
- health records: medicines, dosage, dose history, stock, symptoms and measurements;
- user content: tasks, dependencies, questions and notes;
- completion, hidden and halted states; and
- associated dates and times.

The developer has no Nota Bene account or server and cannot access these records. Android cloud backup and device-to-device transfer of app data are disabled. A user may explicitly export records as an XLSX workbook through Android’s save picker.

## System-mediated features

### Speech recognition

Nota Bene launches Android’s `RecognizerIntent`. The external speech-recognition activity collects audio directly under its provider's settings and privacy policy. Nota Bene receives returned text and retains only text the user accepts; it neither accesses nor transmits audio. The declaration does not count that external activity as an embedded audio-collection SDK. Reassess if speech is moved into the app or any recording/upload path is added.

### Receipt selection and OCR

The user selects an image through Android’s document picker. Bundled ML Kit recognises text on-device. Nota Bene does not retain the receipt image, and Google states that receipt inputs and recognised text are not sent to its servers. ML Kit may nevertheless send the limited diagnostic data listed above.

### User-directed XLSX export

The user chooses the destination through Android’s save picker. A selected cloud provider may upload and process the workbook under its own terms. This is an explicit, user-directed transfer and is not transmission to Nota Bene’s developer.

## Before submission

- Inspect the final signed App Bundle and merged manifest, not only source files.
- Recheck Google’s current ML Kit Data Safety guidance and all dependency disclosures.
- Test **Erase all local data** and confirm Room data and reminder-state preferences are removed.
- Keep the Play answers, privacy policy, store listing and in-app notice consistent.
- Reassess immediately if cloud sync, crash reporting, analytics, accounts, remote OCR or any other network feature is added.
- Do not claim encrypted local storage or guaranteed reminder delivery; neither is provided.
