# Nota Bene — Data Safety working draft

This is a preparation note, not a substitute for reviewing the exact release bundle and current SDK disclosures immediately before submission.

## App behaviour to verify

- User-created collections and records remain in the on-device Room database.
- Android cloud backup and device-to-device app-data transfer are disabled.
- XLSX export is initiated by the user through Android’s document picker.
- System speech recognition is launched only when the user chooses LISTEN. Nota Bene does not retain audio.
- Image-text capture is performed with `com.google.mlkit:text-recognition`.
- The app has no account, advertising SDK, analytics SDK, developer-operated server or cloud synchronisation.

## Proposed answers

### Data collection and sharing

Do **not** select a blanket “no data collected” answer without reconfirming the ML Kit SDK disclosure for the exact dependency in the release.

User-created records processed only on the device are not collected under Google Play’s definition of collection. However, Google’s current ML Kit disclosure says its Android SDKs collect limited device information, application information, performance information and identifiers for diagnostics and usage analytics. Its current documentation says this information is encrypted in transit and not shared with third parties.

In Play Console, use the current ML Kit disclosure to select the applicable data categories and purposes for diagnostics / analytics. Do not declare user-entered notes, images or speech audio as sent to the developer when the final build retains the present behaviour.

### Security and deletion

- Encryption in transit: answer only for data that the final SDK disclosure says is transmitted; ML Kit states its diagnostic traffic uses HTTPS.
- Data deletion: user records can be deleted in-app using individual deletion or **Erase all local data**. Exported copies are controlled by the user where saved.
- The app’s local database is not separately encrypted by Nota Bene. Do not claim it is.

## Sources to recheck before submission

- Google Play Data Safety: <https://support.google.com/googleplay/android-developer/answer/10787469>
- ML Kit Android data disclosure: <https://developers.google.com/ml-kit/android-data-disclosure>
- ML Kit terms / on-device processing: <https://developers.google.com/ml-kit/terms>
