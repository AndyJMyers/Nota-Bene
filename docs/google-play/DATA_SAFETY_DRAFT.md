# Google Play Data Safety draft

Status: working answers for the current Alpha 16 source. Recheck every dependency, permission and data flow against the release bundle before submitting the Play Console form.

## Preliminary answers

| Play Console question | Draft answer | Basis |
| --- | --- | --- |
| Does the app collect or share any required user-data types? | **No**, on the present implementation | Deliberately entered records remain in the app’s local database; there is no developer-operated server, advertising or analytics SDK. User-directed export is not developer collection. |
| Is all user data encrypted in transit? | **Not applicable to Nota Bene’s own data transfer** | Nota Bene does not transmit its database. A selected system speech service or export destination operates separately under its own policies. |
| Can users request deletion? | **Yes, without an account request** | There is no account. Users can clear app storage or uninstall; some records also have individual controls. The privacy policy explains this. |
| Does the app provide accounts? | **No** | No registration, login or developer backend. |

## Data handled locally but not transmitted to the developer

The app handles the following categories on-device:

- financial information: payment merchant, amount and note;
- health information: medicines, dosage, dose history, symptoms and measurements;
- user content: tasks, dependencies, questions and notes;
- app activity/status: completion and halted states; and
- timestamps associated with the records.

Under Google Play’s definition, data generally counts as “collected” when it is transmitted off the device to the developer or a third party by the app. Local-only processing should therefore support the preliminary **No data collected or shared** answer. This must be re-evaluated if analytics, crash reporting, cloud sync, network OCR, accounts, reminders using a remote service, or any other SDK is added.

## System-mediated features requiring care

### Speech recognition

Nota Bene launches `RecognizerIntent`. The installed speech-recognition service may send audio to its provider. Nota Bene receives text and does not retain audio. Confirm the Play Console interpretation for the final device/service configuration and disclose the external service clearly in the privacy policy.

### Receipt selection and OCR

The user selects an image through Android’s document picker. ML Kit text recognition runs on the selected image; the app does not keep the receipt image. Confirm from the final dependency documentation that the bundled recognizer performs no operational telemetry that changes the Data Safety answers.

### Android backup

The manifest currently sets `android:allowBackup="true"`. Android may copy the local database into a user-controlled device/account backup. Decide before release whether sensitive health and payment records should be excluded from backup or protected with explicit backup rules. Revisit the declaration and privacy wording after that decision.

### User-directed XLSX export

The user chooses the destination through Android’s save picker. Selecting a cloud storage provider can transfer the workbook to that provider. This is user-directed sharing, not transmission to the Nota Bene developer, but the privacy policy must continue to explain it.

## Before submission

- Inspect the final Android App Bundle and merged manifest, not only the source manifest.
- Review every SDK’s current Data Safety guidance.
- Ensure the Play Console answers and public privacy policy use consistent language.
- Update this document whenever any networked feature or dependency is introduced.
- Do not claim end-to-end encryption or encrypted local storage; neither is presently implemented by Nota Bene itself.

