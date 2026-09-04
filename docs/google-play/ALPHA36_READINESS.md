# Nota Bene — Alpha 36 Google Play readiness

Reviewed: **4 September 2026**

This is an engineering and publication readiness record, not legal advice. It records the actual Alpha 36 source configuration and the work still needed before a Play submission.

## What is ready

- Application ID: `com.notabene.app`.
- Version: `0.9.0-alpha36` / version code `36`.
- Compile and target SDK: Android 16 / API 36.
- The app source declares `POST_NOTIFICATIONS` and `RECEIVE_BOOT_COMPLETED`. The inspected release merged manifest also contains `INTERNET` and `ACCESS_NETWORK_STATE`, inherited through the bundled ML Kit/data-transport stack, plus an app-private dynamic-receiver permission.
- Android cloud backup and device-to-device transfer are disabled in the manifest and backup-rule resources.
- No Nota Bene account, developer backend, advertising, developer analytics, cloud sync or remote notification service.
- User records are kept in the local Room database; exports occur only through a user-selected Android save destination.
- The `*` Settings sheet contains privacy, export, speech, receipt, reminder, deletion and medical-limit notices.
- The focused JVM suite, debug APK and Android-test APK compile successfully.
- `lint` passes after the Alpha 36 review fixed one misleading MEDS-card indentation issue.
- A release `.aab` was built and its merged manifest inspected. It is intentionally unsigned and therefore not uploadable until the final upload-key/Play App Signing work is done.

## What requires a final decision or action

1. **Public contact and privacy URL.** Choose a public support/privacy email, put it into `PRIVACY_POLICY.md`, publish the policy as a stable, public, non-geofenced HTML page, and link to it from the final app and Play Console.
2. **Final signed bundle.** Create or select the final upload key, configure Play App Signing, build a signed `.aab`, then inspect that exact artefact and its merged manifest. The locally generated Alpha 36 bundle is not signed and must not be uploaded.
3. **Console declarations.** Complete App access, ads, Data Safety, Health Apps, content rating, target audience, category and support-contact questions using the accompanying drafts and the live wording.
4. **Release evidence.** Test a Play-delivered build on real phones: rotation/scrolling, input drafts, receipt OCR, speech recognition, local persistence, repeat MEDS logging, reorder state, notification privacy, delayed/suppressed reminders, export and full erasure.
5. **Listing assets.** Produce the Play icon, feature graphic and screenshots with entirely fictitious payment and medical records. Keep the release copy and the actual app in agreement.

## Data Safety position

Do not declare “no data collected” while bundled ML Kit text recognition is present. Google’s current ML Kit disclosure identifies limited device/app, performance, per-installation identifier, configuration and event/error telemetry for diagnostics and usage analytics. The release merged manifest confirms the bundled ML Kit and Google data-transport components, together with `INTERNET` and `ACCESS_NETWORK_STATE`. Receipt images, recognised text and Nota Bene database records are not transmitted by the bundled text-recognition feature. The precise Console selections remain a final-bundle check.

System speech recognition is deliberately user-invoked. Nota Bene stores only text accepted by the user; the selected device speech service may process audio under its own provider terms and privacy policy.

## Health position

Select the Health Apps declaration accurately. Nota Bene is a local lifestyle recording and organisation app with medication-management and health-observation features. It is not a medical device, does not diagnose, treat, cure or prevent a condition, calculate doses or recommend treatment. Its in-app and listing text must retain these limits and its reminder warning.

## Current blocker summary

There is no source-level policy blocker identified by this pass. Production is blocked only by the deliberate release work: public privacy contact and hosted policy, final signed bundle/Play App Signing, Console declarations, release-assets/screenshots and real-device Play-track testing.
