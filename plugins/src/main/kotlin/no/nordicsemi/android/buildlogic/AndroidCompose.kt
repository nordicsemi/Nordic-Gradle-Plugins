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

@file:Suppress("UnstableApiUsage")

package no.nordicsemi.android.buildlogic

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.compose.compose
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Configure Compose-specific options for Android (Application or Library)
 */
internal fun Project.configureAndroidCompose(
    commonExtension: CommonExtension,
) {
    commonExtension.apply {
        buildFeatures.apply {
            compose = true
        }

        dependencies {
            val bom = libs.findLibrary("androidx-compose-bom").get()
            add("implementation", platform(bom))
            add("androidTestImplementation", platform(bom))
            // Add UI Tooling and Previews
            add("implementation", libs.findLibrary("androidx.compose.ui.tooling.preview").get())
            add("debugImplementation", libs.findLibrary("androidx.compose.ui.tooling").get())
            // Add Material 3 Compose
            add("implementation", libs.findLibrary("androidx.compose.material3").get())
        }
    }
}

/**
 * Configure Compose Multiplatform for all targets (Android, iOS, etc.)
 */
internal fun Project.configureComposeMultiplatform(
    extension: KotlinMultiplatformExtension,
) {
    pluginManager.apply("org.jetbrains.compose")

    extension.sourceSets.getByName("commonMain").dependencies {
        implementation(compose("org.jetbrains.compose.runtime:runtime"))
        implementation(compose("org.jetbrains.compose.foundation:foundation"))
        implementation(compose("org.jetbrains.compose.material3:material3"))
        implementation(compose("org.jetbrains.compose.ui:ui"))
        implementation(compose("org.jetbrains.compose.components:components-resources"))
        implementation(compose("org.jetbrains.compose.ui:ui-tooling-preview"))
    }
}

/**
 * Configure Android-specific options for Compose in a KMP module
 */
internal fun Project.configureAndroidKmpCompose(
    extension: KotlinMultiplatformAndroidLibraryExtension,
) {
    extension.apply {
        androidResources {
            enable = true
        }
    }

    dependencies {
        val bom = libs.findLibrary("androidx-compose-bom").get()
        // Adding the BOM to Android target source sets to align transitive androidx dependencies
        add("androidMainImplementation", platform(bom))
        add("androidDeviceTestImplementation", platform(bom))
        add("androidHostTestImplementation", platform(bom))

        // Add UI Tooling for Android
        add("androidMainImplementation", libs.findLibrary("androidx.compose.ui.tooling.preview").get())
        add("androidRuntimeClasspath", libs.findLibrary("androidx.compose.ui.tooling").get())
    }
}
