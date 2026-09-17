# Parkamat 17

An Android app that presents itself as an ordinary unit converter while hiding a full image-steganography tool behind a user-defined secret code.

## What it is

Parkamat 17 is a fork that merges two open-source projects:

- **[Steganofy](https://github.com/mstaudt/Steganofy)** — image steganography for Android. Hides arbitrary data (plain text or files) in the least-significant bits of PNG pixels and reveals it back, with optional AES-256 encryption.
- **[UnitConverterUltimate](https://github.com/physphil/UnitConverterUltimate)** — a comprehensive unit converter. The conversion factors and temperature logic are adapted from this project.

The goal is plausible deniability: the app looks and behaves exactly like a normal unit converter. Nothing in the UI hints that anything else is there. The steganography side is reachable only through a combination the user chooses themselves.

## The secret combination

On first launch the app shows a one-time setup screen where the user picks:

1. A **category** (e.g. Volume)
2. A **source unit** (e.g. Fl. Oz)
3. A **target unit** (e.g. Millilitre)
4. A **secret numeric value**

From then on, entering that exact combination in the converter silently opens the steganography screen instead of showing a result. There is no button, label, or menu entry pointing to it. The setup screen also allows skipping, which disables the secret entirely.

The combination is stored locally in SharedPreferences and never leaves the device. Clearing the app's data in system settings resets it and brings the setup screen back.

## Integrity check on hide

Hiding data in an image with large uniform areas — or with an alpha channel — can silently corrupt the payload: `ARGB_8888` bitmaps are premultiplied, so the PNG round-trip destroys the least-significant bits wherever alpha is below 255.

To catch this, the app verifies every write end-to-end: after the PNG is produced it is decoded back and the payload is extracted again with the same password. If the result is not byte-identical to the original input, nothing is saved and the user is asked to pick a different image.

## Language support

English and Russian, selected through the system per-app language setting:
**Settings → Apps → Parkamat 17 → Language** on Android 13+. AppCompat handles older versions.

## Building

```
./gradlew assembleRelease
```

The APK lands in `app/build/outputs/apk/release/` as `Parkamat17-<version>.apk`.

**Toolchain**

| | |
|---|---|
| Gradle | 8.7 |
| Android Gradle Plugin | 8.3.2 |
| Kotlin | 1.9 |
| Gradle JDK | 17 or 21 |
| Source/target compatibility | 17 |
| compileSdk / targetSdk | 34 |
| minSdk | 24 |

The JDK that runs Gradle and the bytecode level the project compiles to are separate settings. Gradle itself runs fine on either 17 or 21 — set it under *Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK*. The `compileOptions` and `jvmTarget` values, however, must stay at 17: AGP 8.3 does not accept a higher target, and both modules (`app` and `lib-steganofy`) have to declare the same one or the build fails with a JVM target mismatch.

**Versioning**

`versionCode` and `versionName` are generated, not hardcoded. A counter in `app/version.properties` increments on every real build (`assemble`, `bundle` or `install` tasks) and is left untouched by Gradle syncs. The file is local to each machine, so version numbers are not continuous across checkouts.

## Credits

- Steganography engine: [mstaudt/Steganofy](https://github.com/mstaudt/Steganofy)
- Conversion data: [physphil/UnitConverterUltimate](https://github.com/physphil/UnitConverterUltimate)
