# Migration Guide: 2.x to 3.0

Version 3.0 introduces modular plugin structure, with named plugin IDs and modified functionality.

Also, a new version catalog for applications and libraries targeting Android API 21.

## Version Catalog

The artifact ID of Nordic Gradle Plugins has changed from `no.nordicsemi.android.gradle` to `no.nordicsemi.gradle`.

See: [Maven Central](https://central.sonatype.com/search?q=no.nordicsemi.gradle).

Update the version of the `version-catalog` dependency in your `settings.gradle.kts` file to `3.0`:

```kotlin
versionCatalogs {
    create("libs") {
        // Old:
        // from("no.nordicsemi.android.gradle:version-catalog:2.15")
        
        // New:
        from("no.nordicsemi.gradle:version-catalog:3.0")
    }
}
```

See [libs.versions.toml](https://github.com/nordicsemi/Nordic-Gradle-Plugins/blob/main/gradle/libs.versions.toml).

### Targeting Android SDK 21?

Use `version-catalog-min-sdk-21` instead:
```kotlin
versionCatalogs {
    create("libs") {
        // Old:
        // from("no.nordicsemi.android.gradle:version-catalog:2.11.5")
        
        // New:
        from("no.nordicsemi.gradle:version-catalog-min-sdk-21:3.0")
    }
}
```

All the Android dependencies in this version catalog are frozen in their last version that targeted SDK 21.

Other, non-Android related dependencies will continue to be updated (i.e. Kotlin version, etc.).

See [libs.versions.21.toml](https://github.com/nordicsemi/Nordic-Gradle-Plugins/blob/main/gradle/libs.versions.21.toml).

## Modular Plugin Architecture

Plugins have been split into **Base** plugins (defining the project type) and **Feature** plugins 
(adding optional functionality). 

### 1. Renamed & Unified Plugin IDs

All plugin IDs now follow the `no.nordicsemi.plugin.*` namespace.

| Feature                 | Old ID (2.15)                                                                                           | New ID (3.0)                                                                        |
|:------------------------|:--------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------|
| **Android Application** | `no.nordicsemi.android.plugin.application`                                                              | `no.nordicsemi.plugin.android.application`                                          |
| **Android Library**     | `no.nordicsemi.android.plugin.library`                                                                  | `no.nordicsemi.plugin.android.library`                                              |
| **Android KMP Library** | N/A                                                                                                     | `no.nordicsemi.plugin.android.kmp.library`                                          |
| **KMP Library**         | `org.jetbrains.kotlin.multiplatform`                                                                    | `org.jetbrains.kotlin.multiplatform` (no change)                                    |
| **Kotlin**              | `no.nordicsemi.*.plugin.kotlin`                                                                         | `no.nordicsemi.plugin.kotlin` (single plugin for all)                               |
| **Compose**             | `no.nordicsemi.android.plugin.application.compose`                                                      | `no.nordicsemi.plugin.android.application` + `no.nordicsemi.plugin.feature.compose` |
|                         | `no.nordicsemi.android.plugin.library.compose`                                                          | `no.nordicsemi.plugin.android.library` + `no.nordicsemi.plugin.feature.compose`     |
| **Hilt**                | `no.nordicsemi.android.plugin.hilt`                                                                     | `no.nordicsemi.plugin.feature.hilt`                                                 |
| **Hilt Compose**        | `no.nordicsemi.android.plugin.feature`                                                                  | `no.nordicsemi.plugin.android.library`+ `no.nordicsemi.plugin.feature.hilt.compose` |

### 2. Splitting "All-in-One" Plugins

If you were using bundled plugins like `.compose` or `.feature`, you must now apply the base plugin and the feature plugin separately.

*   **Application with Compose:**
    *   Old: `alias(libs.plugins.nordic.application.compose)`
    *   New: `alias(libs.plugins.nordic.android.application)`, `alias(libs.plugins.nordic.feature.compose)`
*   **Library with Compose:**
    *   Old: `alias(libs.plugins.nordic.library.compose)`
    *   New: `alias(libs.plugins.nordic.android.library)`, `alias(libs.plugins.nordic.feature.compose)`
*   **Feature (Hilt + Compose):**
    *   Old: `alias(libs.plugins.nordic.feature)`
    *   New: `alias(libs.plugins.nordic.android.library)`, `alias(libs.plugins.nordic.feature.hilt.compose)`

### 3. Unified Kotlin Plugin

The separate `kotlin-android`, `kotlin-jvm`, and `kotlin-kmp` plugins have been merged into 
`no.nordicsemi.plugin.kotlin`. It automatically detects your project type.

## Publishing & Documentation

### 1. Extension Renaming
The publishing extension has been renamed for clarity.
*   **Action:** Rename `nordicNexusPublishing` to `nordicPublishing`.

### 2. Publishing Plugins
All publishing plugins have moved from `no.nordicsemi.*.plugin.nexus` to the `no.nordicsemi.plugin.publish.*` namespace:
*   `no.nordicsemi.plugin.publish.android`
*   `no.nordicsemi.plugin.publish.jvm`
*   `no.nordicsemi.plugin.publish.kmp`

## Compose Multiplatform

The `no.nordicsemi.plugin.feature.compose` plugin is now multiplatform-aware. 
*   **Android:** Adds standard Jetpack Compose dependencies.
*   **KMP:** Automatically applies **JetBrains Compose Multiplatform** and configures `commonMain` for cross-platform UI (iOS, etc.) while keeping Android-specific tooling in the Android target.
*   **Stability Config:** You can now add a `compose-stability.conf` file to your root project; the plugin will detect and apply it to all modules automatically.
