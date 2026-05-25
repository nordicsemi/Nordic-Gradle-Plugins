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

import com.android.build.api.dsl.ApplicationExtension
import no.nordicsemi.android.AppConst
import no.nordicsemi.android.buildlogic.getVersionCodeFromTags
import no.nordicsemi.android.buildlogic.getVersionNameFromTags
import no.nordicsemi.android.buildlogic.minSdk
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Sets up the `com.android.application` plugin with the default configuration.
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
            }

            extensions.configure<ApplicationExtension> {
                compileSdk {
                    version = release(AppConst.COMPILE_SDK) {
                        minorApiLevel = 1
                    }
                }

                defaultConfig {
                    minSdk = target.minSdk
                    targetSdk = AppConst.TARGET_SDK
                    versionName = target.getVersionNameFromTags()
                    versionCode = target.getVersionCodeFromTags()
                }

                buildFeatures {
                    buildConfig = true
                }

                signingConfigs {
                    create("release") {
                        storeFile = file("../keystore")
                        storePassword = System.getenv("KEYSTORE_PSWD")
                        keyAlias = System.getenv("KEYSTORE_ALIAS")
                        keyPassword = System.getenv("KEYSTORE_KEY_PSWD")
                    }
                }

                buildTypes {
                    getByName("release") {
                        isMinifyEnabled = true
                        isShrinkResources = true
                        signingConfig = signingConfigs.getByName("release")
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            file("proguard-rules.pro")
                        )
                        buildConfigField("String", "VERSION_NAME", "\"${getVersionNameFromTags()}\"")
                        buildConfigField("String", "VERSION_CODE", "\"${getVersionCodeFromTags()}\"")
                    }

                    getByName("debug") {
                        isMinifyEnabled = false
                        buildConfigField("String", "VERSION_NAME", "\"debug\"")
                        buildConfigField("String", "VERSION_CODE", "\"${getVersionCodeFromTags()}\"")
                    }
                }
            }
        }
    }
}
