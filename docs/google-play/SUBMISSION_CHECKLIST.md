# Nota Bene — Google Play submission checklist

## Blocking product and policy work

- [ ] Add the public privacy contact email to `PRIVACY_POLICY.md` (developer name and publication date are present).
- [ ] Publish the privacy policy as a stable public HTML page (not a PDF, not geofenced, no login required).
- [x] Add clear privacy and safety disclosure text inside Nota Bene under `*` settings.
- [x] Add an in-app medical disclaimer stating that Nota Bene is not a medical device, does not diagnose or recommend treatment, and is not for emergencies.
- [x] Disable Android cloud backup and device-transfer backup, with explicit exclusion rules for all app-data domains.
- [x] Give every user-created record type a clear deletion route, or ensure the in-app privacy/help screen plainly explains clearing all app data.
- [ ] Test the exported XLSX workbook in Excel, Google Sheets and LibreOffice using realistic fictitious records.

## Play Console declarations

- [ ] App access: no login or restricted area.
- [ ] Ads: no ads.
- [ ] Data Safety: complete using `DATA_SAFETY_DRAFT.md`, after inspecting the final bundle and SDK disclosures.
- [ ] Health Apps: declare Medication and Treatment Management; assess Healthcare Services and Management using the live form wording.
- [ ] Content rating questionnaire: complete accurately; the app contains no developer-supplied sexual, violent, gambling or drug-use content.
- [ ] Target audience: adults; do not include child age groups.
- [ ] Category and tags: start with Lifestyle while completing the Health Apps declaration accurately; reassess after testing.
- [ ] Provide a support email and, if available, a support website.

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

- [ ] Build and upload a signed Android App Bundle (`.aab`), not the development APK.
- [ ] Enrol in or configure Play App Signing.
- [ ] Ensure the application ID and signing key are final before production.
- [ ] Run lint, unit/instrumentation tests and a clean-install/upgrade test on supported Android versions.
- [ ] Verify the release targets the API level required on the intended submission date.
- [ ] Test receipt selection, speech recognition, local persistence, dose logging and export on the Play-delivered build.
- [ ] Test locked-screen MEDS notification redaction, delayed/suppressed reminder behaviour and complete local erasure on representative devices.
- [ ] Remove or clearly identify development-only behaviour.

## Personal developer account consideration

If the Google Play developer account is a personal account created after 13 November 2023, check the current closed-testing requirements before applying for production access.
