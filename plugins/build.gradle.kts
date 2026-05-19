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

group = "no.nordicsemi.android.gradle"
version = versionNameFromTags

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    // Commented out to make compile on Android Studio Ladybug Patch 2
    // https://github.com/skiptools/skip/issues/161#issuecomment-2203078945
    jvmToolchain(21)
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.dokka.gradlePlugin)
}

gradlePlugin {
    website.set("https://www.nordicsemi.com/")
    vcsUrl.set("https://github.com/NordicSemiconductor/Android-Gradle-Plugins")
}

gradlePlugin {
    plugins {
        register("android.application.compose") {
            id = "no.nordicsemi.android.plugin.application.compose"
            displayName = "Application with Compose"
            description = "Application plugin extension with Compose and Material3 dependencies. This plugin includes 'application' plugin."
            implementationClass = "AndroidApplicationComposeConventionPlugin"
            tags.addAll("nordicsemi", "Android", "application", "compose")
        }
        register("android.application") {
            id = "no.nordicsemi.android.plugin.application"
            displayName = "Standalone Application configuration"
            description = "Application plugin extension."
            implementationClass = "AndroidApplicationConventionPlugin"
            tags.addAll("nordicsemi", "Android", "application")
        }
        register("android.library.compose") {
            id = "no.nordicsemi.android.plugin.library.compose"
            displayName = "Library with Compose"
            description = "Library plugin extension with Compose and Material3 dependencies. This plugin extends 'library' plugin."
            implementationClass = "AndroidLibraryComposeConventionPlugin"
            tags.addAll("nordicsemi", "Android", "library", "compose")
        }
        register("android.library") {
            id = "no.nordicsemi.android.plugin.library"
            displayName = "Standalone library configuration"
            description = "Library plugin extension."
            implementationClass = "AndroidLibraryConventionPlugin"
            tags.addAll("nordicsemi", "Android", "library")
        }
        register("android.feature") {
            id = "no.nordicsemi.android.plugin.feature"
            displayName = "Feature plugin"
            description = "UI feature plugin with Hilt & Compose. This plugin extends 'library.compose' and 'hilt' plugins and adds Compose navigation."
            implementationClass = "AndroidFeatureConventionPlugin"
            tags.addAll("nordicsemi", "Android", "feature")
        }
        register("android.hilt") {
            id = "no.nordicsemi.android.plugin.hilt"
            displayName = "Hilt plugin"
            description = "Plugin enabling Hilt"
            implementationClass = "AndroidHiltConventionPlugin"
            tags.addAll("nordicsemi", "Android", "hilt")
        }
        register("android.kotlin") {
            id = "no.nordicsemi.android.plugin.kotlin"
            displayName = "Kotlin plugin for Android modules"
            description = "Plugin enabling Kotlin for Android modules."
            implementationClass = "AndroidKotlinConventionPlugin"
            tags.addAll("nordicsemi", "Android", "kotlin")
        }
        register("android.kmp") {
            id = "no.nordicsemi.android.plugin.kotlin.kmp"
            displayName = "Kotlin plugin for Android modules"
            description = "Plugin enabling Kotlin for Android modules."
            implementationClass = "KmpAndroidSdkPlugin"
            tags.addAll("nordicsemi", "Android", "kotlin")
        }
        register("jvm.kotlin") {
            id = "no.nordicsemi.jvm.plugin.kotlin"
            displayName = "Kotlin plugin for JVM projects"
            description = "Plugin setting up Kotlin for JVM modules."
            implementationClass = "JvmKotlinConventionPlugin"
            tags.addAll("nordicsemi", "jvm", "kotlin")
        }
        register("kmp.kotlin") {
            id = "no.nordicsemi.kmp.plugin.kotlin"
            displayName = "Kotlin plugin for KMP projects"
            description = "Plugin setting up Kotlin for KMP modules."
            implementationClass = "KmpKotlinConventionPlugin"
            tags.addAll("nordicsemi", "kmp", "kotlin", "multiplatform")
        }
        register("android.nexus") {
            id = "no.nordicsemi.android.plugin.nexus"
            displayName = "Nexus plugin for Android projects"
            description =
                "Plugin creating a task for publishing Android libraries to Nexus repository."
            implementationClass = "AndroidNexusRepositoryPlugin"
            tags.addAll("nordicsemi", "Android", "nexus", "publish")
        }
        register("jvm.nexus") {
            id = "no.nordicsemi.jvm.plugin.nexus"
            displayName = "Nexus plugin for JVM projects"
            description = "Plugin creating a task for publishing JVM libraries to Nexus repository."
            implementationClass = "JvmNexusRepositoryPlugin"
            tags.addAll("nordicsemi", "jvm", "kotlin", "nexus", "publish")
        }
        register("kmp.nexus") {
            id = "no.nordicsemi.kmp.plugin.nexus"
            displayName = "Nexus plugin for KMP projects"
            description = "Plugin creating a task for publishing KMP libraries to Nexus repository."
            implementationClass = "KmpNexusRepositoryPlugin"
            tags.addAll("nordicsemi", "kmp", "kotlin", "multiplatform", "nexus", "publish")
        }
        register("nordic.dokka") {
            id = "no.nordicsemi.plugin.dokka"
            displayName = "Nordic Dokka plugin"
            description = "Plugin configuring Dokka for Nordic projects."
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
