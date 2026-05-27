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

import no.nordicsemi.android.buildlogic.getVersionNameFromTags
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.engine.plugins.DokkaHtmlPluginParameters
import java.io.File
import java.util.Calendar
import kotlin.io.path.createTempDirectory

class NordicDokkaPlugin : Plugin<Project> {
    private val org = "Nordic Semiconductor ASA"

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.dokka")
            }

            extensions.configure<DokkaExtension> {
                // Set the version.
                moduleVersion.set(getVersionNameFromTags())
                // Set the output directory for the documentation.
                // GitHub Pages are using "docs" directory.
                basePublicationsDirectory.set(rootDir.resolve("docs"))

                val icon = getResourceAsFile("logo-icon.svg")
                val styles = getResourceAsFile("logo-styles.css")

                // Set the footer message.
                pluginsConfiguration.named("html", DokkaHtmlPluginParameters::class.java) {
                    val year = Calendar.getInstance().get(Calendar.YEAR)
                    footerMessage.set("Copyright © 2022 - $year $org. All Rights Reserved.")
                    customAssets.from(icon.absolutePath)
                    customStyleSheets.from(styles.absolutePath)
                }
            }
        }
    }

    private fun getResourceAsFile(resourceName: String): File {
        val resource = this::class.java.getResourceAsStream("dokka/$resourceName")
            ?: throw IllegalStateException("Resource not found: $resourceName")
        val dirPath = createTempDirectory("dokka")
        val dir = dirPath.toFile()
        val tempFile = File(dir, resourceName)
        dir.deleteOnExit()

        resource.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

}
