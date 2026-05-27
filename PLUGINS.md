# Nordic Gradle Plugins

The repo contains plugins which are shared between Nordic's Android and KMP applications.

## Plugins

List of plugins currently available in the repository.

### Base Plugins

1. [no.nordicsemi.plugin.android.application](plugins/src/main/kotlin/AndroidApplicationConventionPlugin.kt)

   This plugin does the following:
   * applies `com.android.application`,
   * defines `compileSdk`, `minSdk` and `targetSdk`,
   * sets `buildConfig`,
   * configures _debug_ and _release_ build types,
   * creates app signing configuration using `../keystore` file.

2. [no.nordicsemi.plugin.android.library](plugins/src/main/kotlin/AndroidLibraryConventionPlugin.kt)

   This plugin does the following:
   * applies `com.android.library`,
   * defines `compileSdk` and `minSdk`,
   * sets `buildConfig`,
   * configures _debug_ and _release_ build types.

3. [no.nordicsemi.plugin.android.kmp.library](plugins/src/main/kotlin/AndroidKmpLibraryConventionPlugin.kt)

   This plugin applies `com.android.kotlin.multiplatform.library` for KMP modules.

### Extension Plugins

1. [no.nordicsemi.plugin.kotlin](plugins/src/main/kotlin/KotlinConventionPlugin.kt)
   
   Configures Kotlin compiler in the module. Automatically detects if it is an Android, KMP, or JVM project.

2. [no.nordicsemi.plugin.feature.compose](plugins/src/main/kotlin/ComposeConventionPlugin.kt)

   Adds Compose support.
   * For Android projects, it adds Jetpack Compose.
   * For KMP projects, it adds JetBrains Compose Multiplatform and enables Android resources.

3. [no.nordicsemi.plugin.feature.hilt](plugins/src/main/kotlin/HiltConventionPlugin.kt)

   Applies `com.google.devtools.ksp` and configures [Hilt](https://dagger.dev/hilt/) for Android modules.

4. [no.nordicsemi.plugin.feature.hilt.compose](plugins/src/main/kotlin/HiltComposeConventionPlugin.kt)

   Adds Hilt ViewModel support for Compose in Android modules.

### Publishing & Documentation

1. [no.nordicsemi.plugin.publish.android](plugins/src/main/kotlin/PublishAndroidConventionPlugin.kt)
   Creates `publish` and `releaseStagingRepositories` tasks for Android projects.

2. [no.nordicsemi.plugin.publish.jvm](plugins/src/main/kotlin/PublishJvmConventionPlugin.kt)
   Creates `publish` and `releaseStagingRepositories` tasks for JVM projects.

3. [no.nordicsemi.plugin.publish.kmp](plugins/src/main/kotlin/PublishKmpConventionPlugin.kt)
   Creates `publish` and `releaseStagingRepositories` tasks for KMP projects.

4. [no.nordicsemi.plugin.dokka](plugins/src/main/kotlin/NordicDokkaPlugin.kt)
   Applies `org.jetbrains.dokka` and applies Nordic styles to Dokka documentation.
