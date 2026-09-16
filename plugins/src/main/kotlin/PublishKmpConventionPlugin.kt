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
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.engine.plugins.DokkaHtmlPluginParameters
import org.spdx.sbom.gradle.SpdxSbomExtension
import java.util.Calendar

class PublishKmpConventionPlugin : Plugin<Project> {
    private val isSigningEnabled: Boolean
        get() = System.getenv("GPG_SIGNING_KEY") != null

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                // This is surely a Kotlin module, so make sure the Kotlin version is aligned.
                apply(KotlinConventionPlugin::class.java)

                apply("org.jetbrains.kotlin.multiplatform")
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

            // The signing configuration will be used by signing plugin.
            extra.set("signing.keyId", System.getenv("GPG_SIGNING_KEY"))
            extra.set("signing.password", System.getenv("GPG_PASSWORD"))
            extra.set("signing.secretKeyRingFile", "${project.rootDir.path}/sec.gpg")

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
                moduleVersion.set(target.provider { nordicPublishing.pomVersionName.orNull ?: gitVersion })
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

            afterEvaluate {
                val effectiveVersion = nordicPublishing.pomVersionName.orNull ?: gitVersion
                version = effectiveVersion

                // Configure SPDX SBOM generation, resolved from the jvm target's runtime
                // classpath as it best represents the full common dependency graph. Not every
                // KMP module declares a jvm() target, and the other platforms' runtime
                // classpaths aren't a safe substitute here: Android's (named
                // "androidRuntimeClasspath" under the newer "Android Kotlin Multiplatform
                // Library" AGP plugin) needs build-type/etc. variant attributes that a bare
                // resolve like this doesn't supply, and fails with variant-ambiguity errors
                // against any Android dependency exposing multiple variants. So modules without
                // a jvm() target simply don't get an SBOM artifact, rather than crashing here or
                // resolving one against an arbitrary/ambiguous variant.
                val sbomClasspathConfiguration =
                    "jvmRuntimeClasspath".takeIf { project.configurations.findByName(it) != null }
                val spdxTask = sbomClasspathConfiguration?.let { classpathConfiguration ->
                    extensions.configure<SpdxSbomExtension> {
                        targets.register("release") {
                            // By default, the 'configurations' have only 1 item: "runtimeClasspath".
                            configurations.set(listOf(classpathConfiguration))
                            with (nordicPublishing) {
                                scm {
                                    uri.set(pomScmUrl)
                                    revision.set(gitRevision)
                                }
                                document {
                                    // TODO Use name.set(pomArtifactId) when it is converted to Property
                                    name.set("$group:${pomArtifactId.get()}")
                                    namespace.set(pomUrl.map { "$it${pomArtifactId.get()}/$version/spdx" })
                                    creator.set(pomOrg.map { "Organization: $it" })
                                    packageSupplier.set(pomOrg.map { "Organization: $it" })
                                }
                            }
                        }
                    }
                    tasks.named("spdxSbomForRelease") {
                        fixSpdx(project.group.toString(), nordicPublishing)
                    }
                } ?: run {
                    logger.log(
                        LogLevel.WARN,
                        "WARNING: Skipping SPDX SBOM generation for module ':$name' — no jvm() target declared, so there is no unambiguous runtime classpath to resolve it from."
                    )
                    null
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
                    // KMP creates publications automatically for each target.
                    // Configure all of them with common settings.
                    val moduleName = project.name
                    publications.withType<MavenPublication>().configureEach {
                        // Set publication properties.
                        with(nordicPublishing) {
                            // TODO Use groupId.set(pomGroup) when it is converted to Property
                            groupId = pomGroup.getOrElse(group.toString())
                            // TODO Same here
                            version = effectiveVersion
                            // Unlike single-platform modules, artifactId cannot just be set to
                            // POM_ARTIFACT_ID here: KMP creates one publication per target and they
                            // must keep distinct coordinates. Kotlin has already named them after the
                            // Gradle module -- "<module>" for the root "kotlinMultiplatform"
                            // publication and "<module>-<target>" for each platform one -- so replace
                            // only that prefix and leave the platform suffix intact.
                            //
                            // Left unset, artifactId silently keeps the Gradle module name, which is
                            // rarely what the module wants to be published as.
                            pomArtifactId.orNull?.let {
                                artifactId = it + artifactId.removePrefix(moduleName)
                            }
                        }
                        // Apply POM configuration.
                        pom {
                            from(nordicPublishing)
                        }
                        // Add Dokka HTML docs.
                        artifact(tasks.named("dokkaHtmlJar"))

                        // Only attach SBOM to the root KMP publication, not every platform artifact.
                        // Note:
                        //   If iOS module has some extra dependencies they won't be included here
                        //   in the SBOM. If we reach this situation, we should perhaps add
                        //   SBOM for iOS artifacts as well.
                        if (name == "kotlinMultiplatform" && spdxTask != null) {
                            artifact(spdxTask) {
                                classifier = "sbom"
                                extension = "json"
                            }
                        }
                    }
                    // This task will add *.asc files to the publication for all artifacts.
                    signing.isRequired = isSigningEnabled
                    signing.sign(publications)
                }

                try {
                    rootProject.tasks.register("releaseStagingRepositories", ReleaseStagingRepositoriesTask::class.java)
                } catch (_: Throwable) { }
            }
        }

        target.tasks.withType<AbstractPublishToMaven>().configureEach {
            val signingTasks = target.tasks.withType<Sign>()
            mustRunAfter(signingTasks)
            dependsOn(signingTasks)
        }
    }
}
