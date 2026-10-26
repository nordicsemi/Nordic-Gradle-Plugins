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

import org.gradle.api.Project
import java.time.ZoneId
import java.time.ZonedDateTime

private const val DEFAULT_CODE = 1
private const val DEFAULT_NAME = "0.0.0"

/**
 * A Gradle property that sets the version name, e.g. `-PreleaseVersion=2.0.0-beta08`.
 *
 * The explicit version has precedence over git tags, and does not require any git history.
 */
const val VERSION_NAME_PROPERTY = "releaseVersion"

/**
 * An environment variable that sets the version name, e.g. `RELEASE_VERSION=2.0.0-beta08`.
 * The [VERSION_NAME_PROPERTY] Gradle property has precedence over it.
 */
const val VERSION_NAME_ENV = "RELEASE_VERSION"

/**
 * Runs git with the given arguments and returns the trimmed output, or `null` if git is not
 * available, the command failed (e.g. there are no tags in a shallow clone), or the output is empty.
 */
private fun Project.git(vararg args: String): String? {
    return try {
        val output = providers.exec {
            commandLine("git", *args)
            isIgnoreExitValue = true
        }
        if (output.result.get().exitValue != 0) {
            logger.info("`git ${args.joinToString(" ")}` failed: ${output.standardError.asText.get().trim()}")
            return null
        }
        output.standardOutput.asText.get().trim().ifEmpty { null }
    } catch (e: Exception) {
        logger.info("Could not run `git ${args.joinToString(" ")}`: ${e.message}")
        null
    }
}

/**
 * This method returns the version code based on the current date (YYMM) and number of git revisions.
 * The format is YYMMxxxxx where x is a number of git revisions.
 */
fun Project.getVersionCodeFromTags(): Int {
    val numberOfCommits = git("rev-list", "--count", "HEAD")?.toIntOrNull() ?: return DEFAULT_CODE
    val now = ZonedDateTime.now(ZoneId.of("UTC"))
    val year = now.year % 100
    val month = String.format("%02d", now.monthValue)
    val day = String.format("%02d", now.dayOfMonth)
    val revisions = String.format("%02d", numberOfCommits % 100)
    val version = "$year$month$day$revisions".toInt()
    return version
}

/**
 * This method returns the version name.
 *
 * The version name is taken from (in this order):
 * 1. the [VERSION_NAME_PROPERTY] Gradle property,
 * 2. the [VERSION_NAME_ENV] environment variable,
 * 3. the latest git tag,
 * 4. `0.0.0` if none of the above is available, e.g. when the repository was cloned without tags.
 *
 * Setting the version explicitly allows building a specific version from a shallow clone.
 */
fun Project.getVersionNameFromTags(): String {
    val explicitVersion = providers.gradleProperty(VERSION_NAME_PROPERTY).orNull
        ?: providers.environmentVariable(VERSION_NAME_ENV).orNull
    if (!explicitVersion.isNullOrBlank()) {
        return explicitVersion.trim()
    }
    val latestTag = git("describe", "--tags", "--abbrev=0")
    if (latestTag == null) {
        logger.info("No git tag found, using $DEFAULT_NAME as the version name.")
        return DEFAULT_NAME
    }
    // Version may have % to add additional information
    return latestTag.split("%")[0]
}

/**
 * This method returns the current git revision.
 */
fun Project.getGitRevision(): String {
    return git("rev-parse", "HEAD") ?: "main"
}
