<p align="center"><img src="fastlane/metadata/android/en-US/images/icon.png" width="150"></p>
<h1 align="center"><b>Auxio+</b></h1>
<h4 align="center">A simple, rational music player for Android — with a few extras.</h4>
<p align="center">
    <a href="https://www.gnu.org/licenses/gpl-3.0">
        <img src="https://img.shields.io/badge/license-GPL%20v3-2B6DBE.svg?style=flat">
    </a>
    <img alt="Minimum SDK Version" src="https://img.shields.io/badge/API-24%2B-1450A8?style=flat">
</p>

> [!NOTE]
> **This is a fork of [Auxio](https://github.com/OxygenCobalt/Auxio) by
> [Alexander Capehart (OxygenCobalt)](https://github.com/OxygenCobalt).**
> All credit for the player itself goes to the original author. This fork adds a few
> features that are intentionally out of scope upstream (see
> [Why Are These Features Missing?](https://github.com/OxygenCobalt/Auxio/wiki/Why-Are-These-Features-Missing%3F))
> and is regularly synced with the upstream `dev` branch.

## About

Auxio is a local music player with a fast, reliable UI/UX without the many useless features
present in other music players. Built off of modern media playback libraries, Auxio has superior
library support and listening quality compared to other apps that use outdated Android
functionality. In short, **It plays music.**

## What this fork adds

- **Sleep timer** — a timer button on the playback screen next to the equalizer:
  - Radial dial to pick up to an hour, plus a field for any custom duration
  - Remembers the last duration you used
  - Silent notification with a live countdown (lock screen included) and a stop button
  - Can be fully disabled in Settings → Personalize, restoring the stock toolbar

Everything else — features, behavior, look and feel — matches upstream Auxio.

## Screenshots

<p align="center">
    <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/shot0.png" width=250>
    <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/shot1.png" width=250>
    <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/shot2.png" width=250>
    <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/shot3.png" width=250>
    <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/shot4.png" width=250>
    <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/shot5.png" width=250>
</p>

## Features

- Playback based on [Media3 ExoPlayer](https://developer.android.com/guide/topics/media/exoplayer)
- Snappy UI derived from the latest Material Design guidelines
- Opinionated UX that prioritizes ease of use over edge cases
- Customizable behavior
- Support for disc numbers, multiple artists, release types,
precise/original dates, sort tags, and more
- Advanced artist system that unifies artists and album artists
- SD Card-aware folder management
- Reliable playlisting functionality
- Playback state persistence
- Android Auto support
- Automatic gapless playback
- Full ReplayGain support (On MP3, FLAC, OGG, OPUS, and MP4 files)
- External equalizer support (ex. Wavelet)
- Edge-to-edge
- Embedded covers support
- Search functionality
- Headset autoplay
- Stylish widgets that automatically adapt to their size
- Completely private and offline
- No rounded album covers (if you want them)
- Sleep timer *(fork addition)*

## Permissions

- Storage (`READ_MEDIA_AUDIO`, `READ_EXTERNAL_STORAGE`) to read and play your music files
- Services (`FOREGROUND_SERVICE`, `WAKE_LOCK`) to keep the music playing in the background
- Notifications (`POST_NOTIFICATION`) to indicate ongoing playback and music loading, and to
show the sleep timer countdown

## Building

Auxio relies on a patched version of Media3 that enables some extra playback features, alongside taglib for metadata
parsing. This adds some caveats to the build process:
1. `cmake` and `ninja-build` must be installed before building the project.
2. The project uses submodules, so when cloning initially, use `git clone --recurse-submodules` to properly
download the external code.
3. You are **unable** to build this project on windows, as the custom Media3 build runs shell scripts that
will only work on unix-based systems.

### Set up Android Studio

#### Install Android Studio.

```bash
pkg -S android-studio
```

#### Configuring Android Studio:

- Be sure to have NDK tools, version 28.2.13676358. You can search it on Languages & Frameworks > Android SDK.
- Install Java-21 with your system package manager

    ```bash
    sudo pkg -S jdk21-openjdk
    ```
    Additionally: Set java version to jdk21-openjdk

- Run ./gradlew assembleDebug

#### Connecting to your Android Device

You can connect your Mobile Phone through USB to run the app. 

1. **Enable Developer Options on your phone**
   - Go to **Settings > About phone**  
   - Tap **Build number** 7 times until you see *"You are now a developer!"*

2. **Enable USB debugging**
   - Go to **Settings > Developer options**  
   - Turn on **USB debugging**

3. **Connect your phone to the computer**
   - Use a USB cable  
   - On your phone, accept the *Allow USB debugging?* prompt

4. **Verify that your device is detected**
   ```bash
   cd ~/Android/Sdk/platform-tools
   ./adb devices
   ```

Android Studio also offers virtual devices that come with this pre-configured.

#### Install the app on the Android Phone
To install the app on your physical device or emulator, run this command:

```bash
./gradlew installDebug
```

Auxio should now appear in the list of Apps

#### Load music to Auxio (Optional)

You can move files from your pc to your device / emulator to test the music using this command:

```bash
cd ~/Android/Sdk/platform-tools
./adb push ~Music/ /sdcard/Music
```

## Contributing & Issues

- Problems with **fork-specific features** (ex. the sleep timer): open an issue **in this
repository**.
- Problems with the **player itself** (playback, library, UI): report them
[upstream](https://github.com/OxygenCobalt/Auxio/issues) — but please reproduce them on
official Auxio first, so the original author isn't debugging this fork's changes.
- **Translations** for the core app belong to the upstream
[Weblate project](https://hosted.weblate.org/engage/auxio/).

See the [Contribution Guidelines](/.github/CONTRIBUTING.md) for details.

## Supporting the original author

This fork exists thanks to OxygenCobalt's work. If you find the app useful, consider
supporting Auxio's development through
[their GitHub Sponsors page](https://github.com/sponsors/OxygenCobalt).

## License

[![GNU GPLv3 Image](https://www.gnu.org/graphics/gplv3-127x51.png)](http://www.gnu.org/licenses/gpl-3.0.en.html)

Auxio is Free Software: You can use, study share and improve it at your
will. Specifically you can redistribute and/or modify it under the terms of the
[GNU General Public License](https://www.gnu.org/licenses/gpl.html) as
published by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

More information can be found [here](https://github.com/OxygenCobalt/Auxio/wiki/Licenses).
