# Google Play Data Safety draft

Status: working answers for Alpha 26. Recheck the final Android App Bundle, merged manifest, Play Console wording and every dependency immediately before submission.

## Preliminary answers

| Play Console question | Draft answer | Basis |
| --- | --- | --- |
| Does the app collect any required user-data types? | **Yes — limited SDK diagnostics only** | Bundled Google ML Kit may transmit app/device information, performance metrics and a per-installation identifier. User-entered Nota Bene records, receipt images and recognised receipt text are not transmitted by ML Kit. |
| Does the app share user data? | **No, provisionally** | Nota Bene has no developer backend, advertising or analytics service. Google documents ML Kit’s diagnostic data as not shared with third parties. Confirm the final Console treatment of Google acting as the SDK service provider. |
| Is collected data encrypted in transit? | **Yes** | Google documents ML Kit data as encrypted in transit. Nota Bene itself does not transmit its records. |
| Can users request deletion? | **Yes, without an account request** | There is no account or developer-held record store. **Erase all local data**, Android clear-storage and uninstall provide device-side deletion. ML Kit diagnostic retention is governed by Google. |
| Does the app provide accounts? | **No** | No registration, login or developer backend. |

## Data types to declare for ML Kit

Map Google’s final ML Kit SDK disclosure onto the current Play Console choices. The likely declarations are:

- **App info and performance** — app/version information and performance metrics; collected for analytics and diagnostics;
- **Device or other identifiers** — per-installation identifier; collected for analytics and diagnostics; and
- device information where the live form places it, following Google’s current ML Kit guidance.

These data are not used for advertising or marketing and are not the user’s Nota Bene records. Do not submit a blanket **No data collected** answer while this dependency is present.

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

Nota Bene launches Android’s `RecognizerIntent`. The installed speech-recognition service may process audio remotely under its provider’s settings and privacy policy. Nota Bene receives the returned text and retains only text the user accepts; it does not retain audio. Confirm whether the final Play form requires additional disclosure for this user-initiated system service.

### Receipt selection and OCR

The user selects an image through Android’s document picker. Bundled ML Kit recognises text on-device. Nota Bene does not retain the receipt image, and Google states that receipt inputs and recognised text are not sent to its servers. ML Kit may nevertheless send the limited diagnostic data listed above.

### User-directed XLSX export

The user chooses the destination through Android’s save picker. A selected cloud provider may upload and process the workbook under its own terms. This is an explicit, user-directed transfer and is not transmission to Nota Bene’s developer.

## Before submission

- Inspect the final App Bundle and merged manifest, not only source files.
- Recheck Google’s current ML Kit Data Safety guidance and all dependency disclosures.
- Test **Erase all local data** and confirm Room data and reminder-state preferences are removed.
- Keep the Play answers, privacy policy, store listing and in-app notice consistent.
- Reassess immediately if cloud sync, crash reporting, analytics, accounts, remote OCR or any other network feature is added.
- Do not claim encrypted local storage or guaranteed reminder delivery; neither is provided.
