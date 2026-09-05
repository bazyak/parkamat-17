# Parkamat 17

A personal Android app that presents itself as a simple unit converter while hiding a full-featured steganography tool behind a user-defined secret code.

## What it is

Parkamat 17 is a fork that merges two open-source projects:

- **[Steganofy](https://github.com/mstaudt/Steganofy)** — image steganography for Android. Hides arbitrary data (text or files) in the least-significant bits of PNG pixels and can reveal it back. Supports AES-256 encryption.
- **[UnitConverterUltimate](https://github.com/physphil/UnitConverterUltimate)** — a comprehensive unit converter covering length, mass, temperature, speed, area, volume, time, energy, pressure, power, data storage, cooking, and more. The conversion math is adapted from this project.

The goal is plausible deniability: the app looks and works exactly like an ordinary unit converter. The steganography screen is only accessible via a secret combination that the user configures themselves.

## How the secret works

On first launch the app shows a one-time setup screen. The user picks:

1. A **category** (e.g. Volume)
2. A **source unit** (e.g. Fl. Oz)
3. A **target unit** (e.g. Millilitre)
4. A **secret numeric value** (e.g. any number only they know)

From then on, whenever that exact combination is entered in the converter, the app silently opens the steganography screen instead of showing a result. No button, no label, no visual hint exists anywhere in the UI.

The secret combination is stored locally in SharedPreferences and never leaves the device. To reset it, clear the app's data in system settings.

## Language support

The app supports **English** and **Russian**. Language is selected via the system per-app language setting:  
**Settings → Apps → Parkamat 17 → Language** (Android 13+; AppCompat handles older versions automatically).

## Building

Standard Android project. Requires:

- Android Studio Flamingo or newer
- JDK 11
- Android Gradle Plugin 7.x
- Kotlin 1.7.x

```
./gradlew assembleDebug
```

## Credits

- Steganography engine: [mstaudt/Steganofy](https://github.com/mstaudt/Steganofy) (MIT)
- Conversion data: [physphil/UnitConverterUltimate](https://github.com/physphil/UnitConverterUltimate) (Apache 2.0)
