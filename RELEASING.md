# Releasing Nordic Gradle Plugins

This repository supports releasing multiple versions of the version catalog from the same branch.

## Version Catalogs

The build system automatically detects and configures additional version catalog projects based on the presence of TOML files in the `gradle/` directory.

-   **Standard Catalog**: Uses `gradle/libs.versions.toml`. Published as `no.nordicsemi.gradle:version-catalog`.
-   **SDK-Specific Catalogs**: Files named `gradle/libs.versions.<sdk>.toml` (e.g., `libs.versions.21.toml`) are automatically included as subprojects named `version-catalog-min-sdk-<sdk>`.

## How to Release

### 1. Release All Catalogs
Run the publish command. All detected catalogs will be published as separate artifacts to Maven Central.
```bash
./gradlew :version-catalog:publishToSonatype
```
Wait! The command above only publishes the standard one. To publish all, you can run:
```bash
./gradlew publishToSonatype
```
Or specifically:
```bash
./gradlew :version-catalog:publishToSonatype :version-catalog-min-sdk-21:publishToSonatype
```

### 2. Release Plugins
The plugins rely on a Gradle property to choose which `minSdk` to target in the generated build standards.

**Release Standard Plugins:**
```bash
./gradlew :plugins:publishPlugins
```

**Release Legacy Plugins (targeting minSdk 21):**
```bash
./gradlew :plugins:publishPlugins -Pnordic.legacy=true
```

## How to Consume Version Catalogs

### Using Standard
```kotlin
versionCatalogs {
    create("libs") {
        from("no.nordicsemi.gradle:version-catalog:<version>")
    }
}
```

### Using a Specific SDK Version (e.g., 21)
```kotlin
versionCatalogs {
    create("libs") {
        from("no.nordicsemi.gradle:version-catalog-min-sdk-21:<version>")
    }
}
```

In both cases, your project will have a catalog named `libs`, and the Nordic plugins will automatically read the correct `minSdk` and dependency versions from it.
