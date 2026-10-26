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

## Versioning

The plugins use one version name for the app (`versionName`, `BuildConfig.VERSION_NAME`), the Maven
publication, and the Dokka documentation. It is resolved in this order:

1. The `releaseVersion` Gradle property, e.g. `./gradlew dokkaGenerate -PreleaseVersion=2.0.0-beta08`.
2. The `RELEASE_VERSION` environment variable.
3. The latest git tag (`git describe --tags --abbrev=0`). Anything after `%` is ignored.
4. `0.0.0`, if none of the above is available, e.g. in a shallow clone without tags.

Setting the version explicitly allows building a given version without creating a tag first, and
running the tests in a shallow clone (`fetch-depth: 1` in GitHub Actions) without any git history.
A git command that fails is not an error, it is only logged with `--info`.
