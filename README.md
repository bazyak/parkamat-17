# Parkamat 17

[English](README.md) · [Русский](README.ru.md)

An Android app that presents itself as an ordinary unit converter while hiding a full image-steganography tool behind a user-defined secret code

## What it is

Parkamat 17 is a fork that merges two open-source projects:

- **[Steganofy](https://github.com/mstaudt/Steganofy)** — image steganography for Android. Hides arbitrary data (plain text or files) in the least-significant bits of PNG pixels and reveals it back, with optional AES-256 encryption;
- **[UnitConverterUltimate](https://github.com/physphil/UnitConverterUltimate)** — a comprehensive unit converter. The conversion factors and temperature logic are adapted from this project

The goal is plausible deniability: the app looks and behaves exactly like a normal unit converter. Nothing in the UI hints that anything else is there. The steganography side is reachable only through a combination the user chooses themselves

## The secret combination

On first launch the app shows a one-time setup screen where the user picks:

1. A **category** (e.g. Volume);
2. A **source unit** (e.g. Fl. Oz);
3. A **target unit** (e.g. Millilitre);
4. A **secret numeric value**

From then on, entering that exact combination in the converter silently opens the steganography screen instead of showing a result. There is no button, label, or menu entry pointing to it. The setup screen also allows skipping, which disables the secret entirely

The combination is stored locally in SharedPreferences and never leaves the device. Clearing the app's data in system settings resets it and brings the setup screen back

## Why a hide can be refused

The payload goes into the two least-significant bits of every pixel byte, and transparency destroys it. `ARGB_8888` bitmaps are stored premultiplied: Skia multiplies R, G and B by `alpha / 255` when decoding and divides them back when the PNG is written, rounding both times. Below alpha 255 the low bits do not survive that round trip, so a payload only keeps its shape inside fully opaque pixels. A picture with an alpha channel may therefore swallow a short password and choke on a long text, depending on how far the data reaches into the transparent region

Rather than guess, the app verifies every write end to end: once the PNG is produced it is decoded back and the payload is extracted again with the same password. If the result is not byte-identical to the input, nothing is saved and the user is asked for a different image. That check is what makes any picture safe to try — a corrupt file never reaches the disk

## Reading the carrier

The image is read straight from MediaStore rather than through the uri the Photo Picker hands over. The picker uri is backed by the picker's own database, which can serve a snapshot taken before the file was overwritten — so hiding data in a picture and immediately revealing it from the same file used to return the previous contents. `READ_MEDIA_IMAGES` is requested on the first pick; declining it is not fatal, the app falls back to the picker stream

## Language support

English and Russian, selected through the system per-app language setting:
**Settings → Apps → Parkamat 17 → Language**

## Requirements

Android 15 (API 35) or newer. There is no compatibility code for older releases

## Building

```
./gradlew assembleRelease
```

The APK lands in `app/build/outputs/apk/release/` as `Parkamat17-<version>.apk`

**Toolchain**

| | |
|---|---|
| Gradle | 8.7 |
| Android Gradle Plugin | 8.6.1 |
| Kotlin | 1.9.0 |
| Gradle JDK | 17 or 21 |
| Source/target compatibility | 17 |
| compileSdk / targetSdk | 35 |
| minSdk | 35 |

The JDK that runs Gradle and the bytecode level the project compiles to are separate settings. Gradle itself runs fine on either 17 or 21 — set it under *Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK*. The `compileOptions` and `jvmTarget` values are a different matter: both modules (`app` and `lib-steganofy`) have to declare the same one or the build fails with a JVM target mismatch. AGP 8.6.1 tops out at compileSdk 35, so going past Android 15 means upgrading the plugin first

**Versioning**

`versionCode` and `versionName` are generated, not hardcoded. A counter in `app/version.properties` increments on every real build (`assemble`, `bundle` or `install` tasks) and is left untouched by Gradle syncs. The file is local to each machine, so version numbers are not continuous across checkouts

## Credits

- Steganography engine: [mstaudt/Steganofy](https://github.com/mstaudt/Steganofy);
- Conversion data: [physphil/UnitConverterUltimate](https://github.com/physphil/UnitConverterUltimate)
