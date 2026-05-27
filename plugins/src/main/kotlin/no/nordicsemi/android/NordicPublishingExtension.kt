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

package no.nordicsemi.android

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.publish.maven.MavenPom
import javax.inject.Inject

/**
 * Gradle extension for configuring publishing POM values.
 *
 * Uses the lazy Property API to avoid eager reads during plugin application.
 * Backwards-compatible uppercase var accessors are provided so existing
 * build scripts that use `POM_NAME = "..."` continue to work.
 */
abstract class NordicPublishingExtension @Inject constructor(objects: ObjectFactory) {
    // Required parameters as Provider-backed properties
    val pomArtifactId: Property<String> = objects.property(String::class.java)
    val pomName: Property<String> = objects.property(String::class.java)
    val pomDescription: Property<String> = objects.property(String::class.java)
    val pomUrl: Property<String> = objects.property(String::class.java)
    val pomScmUrl: Property<String> = objects.property(String::class.java)
    val pomScmConnection: Property<String> = objects.property(String::class.java)
    val pomScmDevConnection: Property<String> = objects.property(String::class.java)

    // Default values:
    val pomGroup: Property<String> = objects.property(String::class.java)

    // License
    val pomLicence: Property<String> = objects.property(String::class.java).convention("BSD-3-Clause")
    val pomLicenceUrl: Property<String> = objects.property(String::class.java).convention("http://opensource.org/licenses/BSD-3-Clause")

    // Developer
    val pomDeveloperId: Property<String> = objects.property(String::class.java).convention("mag")
    val pomDeveloperName: Property<String> = objects.property(String::class.java).convention("Mobile Applications Group")
    val pomDeveloperEmail: Property<String> = objects.property(String::class.java).convention("mag@nordicsemi.no")
    val pomOrg: Property<String> = objects.property(String::class.java).convention("Nordic Semiconductor ASA")
    val pomOrgUrl: Property<String> = objects.property(String::class.java).convention("https://www.nordicsemi.com")

    // Backwards-compatible uppercase setters/getters (keep existing DSL working)
    var POM_ARTIFACT_ID: String?
        get() = pomArtifactId.orNull
        set(value) { if (value != null) pomArtifactId.set(value) }

    var POM_NAME: String?
        get() = pomName.orNull
        set(value) { if (value != null) pomName.set(value) }

    var POM_DESCRIPTION: String?
        get() = pomDescription.orNull
        set(value) { if (value != null) pomDescription.set(value) }

    var POM_URL: String?
        get() = pomUrl.orNull
        set(value) { if (value != null) pomUrl.set(value) }

    var POM_SCM_URL: String?
        get() = pomScmUrl.orNull
        set(value) { if (value != null) pomScmUrl.set(value) }

    var POM_SCM_CONNECTION: String?
        get() = pomScmConnection.orNull
        set(value) { if (value != null) pomScmConnection.set(value) }

    var POM_SCM_DEV_CONNECTION: String?
        get() = pomScmDevConnection.orNull
        set(value) { if (value != null) pomScmDevConnection.set(value) }

    var POM_GROUP: String?
        get() = pomGroup.orNull
        set(value) { if (value != null) pomGroup.set(value) }

    var POM_LICENCE: String
        get() = pomLicence.get()
        set(value) { pomLicence.set(value) }

    var POM_LICENCE_URL: String
        get() = pomLicenceUrl.get()
        set(value) { pomLicenceUrl.set(value) }

    var POM_DEVELOPER_ID: String
        get() = pomDeveloperId.get()
        set(value) { pomDeveloperId.set(value) }

    var POM_DEVELOPER_NAME: String
        get() = pomDeveloperName.get()
        set(value) { pomDeveloperName.set(value) }

    var POM_DEVELOPER_EMAIL: String
        get() = pomDeveloperEmail.get()
        set(value) { pomDeveloperEmail.set(value) }

    var POM_ORG: String
        get() = pomOrg.get()
        set(value) { pomOrg.set(value) }

    var POM_ORG_URL: String
        get() = pomOrgUrl.get()
        set(value) { pomOrgUrl.set(value) }
}

internal fun MavenPom.from(
    extension: NordicPublishingExtension,
) = with (extension) {
    pomName.orNull?.let { name.set(it) }
    pomDescription.orNull?.let { description.set(it) }
    pomUrl.orNull?.let { url.set(it) }

    // https://maven.apache.org/pom.html#licenses
    licenses {
        license {
            name.set(pomLicence.get())
            url.set(pomLicenceUrl.get())
            // The two stated methods are repo (they may be downloaded from a Maven repository) or manual (they must be manually installed).
            distribution.set("repo")
        }
    }

    // https://maven.apache.org/pom.html#scm
    scm {
        pomScmUrl.orNull?.let { url.set(it) }
        pomScmConnection.orNull?.let { connection.set(it) }
        pomScmDevConnection.orNull?.let { developerConnection.set(it) }
    }

    // https://maven.apache.org/pom.html#organization
    organization {
        name.set(pomOrg.get())
        url.set(pomOrgUrl.get())
    }

    // https://maven.apache.org/pom.html#developers
    developers {
        developer {
            id.set(pomDeveloperId.get())
            name.set(pomDeveloperName.get())
            email.set(pomDeveloperEmail.get())
            organization.set(pomOrg.get())
            organizationUrl.set(pomOrgUrl.get())
        }
    }
}
