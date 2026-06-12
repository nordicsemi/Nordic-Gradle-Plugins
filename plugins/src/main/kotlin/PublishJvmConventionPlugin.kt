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

import no.nordicsemi.android.NordicPublishingExtension
import no.nordicsemi.android.buildlogic.getGitRevision
import no.nordicsemi.android.buildlogic.getVersionNameFromTags
import no.nordicsemi.android.fixSpdx
import no.nordicsemi.android.from
import no.nordicsemi.android.tasks.ReleaseStagingRepositoriesTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownDomainObjectException
import org.gradle.api.logging.LogLevel
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.engine.plugins.DokkaHtmlPluginParameters
import org.spdx.sbom.gradle.SpdxSbomExtension
import java.util.Calendar

class PublishJvmConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.jvm")
                apply("maven-publish")
                apply("signing")
                apply("org.jetbrains.dokka")
                apply("org.spdx.sbom")
            }

            val gitVersion = getVersionNameFromTags()
            val gitRevision = getGitRevision()

            // Default Nordic group.
            group = "no.nordicsemi.kotlin"
            version = gitVersion

            val nordicPublishing = extensions.create("nordicPublishing", NordicPublishingExtension::class.java)
            val library = extensions.getByType<JavaPluginExtension>()
            val signing = extensions.getByType<SigningExtension>()
            val dokka = try {
                extensions.getByType<DokkaExtension>()
            } catch (_: UnknownDomainObjectException) {
                logger.log(
                    LogLevel.WARN,
                    "WARNING: Dokka V2 could not be applied, add \"org.jetbrains.dokka.experimental.gradle.pluginMode=V2Enabled\" to gradle.properties."
                )
                null
            }

            // The signing configuration will be user by signing plugin.
            extra.set("signing.keyId", System.getenv("GPG_SIGNING_KEY"))
            extra.set("signing.password", System.getenv("GPG_PASSWORD"))
            extra.set("signing.secretKeyRingFile", "${project.rootDir.path}/sec.gpg")

            // Create a software component with the release variant.
            library.withSourcesJar()
            // Javadoc fails with Java 17:
            // https://github.com/Kotlin/dokka/issues/2956
            // library.withJavadocJar()

            // Instead, configure Dokka to generate HTML docs for the module.
            dokka?.apply {
                dokkaSourceSets.configureEach {
                    // Enable Android documentation links.
                    enableAndroidDocumentationLink.set(true)
                    // Don't add documentation for internal API, even if it's public.
                    perPackageOption {
                        matchingRegex.set(".*internal.*")
                        suppress.set(true)
                    }
                }
                // Set the version.
                moduleVersion.set(gitVersion)
                // Set the footer message.
                pluginsConfiguration.named("html", DokkaHtmlPluginParameters::class.java) {
                    val year = Calendar.getInstance().get(Calendar.YEAR)
                    footerMessage.set("Copyright © 2022 - $year Nordic Semiconductor ASA. All Rights Reserved.")
                }
                // Create a task to generate HTML docs, it will be added to the Maven publication.
                dokkaPublications.named("html") {
                    tasks.register<Jar>("dokkaHtmlJar").configure {
                        dependsOn(tasks.named("dokkaGenerate"))
                        from(outputDirectory)
                        archiveClassifier.set("javadoc")
                    }
                }
                // Add Dokka dependency to root project.
                rootProject.dependencies {
                    try {
                        add("dokka", this@with)
                    } catch (_: Exception) {
                        logger.log(
                            LogLevel.WARN,
                            "WARNING: Dokka could not be configured for module ':$name', apply dokka plugin (libs.plugins.nordic.dokka) in main build.gradle.kts."
                        )
                    }
                }
            } ?: run {
                logger.error("ERROR: Dokka V2 could not be applied, add \"org.jetbrains.dokka.experimental.gradle.pluginMode=V2Enabled\" to gradle.properties.")
            }

            // TODO Remove afterEvaluate when `artifactId` and `groupId` are converted to lazy properties.
            afterEvaluate {
                // Configure SPDX SBOM generation.
                extensions.configure<SpdxSbomExtension> {
                    targets.register("release") {
                        configurations.set(listOf("runtimeClasspath"))
                        with (nordicPublishing) {
                            scm {
                                uri.set(pomScmUrl)
                                revision.set(gitRevision)
                            }
                            document {
                                // TODO Lazy proprties/
                                name.set("$group:${pomArtifactId.get()}")
                                namespace.set(pomUrl.map { "$it${pomArtifactId.get()}/$version/spdx" })
                                creator.set(pomOrg.map { "Organization: $it" })
                                packageSupplier.set(pomOrg.map { "Organization: $it" })
                            }
                        }
                    }
                }
                val spdxTask = tasks.named("spdxSbomForRelease") {
                    fixSpdx(project.group.toString(), nordicPublishing)
                }

                extensions.configure<PublishingExtension> {
                    repositories {
                        maven {
                            name = "ossrh-staging-api"
                            setUrl("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
                            credentials {
                                username = System.getenv("OSSR_USERNAME")
                                password = System.getenv("OSSR_PASSWORD")
                            }
                        }
                    }
                    publications {
                        val publication = create("library", MavenPublication::class.java) {
                            // Set publication properties.
                            with(nordicPublishing) {
                                // TODO Use artifactId.set(pomArtifactId) when they are converted to Property
                                artifactId = pomArtifactId.get()
                                // TODO same here
                                groupId = pomGroup.getOrElse(group.toString())
                                // TODO same here
                                version = gitVersion
                            }
                            // Set the component to be published.
                            from(components["java"])
                            // Apply POM configuration.
                            pom {
                                from(nordicPublishing)
                                packaging = "jar"
                            }
                            // Add Dokka HTML docs.
                            artifact(tasks.named("dokkaHtmlJar"))

                            // Add SPDX SBOM.
                            artifact(spdxTask) {
                                classifier = "sbom"
                                extension = "json"
                            }
                        }
                        // This task will add *.asc files to the publication for all artifacts.
                        signing.sign(publication)
                    }
                }

                try {
                    rootProject.tasks.register("releaseStagingRepositories", ReleaseStagingRepositoriesTask::class.java)
                } catch (_: Throwable) { }
            }
        }
    }
}
