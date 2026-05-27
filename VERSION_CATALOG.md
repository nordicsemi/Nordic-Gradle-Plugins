## Version catalog

![Maven Central Version](https://img.shields.io/maven-central/v/no.nordicsemi.gradle/version-catalog)

The repository also contains Gradle Version Catalog with [toml](gradle/libs.versions.toml) file.
The file is automatically used by Gradle to create libs reference in _build.gradle.kts_ files.

### Set up

Include the following code in your _settings.gradle.kts_ file:
```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from("no.nordicsemi.gradle:version-catalog:<version>")
        }
    }
}
```

Dependencies can be later set using `libs` reference in _build.gradle.kts_ files:
```kotlin
plugins {
    alias(libs.plugins.nordic.android.application) apply false
}

dependencies {
    // Example dependency:
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.leakcanary)
}
```
