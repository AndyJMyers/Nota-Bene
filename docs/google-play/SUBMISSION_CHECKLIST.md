# Nota Bene — Google Play submission checklist

## Blocking product and policy work

- [x] Add the public privacy contact email to `PRIVACY_POLICY.md`: andyjmyers@gmail.com.
- [x] Publish the privacy policy as a stable public HTML page: https://andyjmyers.github.io/Nota-Bene/privacy/
- [x] Add clear privacy and safety disclosure text inside Nota Bene under `*` settings.
- [x] Add an in-app medical disclaimer stating that Nota Bene is not a medical device, does not diagnose or recommend treatment, and is not for emergencies.
- [x] Disable Android cloud backup and device-transfer backup, with explicit exclusion rules for all app-data domains.
- [x] Give every user-created record type a clear deletion route, or ensure the in-app privacy/help screen plainly explains clearing all app data.
- [ ] Test the exported XLSX workbook in Excel, Google Sheets and LibreOffice using realistic fictitious records.

## Play Console declarations

Saved/actioned in Console as at 5 September 2026; not yet sent for review or approved. No App content declarations need attention.

- [x] App access: no login or restricted area.
- [x] Ads: no ads.
- [x] Advertising ID: no; current release merged manifest has no AD_ID permission.
- [x] Data Safety: limited ML Kit diagnostics, app interactions and installation identifiers; exact saved answers in `DATA_SAFETY_DRAFT.md`.
- [x] Health Apps: Medication and Treatment Management; no medical-device intention.
- [x] Content rating questionnaire completed and actioned.
- [x] Financial features and government-app declarations actioned.
- [x] Target audience: adults; no child age groups.
- [ ] Category and tags: start with Lifestyle while completing the Health Apps declaration accurately; reassess after testing.
- [x] Support email: andyjmyers@gmail.com, saved and published.

## Store listing

- [ ] Confirm the final app name (30 characters maximum).
- [ ] Confirm the short description (80 characters maximum).
- [ ] Check the full description against the exact release candidate.
- [ ] Produce a 512 × 512 Play icon.
- [ ] Produce a 1024 × 500 feature graphic.
- [ ] Capture at least two phone screenshots; aim for four or more at 1080 × 1920 portrait.
- [ ] Add concise alt text for every graphic.
- [ ] Use fictitious payment and medical data in every screenshot.

## Release engineering

- [x] Build and inspect a signed Android App Bundle (`.aab`), not the development APK. Alpha 36's signed bundle is at `app/build/outputs/bundle/release/app-release.aab`; its SHA-256 is `0ED5052D540255359F414602ED4EF7C10C6990C340B28B1933E7408E9A4836CE`.
- [ ] Enrol in Play App Signing and upload a current signed `.aab` to the selected testing track.
- [ ] Ensure the application ID and signing key are final before production.
- [ ] Run lint, unit/instrumentation tests and a clean-install/upgrade test on supported Android versions.
- [x] Alpha 36 targets Android 16 / API 36, meeting the current new-app Play requirement as at 4 September 2026. Reconfirm against Play Console on upload.
- [ ] Test receipt selection, speech recognition, local persistence, dose logging and export on the Play-delivered build.
- [ ] Test locked-screen MEDS notification redaction, delayed/suppressed reminder behaviour and complete local erasure on representative devices.
- [ ] Remove or clearly identify development-only behaviour.

## Personal developer account consideration

If the Google Play developer account is a personal account created after 13 November 2023, check the current closed-testing requirements before applying for production access.

Nota Bene's dashboard currently requires 12 opted-in closed testers for 14 continuous days. See `CLOSED_TESTING_PLAN.md` for the verified 32 Visualisations temporary zero-price sale approach. Do not permanently convert Nota Bene to Free.
