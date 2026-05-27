/*
 * Copyright (c) 2022, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

plugins {
    `version-catalog`
    `kotlin-dsl`
    `maven-publish`
    signing
    alias(libs.plugins.publish)
    alias(libs.plugins.ksp)
}
apply(from = "../gradle/git-tag-version.gradle.kts")

val versionNameFromTags: String by extra

group = "no.nordicsemi.gradle"
version = versionNameFromTags

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    // By using `implementation` instead of `compileOnly` we ensure that we won't have to apply
    // them in final projects in the main build.gradle.kts files.
    // I.e. it is enough to add "id("no.nordicsemi.plugin.android.application")"
    // and no need to add "id("com.android.application")".
    implementation(libs.android.gradlePlugin)
    implementation(libs.android.gradleApi)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.ksp.gradlePlugin)
    implementation(libs.hilt.gradlePlugin)
    implementation(libs.compose.gradlePlugin)
    implementation(libs.jetbrains.compose.gradlePlugin)
    implementation(libs.dokka.gradlePlugin)
    implementation(libs.dokka.android.gradlePlugin)
}

gradlePlugin {
    website.set("https://www.nordicsemi.com/")
    vcsUrl.set("https://github.com/nordicsemi/Nordic-Gradle-Plugins")
}

gradlePlugin {
    plugins {
        register("android-application") {
            id = "no.nordicsemi.plugin.android.application"
            displayName = "Android Application Convention Plugin"
            description = "Convention plugin for Android applications."
            implementationClass = "AndroidApplicationConventionPlugin"
            tags.addAll("nordicsemi", "android", "application")
        }
        register("android-library") {
            id = "no.nordicsemi.plugin.android.library"
            displayName = "Android Library Convention Plugin"
            description = "Convention plugin for Android libraries."
            implementationClass = "AndroidLibraryConventionPlugin"
            tags.addAll("nordicsemi", "android", "library")
        }
        register("android-kmp-library") {
            id = "no.nordicsemi.plugin.android.kmp.library"
            displayName = "Android Kotlin Multiplatform Library Convention Plugin"
            description = "Convention plugin for Android Kotlin Multiplatform libraries."
            implementationClass = "AndroidKmpLibraryConventionPlugin"
            tags.addAll("nordicsemi", "android", "kotlin", "kmp", "multiplatform", "library")
        }
        register("kotlin") {
            id = "no.nordicsemi.plugin.kotlin"
            displayName = "Kotlin Convention Plugin"
            description = "Convention plugin configuring Kotlin for Android or KMP modules."
            implementationClass = "KotlinConventionPlugin"
            tags.addAll("nordicsemi", "kotlin")
        }
        register("compose") {
            id = "no.nordicsemi.plugin.feature.compose"
            displayName = "Compose Feature Plugin"
            description = "Convention plugin configuring Jetpack Compose or Compose Multiplatform."
            implementationClass = "ComposeConventionPlugin"
            tags.addAll("nordicsemi", "compose")
        }
        register("hilt") {
            id = "no.nordicsemi.plugin.feature.hilt"
            displayName = "Hilt Feature Plugin"
            description = "Convention plugin configuring Hilt for Android modules."
            implementationClass = "HiltConventionPlugin"
            tags.addAll("nordicsemi", "android", "hilt")
        }
        register("hilt-compose") {
            id = "no.nordicsemi.plugin.feature.hilt.compose"
            displayName = "Hilt Compose Feature Plugin"
            description = "Convention plugin configuring Hilt integration for Compose."
            implementationClass = "HiltComposeConventionPlugin"
            tags.addAll("nordicsemi", "android", "hilt", "compose")
        }
        register("publish-android") {
            id = "no.nordicsemi.plugin.publish.android"
            displayName = "Android Publishing Plugin"
            description = "Convention plugin for publishing Android libraries to Maven Central repositories."
            implementationClass = "PublishAndroidConventionPlugin"
            tags.addAll("nordicsemi", "android", "publish")
        }

        register("publish-jvm") {
            id = "no.nordicsemi.plugin.publish.jvm"
            displayName = "JVM Publishing Plugin"
            description = "Convention plugin for publishing JVM libraries to Maven Central repositories."
            implementationClass = "PublishJvmConventionPlugin"
            tags.addAll("nordicsemi", "jvm", "publish")
        }

        register("publish-kmp") {
            id = "no.nordicsemi.plugin.publish.kmp"
            displayName = "KMP Publishing Plugin"
            description = "Convention plugin for publishing Kotlin Multiplatform libraries to Maven Central repositories."
            implementationClass = "PublishKmpConventionPlugin"
            tags.addAll("nordicsemi", "kotlin", "kmp", "multiplatform", "publish")
        }
        register("dokka") {
            id = "no.nordicsemi.plugin.dokka"
            displayName = "Dokka Convention Plugin"
            description = "Convention plugin configuring Dokka for Nordic projects."
            implementationClass = "NordicDokkaPlugin"
            tags.addAll("nordicsemi", "dokka")
        }
    }
}

ext["signing.keyId"] = System.getenv("GPG_SIGNING_KEY")
ext["signing.password"] = System.getenv("GPG_PASSWORD")
ext["signing.secretKeyRingFile"] = "../sec.gpg"

signing {
    isRequired = System.getenv("GPG_SIGNING_KEY") != null
    sign(publishing.publications)
}

dependencies {
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.moshi)
    ksp(libs.moshi.kotlin.codegen)
}
