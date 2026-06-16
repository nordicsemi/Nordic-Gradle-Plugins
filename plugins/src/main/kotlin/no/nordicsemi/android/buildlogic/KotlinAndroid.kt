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

package no.nordicsemi.android.buildlogic

import com.android.build.api.dsl.CommonExtension
import no.nordicsemi.android.AppConst
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinWithJavaTarget

/**
 * Configure base Kotlin with Android options.
 */
internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension,
) {
    commonExtension.apply {
        compileOptions.apply {
            sourceCompatibility = AppConst.JAVA_SOURCE_VERSION
            targetCompatibility = AppConst.JAVA_TARGET_VERSION
        }

        configureKotlin<KotlinAndroidProjectExtension>()
    }
}

/**
 * Configure base Kotlin options for JVM (non-Android)
 */
internal fun Project.configureKotlinJvm() {
    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = AppConst.JAVA_SOURCE_VERSION
        targetCompatibility = AppConst.JAVA_TARGET_VERSION
    }

    configureKotlin<KotlinJvmProjectExtension>()
}

/**
 * Configure base Kotlin options for KMP
 */
internal fun Project.configureKotlinKmp() {
    // KMP doesn't have a top-level JavaPluginExtension like a pure JVM project,
    // but we still want to ensure Java tasks (if withJava() is used) align versions.
    extensions.findByType<JavaPluginExtension>()?.apply {
        sourceCompatibility = AppConst.JAVA_SOURCE_VERSION
        targetCompatibility = AppConst.JAVA_TARGET_VERSION
    }

    configureKotlin<KotlinMultiplatformExtension>()
}

/**
 * Configure base Kotlin options.
 */
private inline fun <reified T : KotlinBaseExtension> Project.configureKotlin() = configure<T> {
    // Treat all Kotlin warnings as errors (disabled by default)
    // Override by setting warningsAsErrors=true in your ~/.gradle/gradle.properties
    val warningsAsErrors: String? = project.findProperty("warningsAsErrors") as? String

    when (this) {
        is KotlinAndroidProjectExtension -> compilerOptions
        is KotlinJvmProjectExtension -> compilerOptions
        is KotlinMultiplatformExtension -> compilerOptions
        else -> TODO("Unsupported project extension $this ${T::class}")
    }.apply {
        allWarningsAsErrors.set(warningsAsErrors.toBoolean())

        languageVersion.set(AppConst.KOTLIN_VERSION)
        apiVersion.set(AppConst.KOTLIN_VERSION)
        optIn.add("kotlin.RequiresOptIn")
        optIn.add("kotlinx.coroutines.ExperimentalCoroutinesApi")
        optIn.add("kotlinx.coroutines.FlowPreview")
        // https://kotlinlang.org/docs/whatsnew23.html#unused-return-value-checker
        freeCompilerArgs.add("-Xreturn-value-checker=full")

        if (this is KotlinJvmCompilerOptions) {
            jvmTarget.set(AppConst.JVM_TARGET)
        }

        // In KMP, the top-level compilerOptions applies to all targets (iOS, JVM, etc).
        // However, some JVM-specific settings like jvmTarget only exist on JVM/Android targets.
        if (this is KotlinMultiplatformExtension) {
            targets.configureEach {
                if (this is KotlinWithJavaTarget<*, *>) {
                    compilerOptions.jvmTarget.set(AppConst.JVM_TARGET)
                }
            }
        }
    }
}