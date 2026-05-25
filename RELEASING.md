# Releasing Nordic Gradle Plugins

This repository supports releasing two versions of the plugins and version catalog from the same branch:
1.  **Standard Version**: `minSdk = 23`, modern dependencies (default).
2.  **Legacy Version**: `minSdk = 21`, older dependencies.

## Configuration

The build logic switches between these versions based on the `nordic.legacy` Gradle property.

-   **Standard**: Uses `gradle/libs.versions.toml` and sets `minSdk = 23`.
-   **Legacy**: Uses `gradle/libs-legacy.versions.toml` and sets `minSdk = 21`.

## How to Release

The version name is derived from Git tags. When `nordic.legacy=true` is set, a `-legacy` suffix is automatically appended to the version name.

### 1. Release Standard Version
Run the standard publish command:
```bash
./gradlew publish
```
This will release version e.g. `3.0.0` based on the latest tag.

### 2. Release Legacy Version
Run the publish command with the legacy property:
```bash
./gradlew publish -Pnordic.legacy=true
```
This will release version e.g. `3.0.0-legacy` (using the same tag but with a suffix and legacy configuration).

## Maintaining Dependencies
-   Update standard dependencies in `gradle/libs.versions.toml`.
-   Update legacy dependencies in `gradle/libs-legacy.versions.toml`.

The `AppConst` object is automatically generated during the build process based on the selected flavor.
