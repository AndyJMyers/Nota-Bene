<div align="center">

![Nota Bene — Personal Operations Log](docs/readme-header.svg)

<p align="center">
  <img src="docs/assets/nb-fountain-icon-512.png" alt="Nota Bene fountain pen icon" width="160" />
</p>

### A small personal app for recording the things worth remembering.

[![Android](https://img.shields.io/badge/Android-8%2B-164b89?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-Compose-321052?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Status](https://img.shields.io/badge/status-Alpha_26-9d174d?style=for-the-badge)](#current-milestone)
[![Storage](https://img.shields.io/badge/storage-local_first-f2c94c?style=for-the-badge)](#data)

Development builds are released periodically while the configurable collection model is tested on real phones. Settings shows the installed version, build number and Development/Release edition directly beneath its title. Older and development installations can coexist and have separate local records; export before removing an installation.

> **Enter it. Keep it. Show it back. Remind me when necessary. Export it when asked.**

</div>

<p align="center">
  <img src="docs/assets/nota-bene-collections-hero-v2.png" alt="Nota Bene as a celestial black-and-gold cabinet for personal collections" width="720" />
</p>

Nota Bene is being built first and foremost as a personal utility: fast to use, locally persistent, deliberately simple, and designed around information that is easy to forget but useful to have recorded.

## Current scope

Nota Bene is a small local-first organiser made of user-named collections. It opens with one **TODO** collection; the `+` control adds as many others as are useful.

| Collection type | Purpose |
| :--- | :--- |
| **TODO** | A lightweight checklist with completion state and optional follow-on detail |
| **LOG** | A dated stream of notes, observations and other things worth retaining |
| **RECORD** | A dated item with an optional value or reference |
| **REPEAT** | An item to complete again, with an optional repeat interval, quantity and restock point |

The application does not assign a category or meaning to what a person writes. A collection might be household jobs, books, work expenses, research, supplies or anything else the owner finds useful. The contents remain local and private.

## Data

Anything deliberately entered or accepted by the user is treated as the recorded truth until the user changes it.

Primary data is stored locally in a Room database and survives application closure and phone restart. The expected data volume is small.

The `*` settings control contains the export action. It opens Android's standard save picker and creates a dated XLSX workbook with one worksheet for each user-created collection. Completed records and their stored details are included.

> Personal records stay on-device and Android backup is disabled. They leave only when the user explicitly exports an XLSX workbook.

## Design

The complete interface can be cycled through seven persistent visual treatments using the prominent `MOOD >` control: Retro Futurist, Steampunk, Ecclesiastic, Cosmic Funk, Orbital Deco, Art Nouveau and William Morris. Each treatment has its own palette, type character, instrument contours, panel ornament and atmospheric backdrop while preserving the same controls and workflows. Changing style never changes the layout, behaviour or stored records. The newly selected style name glows briefly into view and then fades away.

Art Nouveau surrounds dark peacock enamel controls and warm ivory working panels with a hand-finished ginkgo-and-whiplash frame.

William Morris is the second completed artwork treatment and deliberately uses the full force of the late textiles: a dense indigo field, large winding foliage, madder flowers, strawberries, thrushes and concealed medieval beasts surround warm book-paper work panels. The pattern remains atmospheric and never carries operational text.

Cosmic Funk is the third completed artwork treatment: lacquer-black working surfaces, polished chrome piping, signal-red, cobalt and solar-gold orbital ribbons, starbursts and distant planets give the app the confidence of a 1970s hi-fi control deck while leaving its operational centre calm and legible.

Ecclesiastic is the fourth completed artwork treatment: midnight cathedral blue, Gothic vaults, aged-gold manuscript tracery, ruby-and-cobalt glass and small pools of candlelight surround warm ivory working panels. It suggests an instrument made for a technologically advanced monastery, without turning the app into a sermon.

Orbital Deco is the fifth completed artwork treatment: black enamel, champagne-gold geometry, silver moons, crimson signal jewels and quiet star maps frame ivory working panels. It is the observation lounge of a luxury interplanetary liner, made functional.

Steampunk is the sixth completed artwork treatment: oil-black iron, aged brass, copper joints, careful gauge work and warm filament lamps form a practical alternate-future cabinet around parchment working panels. It is deliberately engineered rather than whimsical.

Retro Futurist is the seventh completed artwork treatment and Nota Bene's home ground: smoked-violet instrument glass, brushed gunmetal, deep blue and quinacridone-crimson stained light, plus glowing amber filaments turn the original industrial-radio idea into a finished machine.

Nota Bene uses the same interface language throughout. Its user-created collection tabs are large industrial radio-style controls presented on one scrolling line:

<div align="center">

`TODO` · `RECORDS` · `REPEATS` · `PROJECTS` · `+`

</div>

The selected control appears illuminated from behind, like a filament bulb glowing through slightly grimy stained glass; unselected controls remain dimmed. Selecting a tab fades its title into view.

The visual character is **retro-futurist rather than steampunk**: functional machinery from an imagined future, not decorative Victorian engineering.

A mood control moves through **dark purple · blue · yellow fusion · quinacridone crimson**. It also governs the pace and intensity of four atmospheric backgrounds:

- a twinkling night sky with a distant supernova and occasional comets;
- wind-drifted falling snow;
- slow-moving oil colour;
- a layered maritime scene with travelling breakers, spray, shore wash and a tiny square-rigger.

A single control cycles forward through the effects. Nota Bene is a utility, not a visualisation application.

## Current milestone

**Generic collections development build** provides:

- a native Kotlin and Jetpack Compose application;
- a configurable collection shell and instrument-panel visual language;
- the continuous four-colour mood control and four animated backgrounds;
- local persistence through Room;
- a collection-based XLSX export through Android's save picker;
- tap-away keyboard dismissal throughout the app;
- the new N.B. fountain-pen identity in the launcher, app header and project page;
- reliable sensor-driven rotation with system-bar-safe content;
- continuous vertical scrolling through every instrument panel;
- disabled cloud and device-transfer backup;
- complete local erasure of collections and records; and
- one compact `*` settings sheet for XLSX import, export and a plain-English privacy notice.

Every collection supports typed entry, system speech recognition and image-text capture; captured material remains editable before it is kept. A collection can be used to monitor anything from milk for kittens to a prescription for a problem panda—the app provides neutral tools, not a predefined category or recommendation.

Near-term work is practical rather than expansive: test image extraction, data migration, complete erasure and exports on real devices; then recapture the store material around the configurable interface. The maintained UI baseline and product-test priorities are in the [TODO](TODO.md).

## Testing

Nota Bene has a focused regression suite rather than a large testing framework. It is being updated alongside the new collection model; ordinary APK assembly does not run or package either suite.

Run the practical local gate with:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug lintDebug
```

The Android suite is compiled separately and run on a connected phone or emulator before releases or after tricky platform work. Commands, scope and deliberate manual checks are documented in [TESTING.md](TESTING.md).

## Build

Open the repository in Android Studio, or run on Windows:

```powershell
.\gradlew.bat assembleDebug
```

The project targets Android 16 (API 36) and supports Android 8+ (API 26).

## Google Play preparation

Publication material will be recreated from the final generic-collections build. The product boundary and local-first data position are in the [generic collections note](docs/google-play/GENERIC_COLLECTIONS_POSITION.md). The public [Privacy Policy](https://andyjmyers.github.io/Nota-Bene/privacy/) is hosted as a static GitHub Pages document.

These remain working drafts until the release candidate and final Play Console review are complete.

## Development approach

Nota Bene is initially being developed for its creator's own regular use. Features will be added or removed according to whether they prove useful in practice. Only if it becomes a genuinely useful daily companion will a wider release be considered.

The priority is not feature count. It is making a small application pleasant enough, fast enough and useful enough to keep opening.

The working method developed through 32 Visualisations and Nota Bene is recorded in [The Human–AI Android Manifesto](docs/HUMAN_AI_ANDROID_MANIFESTO.md).

## Repository policy

This repository is publicly visible so builds can be downloaded and verified. Nota Bene remains a privately directed personal project: external pull requests are not accepted, and publication here is not an invitation to contribute code.
