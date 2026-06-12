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
 * This method returns the version code based on the current date (YYMM) and number of git revisions.
 * The format is YYMMxxxxx where x is a number of git revisions.
 */
fun Project.getVersionCodeFromTags(): Int {
    val numberOfCommits: Int
    try {
        numberOfCommits = providers
            .exec { commandLine("git", "rev-list", "--count", "HEAD") }
            .standardOutput
            .asText.get().trim()
            .toInt()
    } catch (e: Exception) {
        e.printStackTrace()
        return DEFAULT_CODE
    }
    val now = ZonedDateTime.now(ZoneId.of("UTC"))
    val year = now.year % 100
    val month = String.format("%02d", now.monthValue)
    val day = String.format("%02d", now.dayOfMonth)
    val revisions = String.format("%02d", numberOfCommits % 100)
    val version = "$year$month$day$revisions".toInt()
    return version
}

/**
 * This method returns the version name from the latest git tag.
 */
fun Project.getVersionNameFromTags(): String {
    val latestTag: String
    try {
        latestTag = providers
            .exec { commandLine("git", "describe", "--tags", "--abbrev=0") }
            .standardOutput
            .asText.get().trim()
    } catch (e: Exception) {
        e.printStackTrace()
        return DEFAULT_NAME
    }
    // Version may have % to add additional information
    return latestTag.split("%")[0]
}

/**
 * This method returns the current git revision.
 */
fun Project.getGitRevision(): String {
    return try {
        providers.exec { commandLine("git", "rev-parse", "HEAD") }
            .standardOutput.asText.get().trim()
    } catch (e: Exception) {
        e.printStackTrace()
        "main"
    }
}